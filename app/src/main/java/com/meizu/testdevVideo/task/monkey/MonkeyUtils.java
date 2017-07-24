package com.meizu.testdevVideo.task.monkey;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.util.ArrayMap;

import com.alibaba.fastjson.JSON;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.AppInfoHelper;
import com.meizu.testdevVideo.push.android.MPush;
import com.meizu.testdevVideo.push.android.bean.MPushMonkeyBean;
import com.meizu.testdevVideo.service.MonkeyService;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.wifi.WifiUtil;

import java.io.IOException;
import java.util.Map;

/**
 * Monkey相关方法
 * Created by maxueming on 2017/5/30.
 */
public class MonkeyUtils {

    // 开始Monkey服务
    public static void startMonkeyService(Context context){
        PublicMethod.wakeUpAndUnlock(context);   // 点亮下屏幕
        // 判断Monkey类型
        switch (MonkeyTableData.getMonkeyAction(context)){
            case Constants.Monkey.LABEL_OF_ACTION_MONKEY_REPORT:
                String cpu = BaseData.getInstance(SuperTestApplication.getContext()).readStringData("CPU");
                MonkeyService.startActionMonkeyReport(context, null != cpu && cpu.contains("mt"));
                break;
            case Constants.Monkey.LABEL_OF_ACTION_JUST_RUN_MONKEY:
                MonkeyService.startActionJustRunMonkey(context);
                break;
        }

    }

    // 获取MonkeyId
    public static void getMonkeyId(String taskType){
        if(WifiUtil.isWifiConnected(SuperTestApplication.getContext())){
            MPushMonkeyBean mPushMonkeyBean = new MPushMonkeyBean();
            mPushMonkeyBean.setTask(Constants.MpushTaskLabel.POST_MONKEY_TASK_AND_KILL_TASK);
            mPushMonkeyBean.setM_meid(PerformsData.getInstance(SuperTestApplication
                    .getContext()).readStringData(iPerformsKey.imei));

            Map<String, String> data = new ArrayMap<String, String>();
            data.put("testType", "monkey");
            data.put("taskType", taskType);
            data.put("appType", BaseData.getInstance(SuperTestApplication
                    .getContext()).readStringData(SettingPreferenceKey.APP_TYPE));
            data.put("appVersion", AppInfoHelper.getInstance().getAppVersion(BaseData
                    .getInstance(SuperTestApplication.getContext()).readStringData(SettingPreferenceKey.MONKEY_PACKAGE)));
            data.put("stVersion", AppInfoHelper.getInstance().getAppVersion(SuperTestApplication.getContext().getPackageName()));
            mPushMonkeyBean.setData(data);
            MPush.I.sendPush(JSON.toJSONString(mPushMonkeyBean).getBytes(Constants.UTF_8));
        }else{
            Logger.file("WIFI无连接，获取MonkeyId失败", Logger.MONKEY_SERVICE);
        }
    }

    // 设置开始Monkey参数
    public static void setStartMonkeyParams(String taskType, String monkeyCommand, long stopMonkeyTime, int monkeyAction){
        MonkeyTableData.setMonkeyStart(SuperTestApplication.getContext(), true);
        MonkeyTableData.setMonkeyType(SuperTestApplication.getContext(), taskType);
        MonkeyTableData.setMonkeyCommand(SuperTestApplication.getContext(), monkeyCommand);
        MonkeyTableData.setMonkeyStopTime(SuperTestApplication.getContext(), stopMonkeyTime);
        MonkeyTableData.setMonkeyAction(SuperTestApplication.getContext(), monkeyAction);
        MonkeyTableData.setMonkeyStartTime(SuperTestApplication.getContext(), String.valueOf(System.currentTimeMillis()));
    }

    // 设置停止Monkey参数
    public static void setStopMonkeyParams(){
        MonkeyTableData.setMonkeyStart(SuperTestApplication.getContext(), false);
    }

    /**
     * 执行monkey前的操作
     * 发送广播
     */
    public static void runMonkeyInit(final Context context){
        final Context mContext = context;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                try {
                    // 停止日志，若之前有人运行mtklog
                    String cpu = BaseData.getInstance(SuperTestApplication.getContext()).readStringData("CPU");
                    if(null != cpu && cpu.contains("mt")){
                        Runtime.getRuntime().exec(CommonVariable.mtkLogBroadcast.replace("%s", "stop").replace("%d", "7"));
                        Thread.sleep(1000);
                    }

                    if(sharedPreferences.getBoolean(SettingPreferenceKey.CLEAR_LOG, true)){
                        if(null != cpu && cpu.contains("mt")){
                            PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "/Android/log");
                            PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "/mtklog");
                        }else{
                            PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "/Android/log");
                        }
                    }

                    if(null != cpu && cpu.contains("mt")){
                        // 以下对MTK工具有效
                        Thread.sleep(1000);
                        Runtime.getRuntime().exec(CommonVariable.singleLogSizeBroadcast.replace("%s",
                                sharedPreferences.getString(SettingPreferenceKey.SINGLE_LOG_SIZE, "4096")));
                        Thread.sleep(1500);
                        Runtime.getRuntime().exec(CommonVariable.allLogSizeBroadcast.replace("%s",
                                sharedPreferences.getString(SettingPreferenceKey.ALL_LOG_SIZE, "10000")));
                        Thread.sleep(1500);
                        if(sharedPreferences.getBoolean(SettingPreferenceKey.CATCH_LOG_TYPE, true)){
                            Runtime.getRuntime().exec(CommonVariable.mtkLogBroadcast.replace("%s", "start").replace("%d", "1"));
                        }else{
                            Runtime.getRuntime().exec(CommonVariable.mtkLogBroadcast.replace("%s", "start").replace("%d", "7"));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
