package com.meizu.testdevVideo.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meizu.common.widget.LoadingAnimotionView;
import com.meizu.common.widget.LoadingView;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.activity.CommonToolsActivity;
import com.meizu.testdevVideo.activity.OtaPushActivity;
import com.meizu.testdevVideo.activity.PostSearchActivity;
import com.meizu.testdevVideo.adapter.UpdateAppListAdapter;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.FragmentUtils;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.FileSizeUtil;
import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.PostUploadHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.download.DownloadHelper;
import com.meizu.testdevVideo.util.ftp.FtpHelper;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.widget.listview.RefreshListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

import static android.app.Activity.RESULT_OK;

/**
 * 更新应用Fragment
 */
public class NewAppUpdateFragment extends Fragment implements RefreshListView.OnRefreshListener
        , UpdateAppListAdapter.DownloadClickListener, View.OnClickListener{

    private static final String TAG = NewAppUpdateFragment.class.getSimpleName();
    private static final String APP_TYPE = "app_type";
    private static final String DIR = "dir";
    private static final int UPDATE_LIST_WITH_FRESH = 100;
    private static final int UPDATE_LIST_WITHOUT_FRESH = 200;
    private static final int FTP_NOT_CONTENT = 300;
    private static final int NO_NET = 400;
    private static final int UPDATE_MEM = 500;
    private static final int LOADING_VIEW_GONE = 600;
    private String txtMem;
    private View view;
    private RefreshListView listView;
    private LoadingView loadRefreshApk;
    private FTPClient client;
    private String appType;
    private List<Map<String, Object>> adapterList;
    private List<Map<String, Object>> updateList = null;
    private String dir;
    private UpdateAppListAdapter mAdapter;
    private TextView titleList, text_no_content, lastUpdate_view, txt_mem;
    private LoadingAnimotionView refresh_icon;
    private RelativeLayout relativeLayout_title;
    private boolean isGetRespond = false;
    private String type;


    public NewAppUpdateFragment() {
        adapterList = new ArrayList<Map<String, Object>>();
    }

    /**
     * 静态工厂方法
     * 返回新的fragment到调用者
     */
    public static NewAppUpdateFragment newInstance(String appType, String dir, String fragmentType) {
        NewAppUpdateFragment f = new NewAppUpdateFragment();
        Bundle args = new Bundle();
        args.putString(APP_TYPE, appType);
        args.putString(DIR, dir);
        args.putString(FragmentUtils.FRAGMENT_TYPE, fragmentType);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(null == view){
            view = inflater.inflate(R.layout.fragment_new_app_update, container, false);
            initView(view);
        }
        return view;
    }


    /**
     * 初始化视图
     * @param view 视图
     */
    private void initView(View view){
        dir = getArguments().getString(DIR);
        appType = getArguments().getString(APP_TYPE);
        type = getArguments().getString(FragmentUtils.FRAGMENT_TYPE);

        File fileDirector = new File(iPublicConstants.LOCAL_MEMORY + "SuperTest/UpdateApk/" + appType + "/");
        if(!fileDirector.exists()){
            fileDirector.mkdirs();
        }else{
            if(!fileDirector.isDirectory()){
                fileDirector.delete();
                fileDirector.mkdirs();
            }
        }
        titleList = (TextView) view.findViewById(R.id.title_list);
        relativeLayout_title = (RelativeLayout) view.findViewById(R.id.relativeLayout_title);
        text_no_content = (TextView) view.findViewById(R.id.text_no_content);
        lastUpdate_view = (TextView) view.findViewById(R.id.lastUpdate_view);
        txt_mem = (TextView) view.findViewById(R.id.txt_mem);
        listView = (RefreshListView) view.findViewById(R.id.refreshUpdateAppListView);
        refresh_icon = (LoadingAnimotionView) view.findViewById(R.id.refresh_icon);
        loadRefreshApk = (LoadingView) view.findViewById(R.id.load_refresh_apk);

        if(null == isMtBusiness(appType)){
            loadRefreshApk.setVisibility(View.GONE);
        }

        text_no_content.setVisibility(View.GONE);
        titleList.setText(appType);
        checkUpdate();
        mAdapter = new UpdateAppListAdapter(getActivity().getApplicationContext(), adapterList, type);
        mAdapter.setOnDownloadClickListener(this);
        listView.setonRefreshListener(this);
        relativeLayout_title.setOnClickListener(this);
        new Thread(new MyRunnable(false)).start();
    }

    private void checkUpdate(){
        String updateVersion = BaseData.getInstance(getActivity().getApplicationContext()).readStringData(appType);
        if(null == updateVersion){
            lastUpdate_view.setText("暂未更新");
        }else{
            lastUpdate_view.setText(String.format("上次更新：%s", updateVersion));
        }
    }

    @Override
    public void onItemClick(int position) {

        if(null != type && type.equals(FragmentUtils.CHOOSE_APP_FRAGMENT)){
            String fileName = adapterList.get(position).get(iPublicConstants.FILENAME).toString();
            Intent intent = new Intent(getActivity(), OtaPushActivity.class);
            intent.putExtra("URL", Constants.AppUpdate.DOWNLOAD_ADRESS + appType + "/" + fileName);
            getActivity().setResult(RESULT_OK, intent);
            getActivity().finish();

        }else{
            String fileName = adapterList.get(position).get(iPublicConstants.FILENAME).toString();
            File file = new File(iPublicConstants.LOCAL_MEMORY + "SuperTest/UpdateApk/" + appType + "/" + fileName);

            if(file.exists()){
                file.delete();
            }

            String id = DownloadHelper.getInstance(getActivity().getApplicationContext())
                    .download(Constants.AppUpdate.DOWNLOAD_ADRESS + appType + "/" + fileName,
                            "/SuperTest/UpdateApk/" + appType + "/", fileName);

            Constants.UpdateAppValue.appUpdateStringlist.add(id);
            BaseData.getInstance(getActivity().getApplicationContext()).writeStringData(appType, fileName);
            ToastHelper.addToast("下载中..", getActivity().getApplicationContext());
            checkUpdate();
        }
    }

    @Override
    public void onRefresh() {
        new Thread(new MyRunnable(true)).start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.relativeLayout_title:
                openAssignFolder(iPublicConstants.LOCAL_MEMORY + "SuperTest/UpdateApk/" + appType);
            break;
        }
    }

    private void openAssignFolder(String path){
        Intent intent = new Intent("com.meizu.flyme.filemanager.action.VIEW_DIRECTORY");
        intent.putExtra("init_directory", path);
//        intent.putExtra("selected_file","/sdcard/Android/text.txt");
        intent.putExtra("other_app",true);
        startActivity(intent);
    }

    /**
     * 执行获取FTP列表操作
     */
    class MyRunnable implements Runnable{
        private boolean isFresh;

        public MyRunnable(boolean flag){
            this.isFresh = flag;
        }

        @Override
        public void run() {
            try {
                if(null == client){
                    client = new FTPClient();
                }

                if(!client.isConnected()){
                    FtpHelper.connect(client, iPublicConstants.HOST, iPublicConstants.PORT,
                            iPublicConstants.USERNAME, iPublicConstants.PASSWORD);
                    long currentTime = System.currentTimeMillis();
                    while (!client.isConnected()){
                        Thread.sleep(100);
                        if(System.currentTimeMillis() - currentTime > 1000){
                            break;
                        }
                    }
                }

                if(client.isConnected()){
                    client.changeDirectory(dir + "/MediaAppUpdate/" + appType);
                    Thread.sleep(500);
                    updateList = FtpHelper.getApkName(client, iPublicConstants.FILENAME);
                }
            } catch (IOException e) {
                if(client.isConnected()){
                    try {
                        client.disconnect(false);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (FTPIllegalReplyException e1) {
                        e1.printStackTrace();
                    } catch (FTPException e1) {
                        e1.printStackTrace();
                    }
                }
                e.printStackTrace();
            } catch (FTPIllegalReplyException e) {
                e.printStackTrace();
            } catch (FTPException e) {
                e.printStackTrace();
            } catch (FTPDataTransferException e) {
                e.printStackTrace();
            } catch (FTPListParseException e) {
                e.printStackTrace();
            } catch (FTPAbortedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(client.isConnected()){
                if(null != updateList && 0 != updateList.size()){
                    if(this.isFresh){
                        handler.sendEmptyMessage(UPDATE_LIST_WITH_FRESH);
                    }else{
                        handler.sendEmptyMessage(UPDATE_LIST_WITHOUT_FRESH);
                    }
                }else {
                    handler.sendEmptyMessage(FTP_NOT_CONTENT);
                }

                if(!this.isFresh && null != isMtBusiness(appType)){
                    Map<String, String> loadingParams = new HashMap<String, String>();
                    loadingParams.put("m_package_name", isMtBusiness(appType));
                    try {
                        PostUploadHelper.getInstance().submitPostData(iPublicConstants.LOADING_APP_UPDATE, loadingParams, new PostCallBack() {
                            @Override
                            public void resultCallBack(boolean isSuccess, int resultCode, String data) {
                                if(isSuccess && null != data){
                                    JSONObject json = JSON.parseObject(data);
                                    int status = json.getInteger("status");
                                    isGetRespond = true;
                                    if(0 == status){
                                        Logger.d("更新包成功");
                                        new Thread(new MyRunnable(true)).start();
                                    }else{
                                        Logger.d("更新包失败");
                                        handler.sendEmptyMessage(LOADING_VIEW_GONE);
                                    }
                                }else{
                                    Logger.d("更新包失败");
                                    handler.sendEmptyMessage(LOADING_VIEW_GONE);
                                }

                            }
                        });
                    } catch (IOException e) {
                        Logger.d("更新包==>" + e.toString());
                        handler.sendEmptyMessage(LOADING_VIEW_GONE);
                        e.printStackTrace();
                    }
                }
            }else{
                if(null != handler) {
                    handler.sendEmptyMessage(NO_NET);
                }
            }

            File file = new File(iPublicConstants.LOCAL_MEMORY + "SuperTest/UpdateApk/" + appType + "/");
            if(!file.exists()){file.mkdirs();}
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_LIST_WITH_FRESH:
                    try {
                        if(isGetRespond){
                            Logger.d("刷新列表数据");
                            loadRefreshApk.setVisibility(View.GONE);
                        }
                        refresh_icon.setVisibility(View.GONE);
                        adapterList.clear();
                        if(null != updateList){
                            for(int i = 0; i < updateList.size(); i++){
                                adapterList.add(i, updateList.get(i));
                            }
                            text_no_content.setVisibility(View.GONE);
                            mAdapter.notifyDataSetChanged();
                        }
                        listView.onRefreshComplete();
                    }catch (NullPointerException e){
                        System.out.print(e.toString());
                    }
                    break;
                case LOADING_VIEW_GONE:
                    loadRefreshApk.setVisibility(View.GONE);
                    break;
                case UPDATE_LIST_WITHOUT_FRESH:
                    try {
                        refresh_icon.setVisibility(View.GONE);
                        text_no_content.setVisibility(View.GONE);
                        adapterList.clear();
                        if(null != updateList){
                            for(int i = 0; i < updateList.size(); i++){
                                adapterList.add(i, updateList.get(i));
                            }
                            listView.setAdapter(mAdapter);
                        }
                    }catch (NullPointerException e){
                        System.out.print(e.toString());
                    }

                    break;

                case UPDATE_MEM:
                    txtMem = "已占用: " + FileSizeUtil
                            .getAutoFileOrFilesSize(iPublicConstants.LOCAL_MEMORY + "SuperTest/UpdateApk/" + appType);
                    txt_mem.setText(txtMem);
                    break;

                case FTP_NOT_CONTENT:
                    if(isGetRespond){
                        Logger.d("刷新列表数据");
                        loadRefreshApk.setVisibility(View.GONE);
                    }

                    try {
                        adapterList.clear();
                        listView.setAdapter(mAdapter);
                        refresh_icon.setVisibility(View.GONE);
                        text_no_content.setVisibility(View.VISIBLE);
                        text_no_content.setText("空空如也");
                        listView.onRefreshComplete();
                    }catch (NullPointerException e){
                        System.out.print(e.toString());
                    }

                    break;
                case NO_NET:
                    try {
                        adapterList.clear();
                        listView.setAdapter(mAdapter);
                        refresh_icon.setVisibility(View.GONE);
                        text_no_content.setVisibility(View.VISIBLE);
                        text_no_content.setText("网络异常，下拉刷新");
                        listView.onRefreshComplete();
                    }catch (NullPointerException e){
                        System.out.print(e.toString());
                    }
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if(null != handler){
            handler.sendEmptyMessage(UPDATE_MEM);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != handler){
            handler.removeCallbacksAndMessages(null);
        }
        if(adapterList != null){
            adapterList = null;
        }
        if(updateList != null){
            updateList = null;
        }
        if(mAdapter != null){
            mAdapter = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != view) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    // 获取查看是否为多媒体业务
    private String isMtBusiness(String dir){
        if(dir.equals("Video")){
            return iPublicConstants.PACKET_VIDEO;
        }else if(dir.equals("Music")){
            return iPublicConstants.PACKET_MUSIC;
        }else if(dir.equals("Browser")){
            return iPublicConstants.PACKET_BROWSER;
        }else if(dir.equals("Customize_function")){
            return iPublicConstants.PACKET_THEME;
        }else if(dir.equals("Reader")){
            return iPublicConstants.PACKET_READER;
        }else if(dir.equals("ebook")){
            return iPublicConstants.PACKET_EBOOK;
        }else{
            return null;
        }
    }
}
