package com.meizu.testdevVideo.fragment;

import android.content.DialogInterface;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.LinearLayout;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.meizu.common.app.LoadingDialog;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.activity.CommonToolsActivity;
import com.meizu.testdevVideo.activity.OtaPushActivity;
import com.meizu.testdevVideo.adapter.data.gridview.MyContent;
import com.meizu.testdevVideo.adapter.data.listview.TestCaseData;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.fragment.PerformsFragment;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.library.PostUploadHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.download.FileDownloadHelper;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.sharepreference.PushData;
import com.meizu.testdevVideo.util.shell.ShellUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import flyme.support.v7.app.AlertDialog;

/**
 * Ota测试
 */
public class OtaTestFragment extends Fragment {

    private View view;
    private static final int UPDATE_LIST = 1000;
    private static final int UPDATE_FAIL = 1001;
    private static final int UPDATE_VIEW = 1002;
    private String downloadUrl;
    private static final int THREAD_NUM = 2;
    private boolean isDownloading = false;
    private ListView listView;
    private FileDownloadHelper fileDownloadHelper;
    private Activity mActivity;
    private LoadingDialog dialog;
    private TextView textView;
    private AbsListView mToolListView;
    private AbsListView mToolPushListView;
    private AbsListView mToolTaskDetailView;
    private AbsListView mToolTaskListView;
    private SimpleAdapter mToolAdapter;
    private MyContent mSipToolContent;
    private MyContent mPushToolContent;
    private MyContent mDetailToolContent;
    private MyContent mTaskToolContent;
    private View rootView;

    private LinearLayout mProgress;
    private LinearLayout fresh_notified;
    private TextView txt_notified;
    private String message;
    private List<TestCaseData> mTestCaseData;
    private boolean isGetRespond = false;

    private static final int REQUEST_START = 100;
    private static final int REQUEST_PASS = 200;
    private static final int REQUEST_FAIL = 500;
    private static final int REQUEST_NO_NET = 404;
    private static String m_push_url = "";
    private static String m_push_type = "";
    private static String m_push_value = "";
    public String M_STATUS = "0";

    public OtaTestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(null == rootView){
            //sip在线
            rootView = inflater.inflate(R.layout.fragment_ota_test, container, false);
            mToolListView = (AbsListView) rootView.findViewById(R.id.siplist);

            mSipToolContent = new MyContent();
            mSipToolContent.addItem(new MyContent.DummyItem("一键入库", R.drawable.ic_record));
            mSipToolContent.addItem(new MyContent.DummyItem("检测在线", R.drawable.ic_record));
            mSipToolContent.addItem(new MyContent.DummyItem("检测订阅", R.drawable.ic_record));
            mToolAdapter = new SimpleAdapter(getActivity(), mSipToolContent.ITEMS, R.layout.tool_listview,
                    new String[]{"text", "img"},
                    new int[]{R.id.tool_text, R.id.tool_img});    // 生成列表数据
            mToolListView.setAdapter(mToolAdapter);
            mToolListView.setOnItemClickListener(new OtaTestFragment.OnItemClickListener());

            //推送任务
            mToolPushListView = (AbsListView) rootView.findViewById(R.id.pushlist);

            mPushToolContent = new MyContent();
            mPushToolContent.addItem(new MyContent.DummyItem("新增任务", R.drawable.ic_record));
            mPushToolContent.addItem(new MyContent.DummyItem("历史任务", R.drawable.ic_record));
            mToolAdapter = new SimpleAdapter(getActivity(), mPushToolContent.ITEMS, R.layout.tool_listview,
                    new String[]{"text", "img"},
                    new int[]{R.id.tool_text, R.id.tool_img});    // 生成列表数据
            mToolPushListView.setAdapter(mToolAdapter);
            mToolPushListView.setOnItemClickListener(new OtaTestFragment.OnItemClickListener());

            //当前任务
            mToolTaskDetailView = (AbsListView) rootView.findViewById(R.id.taskdetails);
            mDetailToolContent = new MyContent();
            mDetailToolContent.addItem(new MyContent.DummyItem("任务详情", R.drawable.ic_record));
            mToolAdapter = new SimpleAdapter(getActivity(), mDetailToolContent.ITEMS, R.layout.tool_listview,
                    new String[]{"text", "img"},
                    new int[]{R.id.tool_text, R.id.tool_img});    // 生成列表数据
            mToolTaskDetailView.setAdapter(mToolAdapter);
            mToolTaskDetailView.setOnItemClickListener(new OtaTestFragment.OnItemClickListener());

            mToolTaskListView = (AbsListView) rootView.findViewById(R.id.tasklist);
            mTaskToolContent = new MyContent();
            mTaskToolContent.addItem(new MyContent.DummyItem("生效", R.drawable.ic_record));
            mTaskToolContent.addItem(new MyContent.DummyItem("失效", R.drawable.ic_record));
            mTaskToolContent.addItem(new MyContent.DummyItem("推送", R.drawable.ic_record));
            mToolAdapter = new SimpleAdapter(getActivity(), mTaskToolContent.ITEMS, R.layout.tool_listview,
                    new String[]{"text", "img"},
                    new int[]{R.id.tool_text, R.id.tool_img});    // 生成列表数据
            mToolTaskListView.setAdapter(mToolAdapter);
            mToolTaskListView.setOnItemClickListener(new OtaTestFragment.OnItemClickListener());

        }

