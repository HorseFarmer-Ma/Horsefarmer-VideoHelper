package com.meizu.testdevVideo.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.UpdateAppFragmentAdapter;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.util.ftp.FtpHelper;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.widget.viewpage.EdgeViewPage;

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
    private EdgeViewPage viewPager;
    private LinearLayout progressBar, no_netword, viewpager_touch;
    private static final int UPDATE_VIEW_PAGER = 100;
    private static final int NO_NET = 400;
    private List<Fragment> listFragment;
    private UpdateAppFragmentAdapter updateAppFragmentAdapter;
    private FTPClient client;
    private static final String TAG = "UpdateSoftwareFtp";
    private static final String SOFTWARE_PATH = "/MediaAppUpdate";
    private static final int TOTAL_COUNT = 3;
    private int pageCount;

    public UpdateSoftwareFtpFragment() {
        listFragment = new ArrayList<Fragment>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(null == view){
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


    private Thread getFtpData = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                if(!client.isConnected()) {
                    FtpHelper.connect(client, iPublicConstants.HOST, iPublicConstants.PORT,
                            iPublicConstants.USERNAME, iPublicConstants.PASSWORD);
                }
                long currentTime = System.currentTimeMillis();
                while (!client.isConnected() && 1000 == (System.currentTimeMillis() - currentTime));
                if(client.isConnected()){
                    String dir = client.currentDirectory();
                    client.changeDirectory(dir + SOFTWARE_PATH);
                    List<Map<String, Object>> listName = FtpHelper.getFolderName(client, iPublicConstants.FILENAME);
                    pageCount = listName.size();
                    for(int i = 0; i < pageCount; i++){
                        String fileName = listName.get(i).get(iPublicConstants.FILENAME).toString();
                        Log.d(TAG, "文件名为：" + fileName);
                        listFragment.add(NewAppUpdateFragment.newInstance(fileName, dir));
                    }
                    updateAppFragmentAdapter = new UpdateAppFragmentAdapter(getActivity().getSupportFragmentManager(), listFragment);
                    if(null != handler){
                        handler.sendEmptyMessage(UPDATE_VIEW_PAGER);
                    }

                }else {
                    if(null != handler){
                        handler.sendEmptyMessage(NO_NET);
                    }
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

                break;
                case NO_NET:
                    try {
                        progressBar.setVisibility(View.GONE);
                        no_netword.setVisibility(View.VISIBLE);
                    }catch(NullPointerException e){
                        System.out.print(e.toString());
                    }

                break;
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (view != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if(null != handler){
            handler = null;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FtpHelper.disConnect(client);
            }
        }).start();

        BaseData.getInstance(getActivity().getApplicationContext()).writeIntData("viewpager_position", viewPager.getCurrentItem());
    }
}
