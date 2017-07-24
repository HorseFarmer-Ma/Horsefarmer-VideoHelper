package com.meizu.testdevVideo.fragment;

import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.meizu.common.app.LoadingDialog;
import com.meizu.common.widget.LoadingView;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.activity.EmptyActivity;
import com.meizu.testdevVideo.activity.PostSearchActivity;
import com.meizu.testdevVideo.adapter.data.listview.PostSearchData;
import com.meizu.testdevVideo.constant.FragmentUtils;
import com.meizu.testdevVideo.constant.PushApkReturn;
import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.PostUploadHelper;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.PushData;
import com.meizu.testdevVideo.util.wifi.WifiUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtaPushFragment extends Fragment implements View.OnClickListener{
    private EditText edit_task_appname, edit_task_producttype, edit_android_version,
            edit_choose_app, app_description, edit_task_update_version;
    private LoadingView loading;
    private View rootView;
    private TextView push_apk_detail;
    private Button commit;
    private LoadingDialog dialog;
    private CheckBox is_must_update;

    private static final int UPDATE_VIEW = 1000;
    private static final int NO_NET = 1001;
    private static final int UPDATE_VIEW_FAIL = 1002;
    private static final int PUSH_TASK_ADD_SUCCESS = 1003;
    private static final int PUSH_TASK_ADD_FAIL = 1004;
    private static String host = "http://172.16.177.99/ota/uPush";
    private static final String PRODUCT_URL = host + "/bs/productType/getProductTree";
    private static final String APP_URL = host + "/bs/outApp/getEntityList";
    private static final String ANDROID_URL = host + "/bs/sysVersion/getEntityList";
    private static final String UPLOAD_APK_URL = host + "/customApp/upload";
    private static final String PUSH_OTA_TASK = host + "/customApp/insert";
//    private List<PostSearchData> productTypes;
//    private List<PostSearchData> sysVer;
    private PostSearchData appType;
    private Intent intent;

    private String appId;
    private String productTypes;
    private String sysVerIds;
    private Enum type;
    private Enum pushCode;

    private String formatPushReturn = "应用链接1：%s\n应用链接2：%s\n目标类型：%s\n应用版本：%s";

    // 上传文件后返回相关
    private PushApkReturn pushApkReturn;


    private enum ChooseType{
        APP, PRODUCT, ANDROID_VERSION, CHOOSE_APP_PACKAGE;
    }

    private enum ReturnCode{
        PUSH_FAIL, PUSH_PASS, NO_NET, PUSH_RUNNING, PUSH_NO_START
    }


    public OtaPushFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(null == rootView){
            rootView = inflater.inflate(R.layout.fragment_ota_test_project, container, false);
            findView(rootView);
            Ota_fragment_init();
        }

        return rootView;
    }

    /**
     * 控件初始化
     * @param view
     */
    private void findView(View view) {
        edit_task_appname = (EditText) view.findViewById(R.id.edit_task_appname);
        edit_task_producttype = (EditText) view.findViewById(R.id.edit_task_producttype);
        edit_android_version = (EditText) view.findViewById(R.id.edit_android_version);
        edit_choose_app = (EditText) view.findViewById(R.id.edit_choose_app);
        app_description = (EditText) view.findViewById(R.id.app_description);
        edit_task_update_version = (EditText) view.findViewById(R.id.edit_task_update_version);
        push_apk_detail = (TextView) view.findViewById(R.id.push_apk_detail);
        is_must_update = (CheckBox) view.findViewById(R.id.is_must_update);
        commit = (Button) view.findViewById(R.id.commit);
        loading = (LoadingView) view.findViewById(R.id.loading);
        dialog = new LoadingDialog(getActivity());
        dialog.setMessage("正在加载");
        dialog.setCancelable(false);
    }

    /**
     * 页面按钮监听初始化
     */
    private void Ota_fragment_init() {
        // 监听设置
        edit_task_appname.setOnClickListener(this);
        edit_task_producttype.setOnClickListener(this);
        edit_android_version.setOnClickListener(this);
        edit_choose_app.setOnClickListener(this);
        commit.setOnClickListener(this);
        pushCode = ReturnCode.PUSH_NO_START;
        appType = new PostSearchData();
        pushApkReturn = new PushApkReturn();
        intent = new Intent(getActivity(), PostSearchActivity.class);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.edit_task_appname:
                type = ChooseType.APP;
                PostSearchActivity.setDestActivity(getActivity());
                intent.putExtra("URL", APP_URL);
                intent.putExtra("IS_SINGLE_CHOOSE", true);
                startActivityForResult(intent, 0);
                break;
            case R.id.edit_task_producttype:
                type = ChooseType.PRODUCT;
                PostSearchActivity.setDestActivity(getActivity());
                intent.putExtra("URL", PRODUCT_URL);
                intent.putExtra("IS_SINGLE_CHOOSE", false);
                startActivityForResult(intent, 0);
                break;
            case R.id.edit_android_version:
                type = ChooseType.ANDROID_VERSION;
                PostSearchActivity.setDestActivity(getActivity());
                intent.putExtra("URL", ANDROID_URL);
                intent.putExtra("IS_SINGLE_CHOOSE", false);
                startActivityForResult(intent, 0);
                break;
            case R.id.edit_choose_app:
                type = ChooseType.CHOOSE_APP_PACKAGE;
                Intent intent = new Intent(getActivity(), EmptyActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(FragmentUtils.FRAGMENT_TYPE, FragmentUtils.CHOOSE_APP_FRAGMENT);
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
                break;
            case R.id.commit:
                if(TextUtils.isEmpty(edit_task_appname.getText()) || TextUtils.isEmpty(edit_task_producttype.getText())
                        || TextUtils.isEmpty(edit_android_version.getText()) || TextUtils.isEmpty(edit_choose_app.getText())
                        || TextUtils.isEmpty(app_description.getText())){
                    ToastHelper.addToast("没有填写所有必填项，拒绝提交", SuperTestApplication.getContext());
                }else{
                    if(pushCode == ReturnCode.PUSH_PASS){
                        dialog.show();
                        creatNewSimpleTask().executeInSerial();
                    }else if(pushCode == ReturnCode.PUSH_RUNNING){
                        ToastHelper.addToast("APK还没有提交成功，等待返回中，别着急！", SuperTestApplication.getContext());
                    }else if(pushCode == ReturnCode.PUSH_NO_START){
                        ToastHelper.addToast("都还没有选择APK，你就想点，开玩笑", SuperTestApplication.getContext());
                    }else if(pushCode == ReturnCode.NO_NET){
                        ToastHelper.addToast("无网络，提交APK失败，还想跑？", SuperTestApplication.getContext());
                    }else if(pushCode == ReturnCode.PUSH_FAIL){
                        ToastHelper.addToast("提交应用APK失败，重试一下再点击提交？", SuperTestApplication.getContext());
                    }else{
                        ToastHelper.addToast("UNKNOWN", SuperTestApplication.getContext());
                    }
                }

                break;
            default:
                break;
        }
    }


    // 提交任务

    private SimpleTaskHelper creatNewSimpleTask(){
        return new SimpleTaskHelper() {
            @Override
            protected void doInBackground() {
                if(WifiUtil.isWifiConnected(SuperTestApplication.getContext())){
                    try {
                        Map<String, String> taskParams = new HashMap<String, String>();
                        taskParams.put("packLink", pushApkReturn.getUrl());
                        taskParams.put("appId", appId);
                        taskParams.put("productTypes", productTypes);
                        taskParams.put("sysVerIds", sysVerIds);
                        taskParams.put("fileSize", pushApkReturn.getFileSize());
                        taskParams.put("versionCode", pushApkReturn.getVersionCode());
                        taskParams.put("checkArea", "0");
                        taskParams.put("verRange", edit_task_update_version.getText().toString());
                        taskParams.put("versionNum", pushApkReturn.getVersionName());
                        taskParams.put("digest", pushApkReturn.getDigest());
                        taskParams.put("digestType", pushApkReturn.getDigestType());  // #加密方式; 2全包、4头尾包 是
                        taskParams.put("mode", (is_must_update.isChecked() ? "0" : "1"));
                        taskParams.put("detail", app_description.getText().toString());
                        taskParams.put("appLink2", pushApkReturn.getUrl2());
                        Logger.d(JSON.toJSONString(taskParams));
                        PostUploadHelper.getInstance().submitPostData(PUSH_OTA_TASK, taskParams, new PostCallBack() {
                            @Override
                            public void resultCallBack(boolean isSuccess, int resultCode, String data) {
                                if(isSuccess && null != data){
                                    JSONObject json = JSON.parseObject(data);
                                    int status = json.getInteger("status");
                                    if(0 == status){
                                        Logger.d("新增推送成功:" + data);
                                        handler.sendEmptyMessage(PUSH_TASK_ADD_SUCCESS);
                                    }else{
                                        Logger.d("拉取列表失败" + data);
                                        handler.sendEmptyMessage(PUSH_TASK_ADD_FAIL);
                                    }
                                }else{
                                    Logger.d("接口失败" + data);
                                    handler.sendEmptyMessage(PUSH_TASK_ADD_FAIL);
                                }

                            }
                        });
                    } catch (IOException e) {
                        Logger.d("接口请求失败");
                        handler.sendEmptyMessage(PUSH_TASK_ADD_FAIL);
                        e.printStackTrace();
                    }

                }else{

                }
            }
        };
    }



    // 新增上传APK线程
    private SimpleTaskHelper creatNewTask(){
        return new SimpleTaskHelper() {
            @Override
            protected void doInBackground() {
                if(true){
//                if(WifiUtil.isWifiConnected(SuperTestApplication.getContext())){
                    try {
                        Map<String, String> loadingParams = new HashMap<String, String>();
                        loadingParams.put("user", "appadmin");
                        loadingParams.put("filePath", edit_choose_app.getText().toString());

                        PostUploadHelper.getInstance().submitPostData(UPLOAD_APK_URL, loadingParams, new PostCallBack() {
                            @Override
                            public void resultCallBack(boolean isSuccess, int resultCode, String data) {
                                if(isSuccess && null != data){
                                    JSONObject json = JSON.parseObject(data);
                                    int status = json.getInteger("status");
                                    if(0 == status){
                                        String message = json.getString("message");
                                        Logger.d(message);
                                        JSONObject dataJson = json.getJSONObject("data");
                                        pushApkReturn = new PushApkReturn();
                                        pushApkReturn.setUrl(dataJson.getString("url"));
                                        pushApkReturn.setDigestType(dataJson.getString("digestType"));
                                        pushApkReturn.setFileSize(dataJson.getString("fileSize"));
                                        pushApkReturn.setVersionName(dataJson.getString("versionName"));
                                        pushApkReturn.setUrl2(dataJson.getString("url2"));
                                        pushApkReturn.setVersionCode(dataJson.getString("versionCode"));
                                        pushApkReturn.setDigest(dataJson.getString("digest"));
                                        handler.sendEmptyMessage(UPDATE_VIEW);
                                    }else{
                                        Logger.d("拉取列表失败");
                                        handler.sendEmptyMessage(UPDATE_VIEW_FAIL);
                                    }
                                }else{
                                    Logger.d("接口失败");
                                    handler.sendEmptyMessage(UPDATE_VIEW_FAIL);
                                }

                            }
                        });
                    } catch (IOException e) {
                        Logger.d("接口请求失败");
                        handler.sendEmptyMessage(UPDATE_VIEW_FAIL);
                        e.printStackTrace();
                    }
                }else{
                    handler.sendEmptyMessage(NO_NET);
                }
            }
        };
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_VIEW:
                    pushCode = ReturnCode.PUSH_PASS;
                    loading.setVisibility(View.INVISIBLE);
                    push_apk_detail.setVisibility(View.VISIBLE);
                    push_apk_detail.setText(String.format(formatPushReturn, pushApkReturn.getUrl(),
                            pushApkReturn.getUrl2(), pushApkReturn.getDigestType(),
                            pushApkReturn.getVersionName()));
                    break;
                case UPDATE_VIEW_FAIL:
                    pushCode = ReturnCode.PUSH_FAIL;
                    loading.setVisibility(View.INVISIBLE);
                    push_apk_detail.setVisibility(View.VISIBLE);
                    push_apk_detail.setText(getString(R.string.ota_push_apk_fail));
                    break;
                case NO_NET:
                    pushCode = ReturnCode.NO_NET;
                    loading.setVisibility(View.INVISIBLE);
                    push_apk_detail.setVisibility(View.VISIBLE);
                    push_apk_detail.setText(getString(R.string.no_netword));
                    break;
                case PUSH_TASK_ADD_SUCCESS:
                    dialog.dismiss();
                    ToastHelper.addLongToast("推送任务添加成功", SuperTestApplication.getContext());
                    getActivity().finish();
                    break;
                case PUSH_TASK_ADD_FAIL:
                    dialog.dismiss();
                    ToastHelper.addLongToast("推送任务添加失败，请重试", SuperTestApplication.getContext());
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 取出字符串
        if (data != null){
            if(type == ChooseType.CHOOSE_APP_PACKAGE){
                pushCode = ReturnCode.PUSH_RUNNING;
                edit_choose_app.setText(data.getStringExtra("URL"));
                loading.setVisibility(View.VISIBLE);
                push_apk_detail.setText(getString(R.string.loading));
                creatNewTask().executeInSerial();
            }else{
                String text = "";
                Bundle bundle = data.getExtras();
                if(type == ChooseType.PRODUCT){
                    productTypes = "";
                }else if(type == ChooseType.ANDROID_VERSION){
                    sysVerIds = "";
                }
                JSONArray jsonArray = JSON.parseArray(bundle.getString("jsonData"));
                int size  = jsonArray.size();

                if(size > 0){
                    for (int i = 0; i < size; i++){
                        JSONObject jsonObject = JSON.parseObject(jsonArray.get(i).toString());
                        if(type == ChooseType.APP){
                            appId = jsonObject.getString("id");
                            PushData.setAppId(appId);
                            edit_task_appname.setText(jsonObject.getString("name"));
                        }else if(type == ChooseType.PRODUCT){
                            if(i < size - 1){
                                productTypes = productTypes + jsonObject.getString("id") + ",";
                            }else{
                                productTypes = productTypes + jsonObject.getString("id");
                            }

                            text = text + jsonObject.getString("name") + "  ";
                        }else if(type == ChooseType.ANDROID_VERSION){
                            if(i < size - 1){
                                sysVerIds = sysVerIds + jsonObject.getString("id") + ",";
                            }else{
                                sysVerIds = sysVerIds + jsonObject.getString("id");
                            }
                            text = text + jsonObject.getString("name") + "  ";
                        }
                    }

                    if(type == ChooseType.PRODUCT){
                        edit_task_producttype.setText(text);
                    }else if(type == ChooseType.ANDROID_VERSION){
                        edit_android_version.setText(text);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != handler){
            handler.removeCallbacksAndMessages(null);
        }
    }
}
