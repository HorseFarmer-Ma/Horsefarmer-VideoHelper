package com.meizu.testdevVideo;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.service.SuperTestService;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.shell.ShellUtils;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by HDL on 2016/9/8.
 */

public class SuperTestApplication extends Application {
    private static final String TAG = "SuperTestApplication";
    private static Context sApplicationContext = null;

//    private static final int MSG_SET_ALIAS = 1001;
//    private static final int MSG_SET_TAGS = 1002;

    public static Context getContext() {
        return sApplicationContext;
    }


    @Override
    public void onCreate() {
        super.onCreate();
//        LeakCanary.install(this);   // 內存泄漏检测工具
        long i = SystemClock.currentThreadTimeMillis();
        sApplicationContext = this;
        if(!PublicMethod.isServiceWorked(sApplicationContext, "com.meizu.testdevVideo.service.SuperTestService")){
            Intent mIntent = new Intent(sApplicationContext, SuperTestService.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(mIntent);   // 开始服务
        }

        new SimpleTaskHelper(){
            @Override
            protected void doInBackground() {
                TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);   // 获取手机号码信息
                JPushInterface.setDebugMode(false);     // 正式版的时候设置false，关闭调试
                JPushInterface.init(getApplicationContext());

                // 写进IMEI进行保存
                if(TextUtils.isEmpty(PerformsData.getInstance(sApplicationContext).readStringData(iPerformsKey.imei))){
                    Log.d(TAG, "获取IMEI");
                    CommonVariable.about_phone_imei = tm.getDeviceId();
                    PerformsData.getInstance(sApplicationContext).writeStringData(iPerformsKey.imei, CommonVariable.about_phone_imei);
                }

                if(TextUtils.isEmpty(PerformsData.getInstance(sApplicationContext).readStringData(iPerformsKey.deviceType))){
                    Log.d(TAG, "获取设备ID");
                    CommonVariable.about_phone_product_name = ShellUtils.execCommand("getprop ro.product.model", false, true)
                            .successMsg.replaceAll("\\s+", "");
                    PerformsData.getInstance(sApplicationContext).writeStringData(iPerformsKey.deviceType,
                            CommonVariable.about_phone_product_name);
                }
            }
        }.executeInSerial();

        Log.d(TAG, "花费初始化时间为：" + (SystemClock.currentThreadTimeMillis() - i));
    }



    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
//            Log.d(TAG, "OnTrimMemory level= " + level);
        }
    }
}
