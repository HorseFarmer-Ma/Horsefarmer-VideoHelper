package com.meizu.testdevVideo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.register.RegisterPost;
import com.meizu.testdevVideo.util.register.RegisterResultCallBack;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;

import net.tsz.afinal.http.AjaxParams;

import cn.jpush.android.api.JPushInterface;


/**
 * 注册APK
 */
public class RegisterAppService extends Service {

    private final String TAG = RegisterAppService.class.getSimpleName();
    private final String ALIAS_NAME = "alias";
    private final String TAG_NAME = "tag";
    private final String RESGISTER_ID = "resgisterId";
    private AjaxParams params = null;
    private String registerId;
    private int registerTryNumber;   // 尝试注册次数

    public RegisterAppService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        register();     // 首次进来注册
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerTryNumber = 0;   // 尝试次数初始化
        RegisterPost.getInstance().setRegisterResultCallBack(new RegisterResultCallBack() {
            @Override
            public void isSendSuccess(boolean isSuccess, boolean isCompleteFlag) {
                Log.e(TAG, "注册结果" + isSuccess);
                if(isSuccess){
                    PerformsData.getInstance(RegisterAppService.this).writeBooleanData(iPerformsKey.isRegister, true);
                    Log.e(TAG, "注册成功，不再尝试注册");
                }else{
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    register();     // 注册不成功，再次尝试注册
                }
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    private void register(){
        Log.e(TAG, "欲设置的Tag为" + PerformsData.getInstance(this).readStringData(iPerformsKey.deviceType));
        Log.e(TAG, "欲设置的别名为" + PerformsData.getInstance(this).readStringData(iPerformsKey.imei));
        if(!PerformsData.getInstance(this).readBooleanData(iPerformsKey.isRegister)){
            if(PublicMethod.isConnected(this)){
                if(!TextUtils.isEmpty(registerId = JPushInterface.getRegistrationID(getApplicationContext()))){
                    Log.e(TAG, "尝试注册");
                    Log.e(TAG, "欲设置的RegistrationID为" + JPushInterface.getRegistrationID(getApplicationContext()));
                    if(registerTryNumber > 15){
                        stopSelf();   // 尝试注册次数大于15次，停止服务
                    }else {
                        registerTryNumber ++;
                    }
                    if(params == null){
                        params = new AjaxParams();
                        params.put(ALIAS_NAME, PerformsData.getInstance(this).readStringData(iPerformsKey.imei));
                        params.put(TAG_NAME, PerformsData.getInstance(this).readStringData(iPerformsKey.deviceType));
                        params.put(RESGISTER_ID, registerId);
                    }

                    RegisterPost.getInstance().registerPost(this, params);

                    try {
                        Thread.sleep(7 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
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
            }else{
                PublicMethod.lockWifi(PreferenceManager.getDefaultSharedPreferences(this), this);
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                register();
            }
        }
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
