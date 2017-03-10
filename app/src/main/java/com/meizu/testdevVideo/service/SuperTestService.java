package com.meizu.testdevVideo.service;

import android.app.DownloadManager;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.broadcast.AudioReceiver;
import com.meizu.testdevVideo.broadcast.ScreenWakeUpReceiver;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.fragment.MonkeyFragment;
import com.meizu.testdevVideo.fragment.NewAppUpdateFragment;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.broadcast.WifiReceiver;
import com.meizu.testdevVideo.library.ServiceNotificationHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.PublicMethodConstant;
import com.meizu.testdevVideo.util.download.DownloadHelper;
import com.meizu.testdevVideo.util.download.DownloadIdCallback;
import com.meizu.testdevVideo.util.download.DownloadReceiver;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.shell.ShellUtil;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Monkey服务
 */
public class SuperTestService extends Service implements NewAppUpdateFragment.OnDownloadListener,
                                                MonkeyService.OnMonkeyServiceListener{

    private static final int NOTIFICATION_ID = 104;
    private static final String TAG = SuperTestService.class.getSimpleName();
    private SharedPreferences settingSharedPreferences = null;
    private Timer mTimer;
    private ArrayList<String> appUpdateStringlist;
    private Map<String, Object> apkMessageMap;


    @Override
    public void onCreate() {
        long startTime = SystemClock.currentThreadTimeMillis();
        appUpdateStringlist = new ArrayList<String>();
        NewAppUpdateFragment.setOnDownloadListener(this);
        serviceInit();
        registerBroadcastInit();
        timerInit();
        listenerInit();
        checkIsInstallLogReport();
        MonkeyService.setOnMonkeyServiceListener(this);
        Log.e(TAG, "onCreate初始化时间为" + (SystemClock.currentThreadTimeMillis() - startTime));
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Notification.Builder builder = new Notification.Builder(this);
//        builder.setSmallIcon(R.mipmap.ic_app)
//                .setContentTitle("SuperTest")
//                .setDefaults(Notification.DEFAULT_SOUND)
//                .setContentText("SuperTest Running...");
//        Notification notification = builder.build();
//        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
//        stopForeground(true);
        PublicMethod.saveLog(TAG, "服务意外被杀");
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }

        if(mDailyTimeTask != null){
            mDailyTimeTask.cancel();
            mDailyTimeTask = null;
        }

        if(mRegisterTask != null){
            mRegisterTask.cancel();
            mRegisterTask = null;
        }

        unregisterReceiver(WifiReceiver.getInstance());
        unregisterReceiver(AudioReceiver.getInstance());
        unregisterReceiver(DownloadReceiver.getInstance());
        unregisterReceiver(ScreenWakeUpReceiver.getInstance());
        Intent intent = new Intent("st.action.monkey.service.destroy");
        sendBroadcast(intent);
        super.onDestroy();
    }

    /**
     * 服务初始化
     */
    private void serviceInit(){
        settingSharedPreferences = ((settingSharedPreferences ==  null)?
                PreferenceManager.getDefaultSharedPreferences(this) : settingSharedPreferences);
        PublicMethod.mute(this);
        PublicMethod.lockWifi(settingSharedPreferences, this);
    }

    /**
     * 注册广播
     */
    private void registerBroadcastInit(){
        IntentFilter downloadFilter = new IntentFilter();
        IntentFilter netFilter = new IntentFilter();
        IntentFilter audioFliter = new IntentFilter();
        IntentFilter wakeUpFliter = new IntentFilter();

        downloadFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloadFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);

        netFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        netFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        netFilter.addAction("action.st.wifi.bind");

        audioFliter.addAction("android.media.VOLUME_CHANGED_ACTION");

        wakeUpFliter.addAction(Intent.ACTION_SCREEN_OFF);

        registerReceiver(WifiReceiver.getInstance(), netFilter);
        registerReceiver(AudioReceiver.getInstance(), audioFliter);
        registerReceiver(ScreenWakeUpReceiver.getInstance(), wakeUpFliter);
        registerReceiver(DownloadReceiver.getInstance(), downloadFilter);
    }

    /**
     * 定时器初始化
     */
    private void timerInit(){
        mTimer = new Timer();
        mTimer.schedule(mDailyTimeTask, 10 * Constants.TIME.SECOND, 10 * Constants.TIME.SECOND);
        mTimer.schedule(mRegisterTask, 10 * Constants.TIME.SECOND);
    }

    /**
     * 监听器监听初始化
     */
    private void listenerInit(){
        // 监听下载，安装更新的业务APP
        DownloadReceiver.getInstance().setOnDownloadListener(new DownloadIdCallback() {
            @Override
            public void onDownloadListener(String id, String filePath) {
                if(id.equals(CommonVariable.strVideoId) || id.equals(CommonVariable.strMusicId)
                        || id.equals(CommonVariable.strReaderId) || id.equals(CommonVariable.strEbookId)
                        || id.equals(CommonVariable.strGalleryId) || id.equals(CommonVariable.strVipId)){
                    if(filePath != null){
                        PublicMethod.installApp(getApplicationContext(), new File(filePath));
                    }else{
                        ToastHelper.addToast("请您查看服务器放置您的包了没有", getApplicationContext());
                    }
                }

                if(id.equals(CommonVariable.strLogReportId)){
                    try {
                        Runtime.getRuntime().exec("pm install -f " + filePath);
                    } catch (IOException e) {
                        PublicMethod.installApp(getApplicationContext(), new File(filePath));
                        e.printStackTrace();
                    }
                }

                int listSize = appUpdateStringlist.size();
                for(int i = 0; i < listSize; i++){
                    if(id.equals(appUpdateStringlist.get(i))){
                        PublicMethod.installApp(getApplicationContext(), new File(filePath));
                        appUpdateStringlist.remove(i);
                        break;
                    }
                }
            }
        });

    }

    /**
     * 检测是否安装LogReport，无则下载安装
     */
    private void checkIsInstallLogReport(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                apkMessageMap = PublicMethod.getApkMessage(SuperTestService.this, "com.meizu.logreport");
                if(apkMessageMap != null){
                    if(Integer.valueOf(apkMessageMap.get(PublicMethodConstant.VERSION_CODE).toString()) < 3000000){
                        CommonVariable.strLogReportId = DownloadHelper.getInstance(SuperTestService.this).download("http://ats.meizu.com/static/upload/user-resources" +
                                "/SuperTest/MediaAppUpdate/LogReport/LogReport.apk", "/SuperTest/UpdateApk/", "LogReport.apk");
                    }
                }else{
                    CommonVariable.strLogReportId = DownloadHelper.getInstance(SuperTestService.this).download("http://ats.meizu.com/static/upload/user-resources" +
                            "/SuperTest/MediaAppUpdate/LogReport/LogReport.apk", "/SuperTest/UpdateApk/", "LogReport.apk");
                }
            }
        }).start();
    }


    /**
     * 定时器
     */
    private TimerTask mDailyTimeTask = new TimerTask() {
        @Override
        public void run() {
            if (!PublicMethod.isServiceWorked(SuperTestService.this,
                    "com.meizu.testdevVideo.service.DaemonService")) {
                startService(new Intent(SuperTestService.this, DaemonService.class));
                PublicMethod.saveLog("SuperTestService", "检测到守护服务挂了，重新启动守护服务DaemonService");
            }

            if(MonkeyTableData.getInstance(SuperTestService.this).readBooleanData("isStart")
                    && !PublicMethod.isServiceWorked(SuperTestService.this,
                    "com.meizu.testdevVideo.service.MonkeyService")){
                PublicMethod.saveLog("MonkeyService", "SuperTestService检测到MonkeyService服务挂了，重启");
                String cpu = ShellUtil.getProperty("ro.hardware");
                MonkeyService.startActionMonkeyReport(SuperTestService.this, null != cpu && cpu.contains("mt"));
            }
        }
    };

    /**
     * 定时器
     */
    private TimerTask mRegisterTask = new TimerTask() {
        @Override
        public void run() {
            Log.e(TAG, "执行mRegisterTask注册服务");
            if(!PerformsData.getInstance(SuperTestService.this).readBooleanData(iPerformsKey.isRegister)){
                Intent registerIntent = new Intent(SuperTestService.this, RegisterAppService.class);
                startService(registerIntent);
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDownloadListener(String id) {
        Log.e(TAG, "点击的生成下载ID = " + id);
        appUpdateStringlist.add(id);
    }

    @Override
    public void monkeyServiceListener() {
        PublicMethod.saveLog("MonkeyService", "回调监听，monkey进程被杀死，重新启动服务");
        String cpu = ShellUtil.getProperty("ro.hardware");
        MonkeyService.stopActionMonkeyReport(this);
        MonkeyService.startActionMonkeyReport(this, null != cpu && cpu.contains("mt"));
    }
}