package com.meizu.testdevVideo.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.meizu.common.app.LoadingDialog;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.adapter.SchemaAdapter;
import com.meizu.testdevVideo.adapter.data.listview.SchemaInfo;
import com.meizu.testdevVideo.adapter.data.listview.TestCaseData;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.PostUploadHelper;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.download.FileDownloadCallBack;
import com.meizu.testdevVideo.util.download.FileDownloadHelper;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.shell.ShellUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Ota历史记录测试
 */
public class OtaHistoryFragment extends Fragment {

    private View view;
    private static final int UPDATE_LIST = 1000;
    private static final int UPDATE_FAIL = 1001;
    private static final int UPDATE_VIEW = 1002;
    private static final int THREAD_NUM = 2;
    private static String m_url = "";
    private List<SchemaInfo> schemaInfos = new ArrayList<SchemaInfo>();
    private SchemaAdapter schemaAdapter;
    private ListView listView;
    private Activity mActivity;
    private LoadingDialog dialog;


    public OtaHistoryFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        if(null == view){
            view = inflater.inflate(R.layout.fragment_ota_history, container, false);
            mActivity = getActivity();
            listView = (ListView) view.findViewById(R.id.list_history);
            dialog = new LoadingDialog(mActivity);
            dialog.setMessage("正在加载");
            dialog.setCancelable(false);
            getdata();
        }

        return view;
    }


    private void getdata(){
        new SimpleTaskHelper(){
            @Override
            protected void doInBackground() {
//                try {
                    Map<String, String> loadingParams = new HashMap<String, String>();

//                    loadingParams.put("imei", imei);
//                    loadingParams.put("package", package_name);
//
//                    PostUploadHelper.getInstance().submitPostData(m_push_url, loadingParams, new PostCallBack() {
//                        @Override
//                        public void resultCallBack(boolean isSuccess, int resultCode, String data) {
//                            if(isSuccess && null != data){
//                                mTestCaseData = new ArrayList<TestCaseData>();
//                                JSONObject jsonObject = JSON.parseObject(data);
//                                int status = jsonObject.getInteger("status");
//                                message = jsonObject.getString("message");
//                                if(0 == status) {
//                                    handler.sendEmptyMessage(REQUEST_PASS);
//                                }else {
//                                    handler.sendEmptyMessage(REQUEST_FAIL);
//                                }
//
//                            }else{
//                                if(!isSuccess){
//                                    handler.sendEmptyMessage(REQUEST_NO_NET);
//                                }else{
//                                    handler.sendEmptyMessage(REQUEST_FAIL);
//                                }
//                            }
//                        }
//                    });
//                } catch (IOException e) {
//                    Logger.d("更新包==>" + e.toString());
////                    handler.sendEmptyMessage(REQUEST_FAIL);
//                    e.printStackTrace();
//                }
            }
        }.executeInSerial();

    }

//    @SuppressLint("HandlerLeak")
//    private Handler handler=new Handler(Looper.getMainLooper()) {
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case REQUEST_START:
//                    break;
//                case REQUEST_PASS:
//                    if (m_push_type == "0"){
//                        if (m_push_value == "0"){
//                            ToastHelper.addToast("入库成功", SuperTestApplication.getContext());
//                        }else if (m_push_value == "1"){
//                            ToastHelper.addToast("设备在线", SuperTestApplication.getContext());
//                        }else if (m_push_value == "2"){
//                            ToastHelper.addToast("订阅成功", SuperTestApplication.getContext());
//                        }
//                    }
//
//                    break;
//                case REQUEST_NO_NET:
//                    mProgress.setVisibility(View.GONE);
//                    fresh_notified.setVisibility(View.VISIBLE);
////                        txt_notified.setText("非公司网络！");
//                    break;
//                case REQUEST_FAIL:
//                    if (m_push_type == "0"){
//                        if (m_push_value == "0"){
//                            ToastHelper.addToast("入库失败", SuperTestApplication.getContext());
//                        }else if (m_push_value == "1"){
//                            ToastHelper.addToast("设备不在线", SuperTestApplication.getContext());
//                        }else if (m_push_value == "2"){
//                            ToastHelper.addToast("订阅失败", SuperTestApplication.getContext());
//                        }
//                    }
////                        mProgress.setVisibility(View.GONE);
////                        fresh_notified.setVisibility(View.VISIBLE);
////                        txt_notified.setText(message);
//                    break;
//                default:
//                    ToastHelper.addToast("发送异常", SuperTestApplication.getContext());
//                    break;
//            }
//        }
//    };




    @Override
    public void onDestroy() {
//        if(null != handler){
//            handler.removeCallbacksAndMessages(null);
//        }
        super.onDestroy();
    }
}
