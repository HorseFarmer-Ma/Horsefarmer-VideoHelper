package com.meizu.testdevVideo.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.meizu.common.widget.GuidePopupWindow;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.adapter.UpdateAppFragmentAdapter;
import com.meizu.testdevVideo.constant.FragmentUtils;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.ftp.FtpHelper;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.SettingPreference;
import com.meizu.widget.viewpage.EdgeViewPage;

import java.io.File;
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


public class UpdateSoftwareFtpFragment extends Fragment{

    private View view;
    private Menu mMenu;
    private EdgeViewPage viewPager;
    private LinearLayout progressBar, no_netword, viewpager_touch;
    private GuidePopupWindow guideChooseTab;
    private GuidePopupWindow guideInfoDir;
    private static final int UPDATE_VIEW_PAGER = 100;
    private static final int UPDATE_VIEW_MENU = 200;
    private static final int NO_NET = 400;
    private List<Fragment> listFragment;
    private UpdateAppFragmentAdapter updateAppFragmentAdapter;
    private FTPClient client;
    private static final String TAG = "UpdateSoftwareFtp";
    private static final String SOFTWARE_PATH = "/MediaAppUpdate";
    private static final String UPDATE_APP_GUIDE = "UpdateAppGuide";
    private static final String INFO_DIR_GUIDE = "InfoDirGuide";
    private static final int TOTAL_COUNT = 3;
    private int pageCount;
    private List<Map<String, Object>> listName;
    public UpdateSoftwareFtpFragment() {
        listFragment = new ArrayList<Fragment>();
    }
    private String type;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(null == view){
            Bundle bundle = getArguments();
            if(null != bundle){
                type = bundle.getString(FragmentUtils.FRAGMENT_TYPE);
                if(null != type){
                    Logger.d("从公共Activity跳转过来的" + type);
                }
            }
            view = inflater.inflate(R.layout.fragment_update_software_ftp, container, false);
            initView(view);
        }
        return view;
    }

    /**
     * 初始化view
     * @param view
     */
    private void initView(View view){
        setHasOptionsMenu(true);
        no_netword = (LinearLayout) view.findViewById(R.id.no_netword);
        viewpager_touch = (LinearLayout) view.findViewById(R.id.viewpager_touch);
        no_netword.setVisibility(View.GONE);
        viewPager = (EdgeViewPage) view.findViewById(R.id.update_viewpager);
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.page_margin));
        viewPager.setOffscreenPageLimit(TOTAL_COUNT);
        viewPager.setPageTransformer(true, viewPager);

        progressBar = (LinearLayout) view.findViewById(R.id.update_progress_bar);

        viewpager_touch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return viewPager.dispatchTouchEvent(event);
            }
        });

        if(null == client){
            client = new FTPClient();
        }
        getFtpData.start();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        menu.add(0, 0, 0, "item1")
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        mMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        viewPager.setCurrentItem(id);
        if(guideChooseTab != null){
            guideChooseTab.dismiss();
            guideChooseTab = null;
        }
        if(guideInfoDir != null){
            guideInfoDir.dismiss();
            guideInfoDir = null;
        }
        return super.onOptionsItemSelected(item);
    }

    private Thread getFtpData = new Thread(new Runnable() {
        @Override
        public void run() {
            try {

                if(SettingPreference.getInstance(SuperTestApplication.getContext())
                        .getSettingSharedPreferences().getBoolean(SettingPreferenceKey.IF_CLEAR_APP_UPDATE, false)){
                    // 删除更新包
                    File mApk = new File(iPublicConstants.LOCAL_MEMORY + "/SuperTest/UpdateApk/");
                    if(mApk.exists()){
                        PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "/SuperTest/UpdateApk");
                    }
                }

                if(!client.isConnected()) {
                    FtpHelper.connect(client, iPublicConstants.HOST, iPublicConstants.PORT,
                            iPublicConstants.USERNAME, iPublicConstants.PASSWORD);
                }
                long currentTime = System.currentTimeMillis();
                while (!client.isConnected() && 1000 == (System.currentTimeMillis() - currentTime));
                if(client.isConnected()){
                    String dir = client.currentDirectory();
                    client.changeDirectory(dir + SOFTWARE_PATH);
                    if(null != listName){
                        listName.clear();
                    }

                    listName = FtpHelper.getFolderName(client, iPublicConstants.FILENAME);
                    handler.sendEmptyMessage(UPDATE_VIEW_MENU);
                    pageCount = listName.size();
                    for(int i = 0; i < pageCount; i++){
                        String fileName = listName.get(i).get(iPublicConstants.FILENAME).toString();
                        Log.d(TAG, "文件名为：" + fileName);
                        listFragment.add(NewAppUpdateFragment.newInstance(fileName, dir, type));

                    }
                    updateAppFragmentAdapter = new UpdateAppFragmentAdapter(getActivity().getSupportFragmentManager(), listFragment);
                    handler.sendEmptyMessage(UPDATE_VIEW_PAGER);
                }else {
                    handler.sendEmptyMessage(NO_NET);
                }
            } catch (IOException e) {
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
            } catch (IllegalStateException e){
                e.printStackTrace();
            }

        }
    });



    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_VIEW_PAGER:
                    try {
                        progressBar.setVisibility(View.GONE);
                        viewPager.setAdapter(updateAppFragmentAdapter);
                        int position = BaseData.getInstance(getActivity().getApplicationContext()).readIntData("viewpager_position");
                        if(position < pageCount){
                            viewPager.setCurrentItem(position);
                        }else{
                            viewPager.setCurrentItem(pageCount/2);
                        }
                    }catch (NullPointerException e){
                        System.out.print(e.toString());
                    }

                    if(!BaseData.getInstance(SuperTestApplication.getContext()).readBooleanData(INFO_DIR_GUIDE)){
                        guideInfoDir = new GuidePopupWindow(SuperTestApplication.getContext());
                        guideInfoDir.setMessage("点这里，进入已下载APK路径");
                        guideInfoDir.setOutsideTouchable(false);
                        guideInfoDir.setLayoutMode(GuidePopupWindow.GUIDE_LAYOUT_MODE_DOWN);
                        guideInfoDir.show(viewPager, 0, -40);
                        BaseData.getInstance(SuperTestApplication.getContext()).writeBooleanData(INFO_DIR_GUIDE, true);
                    }

                break;
                case NO_NET:
                    try {
                        progressBar.setVisibility(View.GONE);
                        no_netword.setVisibility(View.VISIBLE);
                    }catch(NullPointerException e){
                        System.out.print(e.toString());
                    }

                break;
                case UPDATE_VIEW_MENU:
                    for(int i = 0; i < listName.size(); i++){
                        mMenu.add(0, i, 0, listName.get(i).get(iPublicConstants.FILENAME).toString());
                    }
                    if(!BaseData.getInstance(SuperTestApplication.getContext()).readBooleanData(UPDATE_APP_GUIDE)){
                        guideChooseTab = new GuidePopupWindow(SuperTestApplication.getContext());
                        guideChooseTab.setMessage("点这里，快速定位业务");
                        guideChooseTab.setOutsideTouchable(false);
                        guideChooseTab.setLayoutMode(GuidePopupWindow.GUIDE_LAYOUT_MODE_DOWN);
                        guideChooseTab.setArrowPosition(viewPager.getRootView().getWidth()/2);
                        guideChooseTab.show(viewPager, viewPager.getRootView().getWidth()/5, -viewPager.getHeight());
                        BaseData.getInstance(SuperTestApplication.getContext()).writeBooleanData(UPDATE_APP_GUIDE, true);
                    }

                    break;
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(guideChooseTab != null){
            guideChooseTab.dismiss();
            guideChooseTab = null;
        }
        if(guideInfoDir != null){
            guideInfoDir.dismiss();
            guideInfoDir = null;
        }
        if (view != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        new Thread(new Runnable() {
            @Override
            public void run() {
                FtpHelper.disConnect(client);
            }
        }).start();

        BaseData.getInstance(SuperTestApplication.getContext()).writeIntData("viewpager_position", viewPager.getCurrentItem());
        if(null != handler){
            handler.removeCallbacksAndMessages(null);
        }
    }
}
