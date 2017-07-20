package com.meizu.testdevVideo.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.adapter.data.listview.TestCaseData;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.PerformsAdapter;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.db.util.U2TaskDBUtil;

import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.PostUploadHelper;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.sharepreference.BaseData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flyme.support.v7.app.ActionBar;
import flyme.support.v7.app.AppCompatActivity;


public class PerformsActivity extends AppCompatActivity {
    private PerformsAdapter mPerformsAdapter;   // 适配器
    private ListView lv_Performs;  // 性能测试列表
    private LinearLayout mProgress;
    private LinearLayout fresh_notified;
    private TextView txt_notified;
    private List<TestCaseData> mTestCaseData;
    private String localTaskId = "0";
    private int performsType;
    private String packageName;     // 应用包名
    private String m_class_st;      // 类名
    private String m_package_st;    // 测试类型
    private String message;

    private static final int REQUEST_START = 100;
    private static final int REQUEST_PASS = 200;
    private static final int REQUEST_FAIL = 500;
    private static final int REQUEST_NO_NET = 404;
    private static final int UPDATE_CASE_LIST = 300;
    private static final int CHOOSE_ALL_CASE = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performs);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   // 设置竖屏
        initView();
        getData();
    }

    private void initView(){
        Bundle mBundle =  getIntent().getExtras();
        String project = mBundle.getString("Project");
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setTitle(project);
            mActionBar.setDisplayShowControlTitleBar(true, new ActionBar.ControlTitleBarCallback(){

                @Override
                public void onCreateControlButton(int witchButton, ActionBar.ControlButton button) {
                    switch (witchButton) {
                        case 1: // 设置右边按钮
                            button.setId(CHOOSE_ALL_CASE);
                            button.setTitle("全部添加");
                            break;
                    }
                }
            });
        }
        packageName = BaseData.getInstance(getApplicationContext()).readStringData(SettingPreferenceKey.MONKEY_PACKAGE);
        m_class_st = captureName(packageName.split("\\.")[packageName.split("\\.").length - 1]);
        lv_Performs = (ListView) findViewById(R.id.lv_performs);
        mProgress = (LinearLayout) findViewById(R.id.progress_performs);
        fresh_notified = (LinearLayout) findViewById(R.id.fresh_notified);
        txt_notified = (TextView) findViewById(R.id.txt_notified);
        fresh_notified.setVisibility(View.GONE);
        if(null != project && project.equals("内存测试")){
            m_package_st = "memory";
            performsType = 3;
        }else if(null != project && project.equals("帧率测试")){
            m_package_st = "framerate";
            performsType = 2;
        }else if(null != project && project.equals("纯净后台")){
            m_package_st = "purebackstage";
            performsType = 4;
        }else if(null != project && project.equals("启动时间")){
            m_package_st = "starttime";
            performsType = 1;
        }else{
            m_package_st = "";
            performsType = 0;
        }
    }


    /**
     * 初始化数据线程
     */
    private void getData(){
        new SimpleTaskHelper(){
            @Override
            protected void doInBackground() {
                handler.sendEmptyMessage(REQUEST_START);
                Map<String, String> getListDataParams = new HashMap<String, String>();
                getListDataParams.put("m_package_st", m_package_st);
                getListDataParams.put("m_class_st", m_class_st);
                try {
                    PostUploadHelper.getInstance().submitPostData(iPublicConstants.PPERFORMS_PULL_TESTCASE_URL, getListDataParams, new PostCallBack() {
                        @Override
                        public void resultCallBack(boolean isSuccess, int resultCode, String data) {
                            if(isSuccess && null != data){
                                mTestCaseData = new ArrayList<TestCaseData>();
                                JSONObject jsonObject = JSON.parseObject(data);
                                int status = jsonObject.getInteger("status");
                                message = jsonObject.getString("message");
                                if(0 == status){
                                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                                    for(int i = 0; i < jsonArray.size(); i++){
                                        JSONObject jsonData = jsonArray.getJSONObject(i);
                                        TestCaseData mCaseData = new TestCaseData();
                                        mCaseData.setCaseName(jsonData.getString("m_name"));
                                        mCaseData.setTestDescrition(jsonData.getString("m_step_name"));
                                        mCaseData.setChoose(U2TaskDBUtil.getInstance().isCaseNameExits("0", jsonData.getString("m_name")));
                                        mTestCaseData.add(mCaseData);
                                    }
                                    handler.sendEmptyMessage(REQUEST_PASS);
                                }else{
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
                    handler.sendEmptyMessage(REQUEST_NO_NET);
                }
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                return true;
            case CHOOSE_ALL_CASE:
                if(mTestCaseData != null){
                    handler.sendEmptyMessage(UPDATE_CASE_LIST);
                }else{
                    ToastHelper.addToast("还没刷出列表，别着急！", SuperTestApplication.getContext());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 创建属于主线程的handler
    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case REQUEST_START:
                    mProgress.setVisibility(View.VISIBLE);
                    fresh_notified.setVisibility(View.GONE);
                    break;
                case UPDATE_CASE_LIST:
                    if(m_package_st.equals("purebackstage") && mTestCaseData.size() > 1) {
                        ToastHelper.addToast("最多只能添加一个纯净后台任务", SuperTestApplication.getContext());
                    }else if(!m_package_st.equals("purebackstage") && U2TaskDBUtil.getInstance().isExistPureCase("0")){
                        ToastHelper.addToast("欲全选，请移除任务篮子中最后一个纯净后台任务", SuperTestApplication.getContext());
                    }else{
                        for(int i = 0; i < mTestCaseData.size(); i++){
                            if(!mTestCaseData.get(i).isChoose()){
                                mTestCaseData.get(i).setChoose(true);
                                U2TaskDBUtil.getInstance().saveData(localTaskId, performsType,
                                        mTestCaseData.get(i).getTestDescrition(),
                                        mTestCaseData.get(i).getCaseName(),
                                        Constants.U2TaskConstants.STATUS_TYPE_NO_STATU, true, "", "");
                            }
                        }
                        mPerformsAdapter.notifyDataSetChanged();
                    }

                    break;
                case REQUEST_PASS:
                    fresh_notified.setVisibility(View.GONE);
                    mProgress.setVisibility(View.GONE);   // 隐藏我的加载圈
                    mPerformsAdapter = new PerformsAdapter(mTestCaseData, PerformsActivity.this);
                    lv_Performs.setAdapter(mPerformsAdapter);
                    // 点击列表监听事件
                    lv_Performs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if(mTestCaseData.get(position).isChoose()){
                                ToastHelper.addToast("已经选中，点我也没用\n移除请到任务篮子", SuperTestApplication.getContext());
                            }else{
                                if(U2TaskDBUtil.getInstance().isExistPureCase("0") && m_package_st.equals("purebackstage")){
                                    ToastHelper.addToast("最多只能有一个纯净后台任务", SuperTestApplication.getContext());
                                }else if(U2TaskDBUtil.getInstance().isExistPureCase("0") && !m_package_st.equals("purebackstage")){
                                    ToastHelper.addToast("欲添加案例，请将任务篮子最后一个纯" +
                                            "净后台任务移除，因为纯净后台只能最后执行", SuperTestApplication.getContext());
                                }else{
                                    U2TaskDBUtil.getInstance().saveData(localTaskId, performsType,
                                            mTestCaseData.get(position).getTestDescrition(),
                                            mTestCaseData.get(position).getCaseName(),
                                            Constants.U2TaskConstants.STATUS_TYPE_NO_STATU, true, "", "");
                                    mTestCaseData.get(position).setChoose(true);
                                    mPerformsAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
                    break;
                case REQUEST_NO_NET:
                    mProgress.setVisibility(View.GONE);
                    fresh_notified.setVisibility(View.VISIBLE);
                    txt_notified.setText("非公司网络！");
                    break;
                case REQUEST_FAIL:
                    mProgress.setVisibility(View.GONE);
                    fresh_notified.setVisibility(View.VISIBLE);
                    txt_notified.setText(message);
                    break;
                default:
                    break;
            }
        }
    };


    public static String captureName(String name) {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != handler){
            handler.removeCallbacksAndMessages(null);
        }
    }
}
