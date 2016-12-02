package com.meizu.testdevVideo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.service.RegisterAppService;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.shell.ShellUtils;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by HDL on 2016/9/8.
 */

public class MyApplication extends Application {
    private String Tag = "MyApplication";

//    private static final int MSG_SET_ALIAS = 1001;
//    private static final int MSG_SET_TAGS = 1002;

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化sdk
        long i = SystemClock.currentThreadTimeMillis();
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);   // 获取手机号码信息
        JPushInterface.setDebugMode(false);//正式版的时候设置false，关闭调试
        JPushInterface.init(getApplicationContext());

        // 写进IMEI进行保存
        if(TextUtils.isEmpty(PerformsData.getInstance(this).readStringData(iPerformsKey.imei))){
            Log.e(Tag, "获取IMEI");
            CommonVariable.about_phone_imei = tm.getDeviceId();
            PerformsData.getInstance(this).writeStringData(iPerformsKey.imei, CommonVariable.about_phone_imei);
        }

        if(TextUtils.isEmpty(PerformsData.getInstance(this).readStringData(iPerformsKey.deviceType))){
            Log.e(Tag, "获取设备ID");
            CommonVariable.about_phone_product_name = ShellUtils.execCommand("getprop ro.product.model", false, true)
                    .successMsg.replaceAll("\\s+", "");
            PerformsData.getInstance(this).writeStringData(iPerformsKey.deviceType,
                    CommonVariable.about_phone_product_name);
        }

//                Set<String> set = new HashSet<>();
//                set.add(CommonVariable.about_phone_product_name);
//                JPushInterface.setAliasAndTags(getApplicationContext(), CommonVariable.about_phone_imei, set, new TagAliasCallback(){
//                    @Override
//                    public void gotResult(int code, String alias, Set<String> tags) {
//                        String logs ;
//                        switch (code){
//                            case 0:
//                                Log.e(Tag, "打印状态码，0为成功: " + String.valueOf(code));
//                                Log.e(Tag, "设置的别名为" + alias);
//                                Log.e(Tag, "设置Tag为" + tags);
//                                break;
//
//                            case 6002:
//                                logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
//                                Log.i(Tag, logs);
//                                if (PublicMethod.isConnected(getApplicationContext())) {
//                                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
//                                } else {
//                                    Log.i(Tag, "No network");
//                                }
//                                break;
//
//                            default:
//                                logs = "Failed with errorCode = " + code;
//                                Log.e(Tag, logs);
//                                break;
//                        }
//                    }
//                });
        Log.e(Tag, "花费初始化时间为：" + (SystemClock.currentThreadTimeMillis() - i));
    }

//    @SuppressLint("HandlerLeak")
//    private final Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(android.os.Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case MSG_SET_ALIAS:
//                    Log.d(Tag, "Set alias in handler.");
//                    JPushInterface.setAliasAndTags(getApplicationContext(), (String) msg.obj, null, mAliasCallback);
//                    break;
//
//                case MSG_SET_TAGS:
//                    Log.d(Tag, "Set tags in handler.");
//                    JPushInterface.setAliasAndTags(getApplicationContext(), null, (Set<String>) msg.obj, mTagsCallback);
//                    break;
//
//                default:
//                    Log.i(Tag, "Unhandled msg - " + msg.what);
//            }
//        }
//    };
}