        return rootView;
    }

    class OnItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(parent==mToolListView) {
                //sip在线
                m_push_type = "0";
                if(0 == position) {
                    m_push_url = iPublicConstants.OTA_SIP_DB_URL;
                    m_push_value = "0";
                    sip();
                }else if(1 == position) {
                    m_push_url = iPublicConstants.OTA_SIP_ONLINE_URL;
                    m_push_value = "1";
                    sip();
                }else if(2 == position) {
                    m_push_url = iPublicConstants.OTA_SIP_SUB_URL;
                    m_push_value = "2";
                    if(TextUtils.isEmpty(BaseData.getInstance(SuperTestApplication.getContext())
                            .readStringData(SettingPreferenceKey.MONKEY_PACKAGE))){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("还未选择业务，请选择后重试！").setNeutralButton("我知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }else {
                        sip();
                    }
                }
            }else if(parent==mToolPushListView) {
                //推送任务
                m_push_type = "1";
                if(0 == position) {
                    m_push_url = iPublicConstants.OTA_SIP_DB_URL;
                    m_push_value = "0";

                    Bundle bundle = new Bundle();
                    bundle.putString("object", mPushToolContent.ITEMS.get(position).get("text").toString());
                    Intent intent = new Intent(getActivity(), OtaPushActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    // 设置动画效果
                    getActivity().overridePendingTransition(R.anim.bottom_to_top, R.anim.top_to_bottom);

                }else if(1 == position) {
                    if(PushData.getAppId() == null){
                        ToastHelper.addToast("未初始化，请先到新增任务中新增推送", SuperTestApplication.getContext());
                        return;
                    }
                    m_push_url = iPublicConstants.OTA_SIP_ONLINE_URL;
                    m_push_value = "1";

                    Bundle bundle = new Bundle();
                    String m_data = mPushToolContent.ITEMS.get(position).get("text").toString();
                    bundle.putString("object", mPushToolContent.ITEMS.get(position).get("text").toString());
                    Intent intent = new Intent(getActivity(), OtaPushActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    // 设置动画效果
                    getActivity().overridePendingTransition(R.anim.bottom_to_top, R.anim.top_to_bottom);
                }
            }else if(parent==mToolTaskDetailView) {
                //当前任务-详情
                if(PushData.getTaskId() == null){
                    ToastHelper.addToast("请到历史任务中选中要查看的任务详情", SuperTestApplication.getContext());
                    return;
                }
                ToastHelper.addToast("当前任务-详情", SuperTestApplication.getContext());
            }else if(parent==mToolTaskListView) {
                //当前任务-执行
                m_push_type = "3";
                if(0 == position) {
                    m_push_url = iPublicConstants.OTA_TASK_ChangeStatus_URL;
                    m_push_value = "0";
                    sip();
                }else if(1 == position) {
                    m_push_url = iPublicConstants.OTA_TASK_ChangeStatus_URL;
                    m_push_value = "1";
                    sip();
                }else if(2 == position) {
                    ToastHelper.addToast("当前任务-推送", SuperTestApplication.getContext());
                }
            }
        }


        private void sip(){
            new SimpleTaskHelper(){
                @Override
                protected void doInBackground() {
                    try {
                        Map<String, String> loadingParams = new HashMap<String, String>();
                        if (m_push_type == "0"){
                            if (m_push_value == "0"){
                                String imei = PerformsData.getInstance(SuperTestApplication.getContext()).readStringData(iPerformsKey.imei);
                                String sn = ShellUtil.getProperty("ro.serialno");
                                loadingParams.put("operate", "Add");
                                loadingParams.put("imei", imei);
                                loadingParams.put("sn", sn);
                            }else if (m_push_value == "1"){
                                String imei = PerformsData.getInstance(SuperTestApplication.getContext()).readStringData(iPerformsKey.imei);
                                loadingParams.put("imei", imei);
                            }else if (m_push_value == "2"){
                                String imei = PerformsData.getInstance(SuperTestApplication.getContext()).readStringData(iPerformsKey.imei);
                                String package_name = BaseData.getInstance(getActivity().getApplicationContext()).readStringData(SettingPreferenceKey.MONKEY_PACKAGE);
                                loadingParams.put("imei", imei);
                                loadingParams.put("package", package_name);
                            }
                        }else if (m_push_type == "3"){
                            if (m_push_value == "0"){
                                loadingParams.put("m_status", "1");
                            }else if (m_push_value == "1"){
                                loadingParams.put("m_status", "2");
                            }
                        }
                        PostUploadHelper.getInstance().submitPostData(m_push_url, loadingParams, new PostCallBack() {
                            @Override
                            public void resultCallBack(boolean isSuccess, int resultCode, String data) {
                                if(isSuccess && null != data){
                                    mTestCaseData = new ArrayList<TestCaseData>();
                                    JSONObject jsonObject = JSON.parseObject(data);
                                    int status = jsonObject.getInteger("status");
                                    message = jsonObject.getString("message");
                                    if(0 == status) {
                                        handler.sendEmptyMessage(REQUEST_PASS);
                                    }else {
                                        handler.sendEmptyMessage(REQUEST_FAIL);
                                    }

                                }else{
                                    if(!isSuccess){
                                        handler.sendEmptyMessage(REQUEST_NO_NET);
                                    }else{
                                        handler.sendEmptyMessage(REQUEST_FAIL);
                                    }
                                }
                            }
                        });
                    } catch (IOException e) {
                        Logger.d("更新包==>" + e.toString());
                        handler.sendEmptyMessage(REQUEST_FAIL);
                        e.printStackTrace();
                    }
                }
            }.executeInSerial();

        }

        @SuppressLint("HandlerLeak")
        private Handler handler=new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case REQUEST_START:
                        break;
                    case REQUEST_PASS:
                        if (m_push_type == "0"){
                            if (m_push_value == "0"){
                                ToastHelper.addToast("入库成功", SuperTestApplication.getContext());
                            }else if (m_push_value == "1"){
                                ToastHelper.addToast("设备在线", SuperTestApplication.getContext());
                            }else if (m_push_value == "2"){
                                ToastHelper.addToast("订阅成功", SuperTestApplication.getContext());
                            }
                        }else if (m_push_type == "3"){
                            if (m_push_value == "0"){
                                ToastHelper.addToast("任务生效", SuperTestApplication.getContext());
                            }else if (m_push_value == "1"){
                                ToastHelper.addToast("任务失效", SuperTestApplication.getContext());
                            }
                        }

                        break;
                    case REQUEST_NO_NET:
                        mProgress.setVisibility(View.GONE);
                        fresh_notified.setVisibility(View.VISIBLE);
//                        txt_notified.setText("非公司网络！");
                        break;
                    case REQUEST_FAIL:
                        if (m_push_type == "0"){
                            if (m_push_value == "0"){
                                ToastHelper.addToast("入库失败", SuperTestApplication.getContext());
                            }else if (m_push_value == "1"){
                                ToastHelper.addToast("设备不在线", SuperTestApplication.getContext());
                            }else if (m_push_value == "2"){
                                ToastHelper.addToast("订阅失败", SuperTestApplication.getContext());
                            }
                        }else if (m_push_type == "3"){
                            if (m_push_value == "0"){
                                ToastHelper.addToast("任务异常", SuperTestApplication.getContext());
                            }else if (m_push_value == "1"){
                                ToastHelper.addToast("任务异常", SuperTestApplication.getContext());
                            }
                        }
                        break;
                    default:
                        ToastHelper.addToast("发送异常", SuperTestApplication.getContext());
                        break;
                }
            }
        };

    }
}
