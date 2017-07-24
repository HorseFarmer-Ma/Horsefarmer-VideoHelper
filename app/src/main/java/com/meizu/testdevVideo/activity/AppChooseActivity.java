package com.meizu.testdevVideo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;

import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.meizu.common.widget.LoadingAnimotionView;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.adapter.AppListAdapter;
import com.meizu.testdevVideo.adapter.data.listview.AppInfo;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.library.AppInfoHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.push.android.MPush;
import com.meizu.testdevVideo.push.android.bean.MPushBindData;
import com.meizu.testdevVideo.push.android.bean.MPushBindUserBean;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.query.QueryApp;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.mpush.api.Constants;

import java.util.List;

public class AppChooseActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
                                            AppListAdapter.OnCheckListener, View.OnClickListener{

    public static final String TITLE = "title";
    public static final int UPDATE_APP_LIST = 100;
    public static final int UPDATE_LOGIN_MESSAGE = 200;
    public static final int UPDATE_LOGIN_FAIL_TOAST = 300;
    public static final int UPDATE_LOGIN_TIME_OUT = 400;
    public static final int TIME_OUT = 3000;


    private SearchView searchView;
    private ListView appListView;
    private LoadingAnimotionView loadingView;
    private LinearLayout mProgress;
    private LinearLayout view_height;
    private LinearLayout button_tab;
    private Button btn_cancel;
    private Button btn_ok;
    private TextView choose_item_remind;
    private EditText email_adress;
    private QueryApp queryApp;
    private List<AppInfo> mlistAppInfo = null;
    private AppListAdapter mAppAdapter = null;
    private String title = null;
    private AppListAdapter.Choose task = null;
    private Thread loginThread = null;
    private String appType;
    private String packageName;
    private String imei;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_app_list);
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString(TITLE);
        setTitle(title);
        findView();
        mAppListThread.start();      // 启动线程
    }


    private void findView(){
        view_height = (LinearLayout) findViewById(R.id.view_height);
        ViewGroup.LayoutParams params = view_height.getLayoutParams();
        params.height = PublicMethod.dp2Px(getApplicationContext(), 450);
        view_height.setLayoutParams(params);
        appListView = (ListView) findViewById(R.id.listviewApp);
        searchView = (SearchView) findViewById(R.id.editTextApp);
        mProgress = (LinearLayout) findViewById(R.id.mProgress);
        button_tab = (LinearLayout) findViewById(R.id.button_tab);
        email_adress = (EditText) findViewById(R.id.email_adress);
        loadingView = (LoadingAnimotionView) findViewById(R.id.loadingView);

        if(title.equals(getResources().getString(R.string.choose_app_type))){
            task = AppListAdapter.Choose.APP_CHOOSE;
            String email = BaseData.getInstance(getApplicationContext()).readStringData(SettingPreferenceKey.EMAIL_ADDRESS);
            email_adress.setVisibility(View.VISIBLE);
            if(!TextUtils.isEmpty(email)){
                email_adress.setText(email);
            }else{
                email_adress.setText("@meizu.com");
            }
        }else if(title.equals(getResources().getString(R.string.choose_monkey_app))){
            button_tab.setVisibility(View.VISIBLE);
            btn_cancel = (Button) findViewById(R.id.btn_cancel);
            btn_ok = (Button) findViewById(R.id.btn_ok);
            if(null != btn_ok){
                btn_ok.setEnabled(false);
            }

            choose_item_remind = (TextView) findViewById(R.id.choose_item_remind);
            choose_item_remind.setText(getResources().getString(R.string.choose_item_remind).replace("%d", "0"));
            btn_cancel.setOnClickListener(this);
            btn_ok.setOnClickListener(this);
            task = AppListAdapter.Choose.MONKEY_APP_CHOOSE;
        }
    }



    private Thread mAppListThread = new Thread(){
        public void run(){
            queryApp = new QueryApp(getApplicationContext());
            mlistAppInfo = queryApp.getListAppInfo(QueryApp.Query.FILTER_ALL_APP);
            if(null != handler){
                handler.sendEmptyMessage(UPDATE_APP_LIST);
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler= new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_APP_LIST:
                    try {
                        mProgress.setVisibility(View.GONE);
                        // 调用AppListAdapter，生成列表信息
                        mAppAdapter = new AppListAdapter(getApplicationContext(), mlistAppInfo, task);
                        appListView.setAdapter(mAppAdapter);
                        // 创建监听器实现类对象
                        appListView.setOnItemClickListener(AppChooseActivity.this);
                        mAppAdapter.setOnCheckListener(AppChooseActivity.this);
                    }catch (NullPointerException e){
                        System.out.print(e.toString());
                    }

                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
                        @Override
                        public boolean onQueryTextChange(String query) {
                            mlistAppInfo = queryApp.searchListAppInfo(query);
                            mAppAdapter.notifyDataSetChanged();
                            return false;
                        }

                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }});
                    searchView.setSubmitButtonEnabled(false);

                    break;

                case UPDATE_LOGIN_MESSAGE:
                    MPushBindData.setBindStatus(getApplicationContext(),
                            com.meizu.testdevVideo.constant.Constants.MpushBindUser.NO_STATUS);
                    loadingView.setVisibility(View.GONE);
                    appListView.setEnabled(true);
                    Intent intent = new Intent(AppChooseActivity.this, SuperTestActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(SettingPreferenceKey.APP_TYPE, appType);
                    bundle.putString(SettingPreferenceKey.MONKEY_PACKAGE, packageName);
                    bundle.putString(SettingPreferenceKey.EMAIL_ADDRESS, email);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;

                case UPDATE_LOGIN_FAIL_TOAST:
                    MPushBindData.setBindStatus(getApplicationContext(),
                            com.meizu.testdevVideo.constant.Constants.MpushBindUser.NO_STATUS);
                    loadingView.setVisibility(View.GONE);
                    appListView.setEnabled(true);
                    ToastHelper.addToast("邮箱信息有误\n请根据钉钉邮箱信息进行填写！", getApplicationContext());
                    break;

                case UPDATE_LOGIN_TIME_OUT:
                    MPushBindData.setBindStatus(getApplicationContext(),
                            com.meizu.testdevVideo.constant.Constants.MpushBindUser.NO_STATUS);
                    loadingView.setVisibility(View.GONE);
                    appListView.setEnabled(true);
                    ToastHelper.addToast("请求超时，请检查是否为公司网络！", getApplicationContext());
                    break;
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if(PublicMethod.hasNetwork(getApplicationContext())){
            if(TextUtils.isEmpty(email_adress.getText().toString())){
                ToastHelper.addToast("请先填写个人接收通知邮箱", getApplicationContext());
            }else if(!email_adress.getText().toString().contains("@")){
                ToastHelper.addToast("请填写正确格式邮箱，需带@后缀", getApplicationContext());
            }else{
                loadingView.setVisibility(View.VISIBLE);
                appListView.setEnabled(false);
                appType = mlistAppInfo.get(position).getAppLabel();
                imei = PerformsData.getInstance(getApplicationContext()).readStringData(iPerformsKey.imei);
                email = email_adress.getText().toString();
                packageName = mlistAppInfo.get(position).getPkgName();
                MPushBindUserBean mPushBindUserBean = new MPushBindUserBean();
                mPushBindUserBean.setImei(imei);
                mPushBindUserBean.setEmail(email);
                mPushBindUserBean.setPackageName(packageName);
                mPushBindUserBean.setVersion(AppInfoHelper.getInstance().getAppVersion(getPackageName()));
                mPushBindUserBean.setModule(PerformsData.getInstance(getApplicationContext()).readStringData(iPerformsKey.deviceType));
                mPushBindUserBean.setTask(com.meizu.testdevVideo.constant.Constants.MpushTaskLabel.CHECK_PHONE_IMEI);
                Logger.d(JSON.toJSONString(mPushBindUserBean));
                MPush.I.sendPush(JSON.toJSONString(mPushBindUserBean).getBytes(Constants.UTF_8));

                loginThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long startTime = System.currentTimeMillis();
                        long checkTime;
                        while ((checkTime = System.currentTimeMillis() - startTime) < TIME_OUT){
                            if(MPushBindData.getBindStatus(SuperTestApplication.getContext())
                                    == com.meizu.testdevVideo.constant.Constants.MpushBindUser.PASS){
                                handler.sendEmptyMessage(UPDATE_LOGIN_MESSAGE);
                                break;

                            }else if(MPushBindData.getBindStatus(SuperTestApplication.getContext())
                                    == com.meizu.testdevVideo.constant.Constants.MpushBindUser.FAIL){
                                handler.sendEmptyMessage(UPDATE_LOGIN_FAIL_TOAST);
                                break;
                            }

                            // 每隔100ms检测一次
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if(checkTime >= TIME_OUT){
                            handler.sendEmptyMessage(UPDATE_LOGIN_TIME_OUT);
                        }
                    }
                });
                loginThread.start();
            }
        }else{
            ToastHelper.addToast("无网络连接，点击mBack返回\n请连接网络后进行登录绑定", getApplicationContext());
        }

    }


    @Override
    public void onCheck(int position, boolean isChecked) {
        choose_item_remind.setText(getResources()
                .getString(R.string.choose_item_remind)
                .replace("%d", String.valueOf(mAppAdapter.getCheckBoxMapNumber())));
        if(mAppAdapter.getCheckBoxMapNumber() > 0){
            if(null != btn_ok){
                btn_ok.setEnabled(true);
            }
        }else{
            if(null != btn_ok){
                btn_ok.setEnabled(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_ok:
                Intent intent = new Intent(AppChooseActivity.this, SuperTestActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(SettingPreferenceKey.MONKEY_CHOOSE_APP,
                        mAppAdapter.getCheckBoxMap().values().toString().replace(",", " -p").replace("[", "").replace("]", ""));
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:break;
        }
    }

    @Override
    public void onDestroy() {
        // 移除多余信息
        if(null != loginThread){
            loginThread.interrupt();
        }
        if(null != handler){
            handler.removeCallbacksAndMessages(null);
        }

        super.onDestroy();
    }
}
