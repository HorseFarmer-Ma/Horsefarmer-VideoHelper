package com.meizu.testdevVideo;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.library.AlarmSetting;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.push.android.MPushReceiver;
import com.meizu.testdevVideo.push.android.MPushStart;
import com.meizu.testdevVideo.service.SuperTestService;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.shell.ShellUtil;
import com.meizu.testdevVideo.util.shell.ShellUtils;

import java.util.LinkedHashSet;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * 全局Application
 * Created by HDL on 2016/9/8.
 */

public class SuperTestApplication extends Application {
    private static final String TAG = "SuperTestApplication";
    private static Context sApplicationContext = null;
    private static final int MSG_SET_ALIAS = 1001;
    private static final int MSG_SET_TAGS = 1002;
    private int aliasTryTime = 3;
    private int tagsTryTime = 3;

    public static Context getContext() {
        return sApplicationContext;
    }


    @Override
    public void onCreate() {
        super.onCreate();
//        LeakCanary.install(this);   // 內存泄漏检测工具
        String processName = PublicMethod.getProcessName(this, android.os.Process.myPid());
        if (processName != null) {
            boolean defaultProcess = processName.equals("com.meizu.testdevVideo");
            if (defaultProcess) {
                long i = SystemClock.currentThreadTimeMillis();
                sApplicationContext = this;
                // JPush相关
                JPushInterface.init(sApplicationContext);
                if(!PublicMethod.isServiceWorked(sApplicationContext, "com.meizu.testdevVideo.service.SuperTestService")){
                    Intent mIntent = new Intent(sApplicationContext, SuperTestService.class);
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startService(mIntent);   // 开始服务
                }

                new SimpleTaskHelper(){
                    @Override
                    protected void doInBackground() {
                        try {
                            String cpu = BaseData.getInstance(sApplicationContext).readStringData("CPU");
                            if(TextUtils.isEmpty(cpu)){
                                if(ShellUtil.getProperty("ro.meizu.hardware.soc").equals("")){
                                    BaseData.getInstance(sApplicationContext).writeStringData("CPU", ShellUtil.getProperty("ro.hardware"));
                                }else{
                                    BaseData.getInstance(sApplicationContext).writeStringData("CPU", ShellUtil.getProperty("ro.meizu.hardware.soc"));
                                }
                            }

                            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);   // 获取手机号码信息

                            // 写进IMEI进行保存
                            String meid = PerformsData.getInstance(sApplicationContext).readStringData(iPerformsKey.imei);
                            if(TextUtils.isEmpty(meid)){
                                Log.d(TAG, "获取IMEI");
                                PerformsData.getInstance(sApplicationContext).writeStringData(iPerformsKey.imei, meid = tm.getDeviceId());
                            }

                            String deviceType = PerformsData.getInstance(sApplicationContext).readStringData(iPerformsKey.deviceType);
                            if(TextUtils.isEmpty(deviceType)){
                                Log.d(TAG, "获取设备ID");
                                CommonVariable.about_phone_product_name = ShellUtils.execCommand("getprop ro.product.model", false, true)
                                        .successMsg.replaceAll("\\s+", "");
                                PerformsData.getInstance(sApplicationContext).writeStringData(iPerformsKey.deviceType,
                                        CommonVariable.about_phone_product_name);
                                deviceType = CommonVariable.about_phone_product_name;
                            }

                            mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, meid));
                            Set<String> tagSet = new LinkedHashSet<String>();
                            tagSet.add(deviceType);
                            mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_TAGS, tagSet));
                            MPushStart.startMush();
                            AlarmSetting.getInstance().setOnceAlarm(sApplicationContext,
                                    MPushReceiver.ACTION_HEALTH_CHECK, System.currentTimeMillis() + 10000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }.executeInSerial();

                Logger.d("花费初始化进程：" + processName + "的时间为：" + (SystemClock.currentThreadTimeMillis() - i));
            } else if (processName.contains(":pushcore")) {
                Logger.d("初始化进程：" + processName);
            }
        }
    }

    // 处理绑定别名和TAG
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    JPushInterface.setAliasAndTags(getApplicationContext(), (String) msg.obj, null, mAliasCallback);
                    break;

                case MSG_SET_TAGS:
                    JPushInterface.setAliasAndTags(getApplicationContext(), null, (Set<String>) msg.obj, mTagsCallback);
                    break;
            }
        }
    };

    // 别名回调函数
    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            switch (code) {
                case 0:
                    Log.d(TAG, "Set alias success");
                    break;

                case 6002:
                    if (PublicMethod.hasNetwork(getApplicationContext()) && aliasTryTime > 0) {
                        Log.d(TAG, "Failed to set alias due to timeout. Try again after 60s.");
                        -- aliasTryTime;
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    }
                    break;

            }
        }

    };

    // Tag回调函数
    private final TagAliasCallback mTagsCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            switch (code) {
                case 0:
                    Log.d(TAG, "Set tag success");
                    break;

                case 6002:
                    if (PublicMethod.hasNetwork(getApplicationContext()) && tagsTryTime > 0) {
                        Log.d(TAG, "Failed to set tags due to timeout. Try again after 60s.");
                        -- tagsTryTime;
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_TAGS, tags), 1000 * 60);
                    }
                    break;
            }
        }

    };


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
        }
    }
}
