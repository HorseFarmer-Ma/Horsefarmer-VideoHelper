package com.meizu.testdevVideo.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;

import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.PostUploadHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;


/**
 * 注册APK
 */
public class RegisterAppService extends IntentService {

    private final String TAG = RegisterAppService.class.getSimpleName();
    private final String ALIAS_NAME = "alias";
    private final String TAG_NAME = "tag";
    private final String RESGISTER_ID = "resgisterId";
    private String registerId;
    private Map<String, String> params = null;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RegisterAppService() {
        super("register");
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        register();
    }

    /**
     * 注册函数
     */
    private void register(){
        Log.e(TAG, "欲设置的Tag为" + PerformsData.getInstance(RegisterAppService.this).readStringData(iPerformsKey.deviceType));
        Log.e(TAG, "欲设置的别名为" + PerformsData.getInstance(RegisterAppService.this).readStringData(iPerformsKey.imei));
        if(!PerformsData.getInstance(RegisterAppService.this).readBooleanData(iPerformsKey.isRegister)) {
            if (PublicMethod.isConnected(RegisterAppService.this)) {
                if (!TextUtils.isEmpty(registerId = JPushInterface.getRegistrationID(getApplicationContext()))) {
                    Log.e(TAG, "尝试注册");
                    Log.e(TAG, "欲设置的RegistrationID为" + JPushInterface.getRegistrationID(getApplicationContext()));
                    if(params == null){
                        params = new HashMap<String, String>();
                        params.put(TAG_NAME, PerformsData.getInstance(RegisterAppService.this).readStringData(iPerformsKey.deviceType));
                        params.put(ALIAS_NAME, PerformsData.getInstance(RegisterAppService.this).readStringData(iPerformsKey.imei));
                        params.put(RESGISTER_ID, registerId);
                    }
                    try {
                        PostUploadHelper.getInstance().submitPostData(iPublicConstants.PERFORMS_POST_ID_TAG_ALIAS_URL, params, new PostCallBack() {
                            @Override
                            public void resultCallBack(boolean isSuccess, int resultCode, String data) {
                                Log.e("POST结果", "isSuccess：" + isSuccess);
                                Log.e("POST结果", "resultCode：" + resultCode);
                                Log.e("POST结果", "result：" + data);
                                if(null != data && data.equals("200")){
                                    handler.sendEmptyMessage(100);
                                    PerformsData.getInstance(RegisterAppService.this).writeBooleanData(iPerformsKey.isRegister, true);
                                }
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                stopSelf();
                            }
                        });
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(7 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    register();
                }

                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                PublicMethod.lockWifi(PreferenceManager.getDefaultSharedPreferences(RegisterAppService.this), RegisterAppService.this);
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 100:
                    ToastHelper.addToast("注册成功", RegisterAppService.this);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "注册完成，退出注册服务");
    }
}
