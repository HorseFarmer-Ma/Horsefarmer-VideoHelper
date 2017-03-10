package com.meizu.testdevVideo.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.meizu.testdevVideo.adapter.data.listview.TestCaseData;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.adapter.PerformsAdapter;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.GetFinalHttpHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.service.PerformsTestService;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;

import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PerformsActivity extends AppCompatActivity {
    private String TAG  = PerformsActivity.class.getSimpleName();
    private String url;    // 获取列表网址
    private String project;    // 测试项目
    private PerformsAdapter mPerformsAdapter;   // 适配器
    private ListView lv_Performs;  // 性能测试列表
    private LinearLayout mProgress;
    private Intent mIntent;     // 后台服务类
    private static int mPosition;     // 点击位置
    private LinearLayout fresh_notified;
    private TextView txt_notified;
    private ImageView img_fresh;
    private List<TestCaseData> mTestCaseData = new ArrayList<TestCaseData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performs);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);   // 设置竖屏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle mBundle =  getIntent().getExtras();
        project = mBundle.getString("Project");
        setTitle(project);

        lv_Performs = (ListView) findViewById(R.id.lv_performs);
        mProgress = (LinearLayout) findViewById(R.id.progress_performs);
        fresh_notified = (LinearLayout) findViewById(R.id.fresh_notified);
        txt_notified = (TextView) findViewById(R.id.txt_notified);
        img_fresh = (ImageView) findViewById(R.id.img_fresh);

        fresh_notified.setVisibility(View.GONE);
        img_fresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });

        if(project.equals("内存测试")){
            url = iPublicConstants.PPERFORMS_PULL_TESTCASE_URL + "memory.do";
        }else if(project.equals("帧率测试")){
            url = iPublicConstants.PPERFORMS_PULL_TESTCASE_URL + "framerate.do";
        }else if(project.equals("纯净后台")){
            url = iPublicConstants.PPERFORMS_PULL_TESTCASE_URL + "purebackstage.do";
        }else if(project.equals("启动时间")){
            url = iPublicConstants.PPERFORMS_PULL_TESTCASE_URL + "starttime.do";
        }
        getData();
    }


    /**
     * 初始化数据线程
     */
    private void getData(){
        GetFinalHttpHelper.getInstance().get(url, new AjaxCallBack<String>() {
            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                handler.sendEmptyMessage(404);
            }

            @Override
            public void onStart() {
                super.onStart();
                handler.sendEmptyMessage(100);
            }

            @Override
            public void onSuccess(String s) {
                super.onSuccess(s);
                // 根据服务器返回的json数据，判断上传是否成功
                if(!TextUtils.isEmpty(s)){
                    try {
                        JSONObject obj = new JSONObject(s);
                        Log.e(TAG, obj.toString());
                        if(obj.optInt("code") == 200){
                            mTestCaseData = new ArrayList<TestCaseData>();
                            JSONArray data = obj.optJSONArray("data");
                            String testType = obj.optString("title");
                            for(int i = 0; i < data.length(); i++){
                                JSONObject dataSon = (JSONObject) data.get(i);
                                TestCaseData mCaseData = new TestCaseData();
                                mCaseData.setTestAppType(dataSon.getString("appType"));
                                mCaseData.setTestDescrition(dataSon
                                        .getString("testDescription").equals("null")? "配置人很懒，暂无描述" : dataSon.getString("testDescription"));
                                mCaseData.setCaseName(dataSon.getString("testPackageName"));
                                mCaseData.setTestAppVersion(dataSon.getString("destAppVersion"));
                                mCaseData.setTestType(testType);
                                mTestCaseData.add(mCaseData);
                                handler.sendEmptyMessage(200);
                            }
                        }else{
                            handler.sendEmptyMessage(500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "出现未知解析错误");
                    }
                }
            }

            @Override
            public void onLoading(long count, long current) {
                super.onLoading(count, current);
            }
        });
    }


    // 创建属于主线程的handler
    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 100:
                    mProgress.setVisibility(View.VISIBLE);
                    fresh_notified.setVisibility(View.GONE);
                    break;
                case 200:
                    fresh_notified.setVisibility(View.GONE);
                    mProgress.setVisibility(View.GONE);   // 隐藏我的加载圈
                    mPerformsAdapter = new PerformsAdapter(mTestCaseData, PerformsActivity.this);
                    lv_Performs.setAdapter(mPerformsAdapter);

                    // 点击列表监听事件
                    lv_Performs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if(!CommonVariable.isPerformsStart){
                                testChooseDialog(position);
                            }else{
                                ToastHelper.addToast("存在任务，无法点击，请退出中止任务后进行", PerformsActivity.this);
                            }
                        }
                    });
                    break;
                case 404:
                    mProgress.setVisibility(View.GONE);
                    fresh_notified.setVisibility(View.VISIBLE);
                    txt_notified.setText("找不到服务器，刷新看看\n或当前WiFi不是MZ-Inweb-Test");
                    break;
                case 500:
                    mProgress.setVisibility(View.GONE);
                    fresh_notified.setVisibility(View.VISIBLE);
                    txt_notified.setText("服务器返回失败，刷新看看");
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 弹窗显示是否执行案例
     */
    private void testChooseDialog(int position){
        mPosition = position;
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(PerformsActivity.this);
        mBuilder.setTitle(project);
        mBuilder.setMessage("确定执行本案例：" + mTestCaseData.get(position).getCaseName() +
                "\n点击确定后，请等待执行!");
        mBuilder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ToastHelper.addToast("初始化: 写入执行数据", getApplicationContext());
                PublicMethod.saveStringToFileWithoutDeleteSrcFile(PublicMethod.getSystemTime() + "初始化: 写入执行数据\n",
                        "Performs_Log", iPublicConstants.LOCAL_MEMORY + "SuperTest/ApkLog/");
                initTestData(mPosition);
                ToastHelper.addToast("初始化: 清空对应目录下数据", getApplicationContext());
                PublicMethod.saveStringToFileWithoutDeleteSrcFile(PublicMethod.getSystemTime() + "初始化: 清空对应目录下数据\n",
                        "Performs_Log", iPublicConstants.LOCAL_MEMORY + "SuperTest/ApkLog/");
                clearTestData(project);
                ToastHelper.addToast("初始化: 开始执行", getApplicationContext());
                PublicMethod.saveStringToFileWithoutDeleteSrcFile(PublicMethod.getSystemTime() + "初始化: 开始执行\n",
                        "Performs_Log", iPublicConstants.LOCAL_MEMORY + "SuperTest/ApkLog/");
                mIntent = new Intent(PerformsActivity.this, PerformsTestService.class);
                mIntent.putExtra("taskType", 0);
                stopService(mIntent);   // 停止服务
                startService(mIntent);   // 开始服务
            }
        });

        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mBuilder.create().show();
    }

    /**
     * 清除测试数据
     */
    private void clearTestData(String testType){
        if(testType.equals("帧率测试")){
            PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_FPS_RESULT);
        }else if(testType.equals("内存测试")){
            PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_MEMORY_RESULT);
        }else if(testType.equals("启动时间")){
            PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_TIME_RESULT);
        }else if(testType.equals("纯净后台")){
            PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_PURE_BACKGROUND_RESULT);
        }
        PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_TESTCASE_PATH);   // 清除jar包
        PublicMethod.deleteDirectory(iPublicConstants.PERFORMS_LOG);   // 清除打印的Log
    }

    /**
     * 初始化测试数据
     */
    private void initTestData(int position){
        PerformsData.getInstance(PerformsActivity.this).writeStringData(iPerformsKey.testTime,
                String.valueOf(System.currentTimeMillis()));
        PerformsData.getInstance(PerformsActivity.this).writeStringData(iPerformsKey.testType,
                mTestCaseData.get(position).getTestType());
        PerformsData.getInstance(PerformsActivity.this).writeStringData(iPerformsKey.appType,
                mTestCaseData.get(position).getTestAppType());
        PerformsData.getInstance(PerformsActivity.this).writeStringData(iPerformsKey.appVersion,
                PublicMethod.getAppVersion(this, mTestCaseData.get(position).getTestAppType()));
        PerformsData.getInstance(PerformsActivity.this).writeStringData(iPerformsKey.doPackageName,
                mTestCaseData.get(position).getCaseName());
    }




//    /**
//     * 获取文件夹下文件
//     * @return
//     */
//    public List<TestCaseData> getList() {
//        List<TestCaseData> LvTestCaseData = new ArrayList<TestCaseData>();
//        File SDFile = new File(scan_path);
//        File sdPath = new File(SDFile.getAbsolutePath());
//        if(sdPath.listFiles().length > 0) {
//            for(File file : sdPath.listFiles()) {
//                TestCaseData mCaseData = new TestCaseData();
//                mCaseData.setCaseName(file.getName());
//                LvTestCaseData.add(mCaseData);
//                Log.e(PerformsActivity.class.getSimpleName(), file.getName());
//            }
//        }
//        return LvTestCaseData;
//    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
