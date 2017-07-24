package com.meizu.testdevVideo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.AlarmSetting;
import com.meizu.testdevVideo.library.ServiceNotificationHelper;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.library.apkController.ApkControllerUtils;
import com.meizu.testdevVideo.task.monkey.MonkeyUtils;
import com.meizu.testdevVideo.task.monkey.SilenceAppMonkeyInfo;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.BaseData;


/**
 * Monkey相关
 * Created by maxueming on 2017/5/30.
 */
public class MonkeyReceiver extends BroadcastReceiver{
    private Context mContext;
    @Override
    public void onReceive(final Context context, final Intent intent) {
        mContext = context;
        String action = intent.getAction();
        // 静默安装APK，并执行Monkey指令
        if(null != action && action.equals(Constants.Monkey.ACTION_SILENCE_INSTALL_APK)){
            new SimpleTaskHelper(){
                @Override
                protected void doInBackground() {
                    if(null != intent.getStringExtra("filePath") && !intent.getStringExtra("filePath").equals("")){
                        String pkgName = ApkControllerUtils.getPackageName(SuperTestApplication.getContext(), intent.getStringExtra("filePath"));
                        if(!TextUtils.isEmpty(pkgName) && ApkControllerUtils.preInstallForApk(SuperTestApplication
                                .getContext(), pkgName, iPublicConstants.LOCAL_MEMORY +
                                "SuperTest/SilenceTask/" + "SilenceTask.apk")){
                            Logger.file("MPush==>静默安装Apk", Logger.MPUSH_TASK_SERVICE);
                            ServiceNotificationHelper.getInstance(SuperTestApplication.getContext())
                                    .notificationCanCancel("Monkey调度", "静默安装APK", 1);
                            boolean isSuccess = ApkControllerUtils.clientInstall(intent.getStringExtra("filePath"));
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (isSuccess){
                                Logger.file("MPush==>静默安装Apk成功，开始执行Monkey", Logger.MPUSH_TASK_SERVICE);
                                ServiceNotificationHelper.getInstance(SuperTestApplication.getContext())
                                        .notificationCanCancel("Monkey调度", "静默安装APK成功，开始执行", 1);
                                runSilenceMonkeyTask();
                            }else{
                                ServiceNotificationHelper.getInstance(SuperTestApplication.getContext())
                                        .notificationCanCancel("Monkey调度", "静默安装APK失败，停止执行", 1);
                                Logger.file("MPush==>静默安装Apk失败", Logger.MPUSH_TASK_SERVICE);
                            }
                        }else{
                            if(TextUtils.isEmpty(pkgName)){
                                ServiceNotificationHelper.getInstance(context)
                                        .notificationCanCancel("Monkey任务出错", "APK下载地址有误", 1);
                                Logger.file("包名解析为空，传入失败", Logger.MPUSH_TASK_SERVICE);
                            }else{
                                Logger.file("云端任务静默卸载APK失败", Logger.MPUSH_TASK_SERVICE);
                            }
                        }
                    }else if(null != intent.getStringExtra("filePath") && intent.getStringExtra("filePath").equals("")){
                        ServiceNotificationHelper.getInstance(SuperTestApplication.getContext())
                                .notificationCanCancel("Monkey调度", "本任务不升级APK，直接执行Monkey", 1);
                        runSilenceMonkeyTask();
                    }else{
                        ServiceNotificationHelper.getInstance(SuperTestApplication.getContext())
                                .notificationCanCancel("Monkey调度", "APK地址为null，请检查云端配置", 1);
                    }

                }
            }.executeInSerial();
        }
    }


    /**
     * 跑静默Monkey
     */
    private void runSilenceMonkeyTask(){
        String monkeyCommand = null;
        String taskType = null;
        taskType = SilenceAppMonkeyInfo.getInstance()
                .getMonkeyType().equals("1")? "应用级" : "系统级";
        if (taskType.equals("系统级")){
            // 写黑名单
            PublicMethod.saveStringToFile(PublicMethod.readFile(iPublicConstants
                            .MEMORY_BACK_UP + "blacklist.txt") + "\n" + SilenceAppMonkeyInfo
                            .getInstance().getPkgBlacklist().replace(",", "\n"),
                    "blacklist.txt", iPublicConstants.LOCAL_MEMORY);
        }

        if(BaseData.getInstance(SuperTestApplication.getContext()).readStringData("CPU").contains("mt")){
            monkeyCommand = taskType.equals("应用级")? String.format(Constants.MonkeyCommand.MTK_APP_MONKEY,
                    SilenceAppMonkeyInfo.getInstance().getSeed(), SilenceAppMonkeyInfo.getInstance().getApp(),
                    SilenceAppMonkeyInfo.getInstance().getTimes(), SilenceAppMonkeyInfo.getInstance().getNumber())
                    : Constants.MonkeyCommand.MTK_SYSTEM_MONKEY;
        }else{
            monkeyCommand =  taskType.equals("应用级")? String.format(Constants.MonkeyCommand.SAMSUNG_APP_MONKEY,
                    SilenceAppMonkeyInfo.getInstance().getApp(), SilenceAppMonkeyInfo.getInstance().getTimes(),
                    SilenceAppMonkeyInfo.getInstance().getNumber()) : Constants.MonkeyCommand.SAMSUNG_SYSTEM_MONKEY;
        }

        Logger.file("执行Monkey指令为：" + monkeyCommand, Logger.MPUSH_TASK_SERVICE);

        MonkeyUtils.setStartMonkeyParams(taskType, monkeyCommand,
                System.currentTimeMillis() + Long.parseLong(SilenceAppMonkeyInfo.getInstance()
                        .getRuntime()) * 60 * 1000, Constants.Monkey.LABEL_OF_ACTION_MONKEY_REPORT);
        // 获取Monkey任务id
        MonkeyUtils.getMonkeyId(taskType);
        // 跑Monkey前初始化
        MonkeyUtils.runMonkeyInit(mContext);
        // 开始Monkey服务
        MonkeyUtils.startMonkeyService(mContext);
        // 设置Monkey停止时间
        AlarmSetting.getInstance().setOnceAlarm(mContext, Constants.Monkey.ACTION_KILL_MONKEY,
                System.currentTimeMillis() + Long.parseLong(SilenceAppMonkeyInfo.getInstance().getRuntime()) * 60 * 1000);
        SilenceAppMonkeyInfo.getInstance().clearParma();                // 执行完毕，清除参数信息
    }
}
