package com.meizu.testdevVideo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.task.performs.U2AutoTestTaskCallBack;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.SettingPreference;
import com.meizu.testdevVideo.util.wifi.WifiUtil;

import java.io.IOException;

/**
 * 接收案例测试结束广播
 * Created by maxueming on 2016/8/8.
 */
public class PerformsReceiver extends BroadcastReceiver {
    private U2AutoTestTaskCallBack u2AutoTestTaskCallBack;
    public static PerformsReceiver mInstance;

    public synchronized static PerformsReceiver getInstance(){
        if(mInstance == null){
            mInstance = new PerformsReceiver();
        }
        return mInstance;
    }

    public void setOnU2CallBack(U2AutoTestTaskCallBack u2CallBack){
        u2AutoTestTaskCallBack = u2CallBack;
    }


    @Override
    public void onReceive(final Context context, Intent intent) {
        String mAction = intent.getAction();

        if(mAction.equals(Constants.U2TaskConstants.U2_TASK_BROADCAST_ACTION)){
            if(null != u2AutoTestTaskCallBack){
                u2AutoTestTaskCallBack.runU2Task(intent
                        .getStringExtra(Constants.U2TaskConstants.U2_TASK_TASKJSON),
                        intent.getIntExtra(Constants.U2TaskConstants.U2_TASK_TASKID, -1));
            }
        }

        if(mAction.equals(Constants.U2TaskConstants.U2_TASK_APK_INSTALL_SUCCESS_BROADCAST_ACTION)){
            if(null != u2AutoTestTaskCallBack){
                u2AutoTestTaskCallBack.installApkFinish(intent
                        .getStringExtra(Constants.U2TaskConstants.U2_TASK_INSTALLED_APK_ID),
                        intent.getBooleanExtra(Constants.U2TaskConstants.U2_TASK_INSTALLED_APK_PASS_OR_FAIL, false));
            }
        }

        if(mAction.equals(Constants.U2TaskConstants.U2_TASK_ALARM_TIME_FINISH)){
            new SimpleTaskHelper(){

                @Override
                protected void doInBackground() {
                    try {
                        PublicMethod.wakeUpAndUnlock(context);
                        Logger.file("纯净后台，时间到达，唤醒屏幕", Logger.U2TASK);
                        SettingPreference.getInstance(context).setWakeUp(true);
                        SettingPreference.getInstance(context).setWifiLockType("MZ-Inweb-Test");
                        SettingPreference.getInstance(context).setWifiLock(true);
                        PublicMethod.lockWifi(SettingPreference.getInstance(context).getSettingSharedPreferences(), context);
                        try {
                            Runtime.getRuntime().exec("am broadcast -a com.meizu.logreport.adb_cmd --ei action 1 --ei type 0 --ez zip false");
                            Thread.sleep(8 * Constants.TIME.SECOND);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        long lastTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - lastTime < 5 * Constants.TIME.MINUTE){
                            if(!WifiUtil.isWifiConnected(context)){
                                try {
                                    Thread.sleep(20 * Constants.TIME.SECOND);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                Logger.file("纯净后台，执行完毕，发送收集结果广播", Logger.U2TASK);
                                u2AutoTestTaskCallBack.alarmFinish();
                                break;
                            }
                        }
                        if(!WifiUtil.isWifiConnected(context)){
                            Logger.file("纯净后台，WiFi连接不上，请检查周围网络情况，退出", Logger.U2TASK);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Logger.file("闹钟唤醒执行任务异常==>" + e.toString(), Logger.U2TASK);
                    }
                }
            }.executeInSerial();
        }

        if(mAction.equals(Constants.U2TaskConstants.U2_TASK_STOP_TASK)){
            u2AutoTestTaskCallBack.stopTask();
        }
    }
}
