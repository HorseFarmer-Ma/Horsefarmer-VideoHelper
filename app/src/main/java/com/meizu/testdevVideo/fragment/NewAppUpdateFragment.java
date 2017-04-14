package com.meizu.testdevVideo.fragment;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.UpdateAppListAdapter;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.download.DownloadHelper;
import com.meizu.testdevVideo.util.ftp.FtpHelper;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.widget.listview.RefreshListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

/**
 * 更新应用Fragment
 */
public class NewAppUpdateFragment extends Fragment implements RefreshListView.OnRefreshListener, UpdateAppListAdapter.DownloadClickListener{

    private static final String TAG = NewAppUpdateFragment.class.getSimpleName();
    private static final String APP_TYPE = "app_type";
    private static final String DIR = "dir";
    private static final int UPDATE_LIST_WITH_FRESH = 100;
    private static final int UPDATE_LIST_WITHOUT_FRESH = 200;
    private static final int FTP_NOT_CONTENT = 300;
    private static final int NO_NET = 400;
    private View view;
    private RefreshListView listView;
    private FTPClient client;
    private String appType;
    private List<Map<String, Object>> adapterList;
    private List<Map<String, Object>> updateList = null;
    private String dir;
    private UpdateAppListAdapter mAdapter;
    private TextView titleList, text_no_content, lastUpdate_view;
    private ProgressBar refresh_icon;
    private static final String DOWNLOAD_ADRESS = "http://ats.meizu.com/static/upload/user-resources/SuperTest/MediaAppUpdate/";

    public NewAppUpdateFragment() {
        adapterList = new ArrayList<Map<String, Object>>();
    }

    /**
     * 静态工厂方法
     * 返回新的fragment到调用者
     */
    public static NewAppUpdateFragment newInstance(String appType, String dir) {
        NewAppUpdateFragment f = new NewAppUpdateFragment();
        Bundle args = new Bundle();
        args.putString(APP_TYPE, appType);
        args.putString(DIR, dir);
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

        titleList = (TextView) view.findViewById(R.id.title_list);
        text_no_content = (TextView) view.findViewById(R.id.text_no_content);
        lastUpdate_view = (TextView) view.findViewById(R.id.lastUpdate_view);
        listView = (RefreshListView) view.findViewById(R.id.refreshUpdateAppListView);
        refresh_icon = (ProgressBar) view.findViewById(R.id.refresh_icon);

        text_no_content.setVisibility(View.GONE);
        titleList.setText(appType);
        checkUpdate();
        mAdapter = new UpdateAppListAdapter(getActivity().getApplicationContext(), adapterList);
        mAdapter.setOnDownloadClickListener(this);
        listView.setonRefreshListener(this);
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
        onDownloadListener.onDownloadListener(DownloadHelper.getInstance(getActivity().getApplicationContext())
                .download(DOWNLOAD_ADRESS + appType + "/" + adapterList.get(position).get(iPublicConstants.FILENAME),
                        "/SuperTest/UpdateApk/", appType + ".apk"));
        BaseData.getInstance(getActivity().getApplicationContext()).writeStringData(appType,
                adapterList.get(position).get(iPublicConstants.FILENAME).toString());
        ToastHelper.addToast("下载中..", getActivity().getApplicationContext());
        checkUpdate();
    }

    @Override
    public void onRefresh() {
        new Thread(new MyRunnable(true)).start();
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
                    while (!client.isConnected() && 1000 == (System.currentTimeMillis() - currentTime));
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
                        if(null != handler){
                            handler.sendEmptyMessage(UPDATE_LIST_WITH_FRESH);
                        }
                    }else{
                        if(null != handler) {
                            handler.sendEmptyMessage(UPDATE_LIST_WITHOUT_FRESH);
                        }
                    }
                }else {
                    if(null != handler){
                        handler.sendEmptyMessage(FTP_NOT_CONTENT);
                    }
                }
            }else{
                if(null != handler) {
                    handler.sendEmptyMessage(NO_NET);
                }
            }
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_LIST_WITH_FRESH:
                    try {
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
                case FTP_NOT_CONTENT:
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
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if(null != handler){
            handler = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != view) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    private static OnDownloadListener onDownloadListener;

    public interface OnDownloadListener{
        void onDownloadListener(String id);
    }

    public static void setOnDownloadListener(OnDownloadListener listener){
        onDownloadListener = listener;
    }

}
