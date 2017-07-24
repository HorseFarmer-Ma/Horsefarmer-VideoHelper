package com.meizu.testdevVideo.service;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.net.wifi.WifiManager;

import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;

import android.preference.PreferenceManager;
import android.util.Log;

import com.meizu.aidl.ISuperTestAidl;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.broadcast.AudioReceiver;
import com.meizu.testdevVideo.broadcast.ScreenWakeUpReceiver;
import com.meizu.testdevVideo.broadcast.SuperTestReceiver;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.broadcast.WifiReceiver;
import com.meizu.testdevVideo.db.util.U2TaskDBUtil;
import com.meizu.testdevVideo.interports.SuperTestCallBack;
import com.meizu.testdevVideo.interports.iPublicConstants;

import com.meizu.testdevVideo.library.ServiceNotificationHelper;
import com.meizu.testdevVideo.library.SimpleTaskHelper;
import com.meizu.testdevVideo.library.SqlAlterHelper;
import com.meizu.testdevVideo.library.apkController.ApkControllerUtils;
import com.meizu.testdevVideo.push.android.MPushService;
import com.meizu.testdevVideo.task.monkey.MonkeyUtils;
import com.meizu.testdevVideo.task.monkey.SilenceAppMonkeyInfo;
import com.meizu.testdevVideo.task.performs.GetFps;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.PublicMethodConstant;
import com.meizu.testdevVideo.util.download.DownloadHelper;
import com.meizu.testdevVideo.util.download.DownloadIdCallback;
import com.meizu.testdevVideo.util.download.DownloadReceiver;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
import com.meizu.testdevVideo.util.sharepreference.SettingPreference;
import com.meizu.testdevVideo.util.shell.ShellUtils;

import java.io.File;
import java.io.IOException;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SuperTestService extends Service implements SuperTestCallBack{

    private static final String TAG = SuperTestService.class.getSimpleName();
    private SharedPreferences settingSharedPreferences = null;
    private Timer mTimer;
    private Map<String, Object> apkMessageMap;
    private int iCheckTimes = 0;
    private int NOTIFICATION_ID = 100;

    @Override
    public void onCreate() {
        super.onCreate();
        SuperTestReceiver.setSuperTestNotification(this);
        if(SettingPreference.getInstance(SuperTestApplication.getContext()).getNotifition()){
            ServiceNotificationHelper.getInstance(this).notification(NOTIFICATION_ID,
                    this, "Multimedia Tool", "ST Running..");
        }
        checkIsInstallLogReport();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long startTime = SystemClock.currentThreadTimeMillis();
        serviceInit();
        registerBroadcastInit();
        timerInit();
        listenerInit();
        Log.d(TAG, "onStartCommand初始化时间为" + (SystemClock.currentThreadTimeMillis() - startTime));
        return START_STICKY;
    }

    @Override
    public void onDestroy(){

        try {
            if(SettingPreference.getInstance(SuperTestApplication.getContext()).getNotifition()){
                ServiceNotificationHelper.getInstance(this).notificationCancel(this, NOTIFICATION_ID);
            }

            Logger.file("服务意外被杀", Logger.SUPER_TEST);
            if(mTimer != null){
                mTimer.cancel();
                mTimer = null;
            }

            if(mDailyTimeTask != null){
                mDailyTimeTask.cancel();
                mDailyTimeTask = null;
            }

            unregisterReceiver(WifiReceiver.getInstance());
            unregisterReceiver(AudioReceiver.getInstance());
            unregisterReceiver(DownloadReceiver.getInstance());
            unregisterReceiver(ScreenWakeUpReceiver.getInstance());

            U2TaskDBUtil.getInstance().closeU2TaskDB();
            SqlAlterHelper.getInstance(SuperTestApplication.getContext()).close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Intent intent = new Intent("st.action.monkey.service.destroy");
            sendBroadcast(intent);
//            startServiceAfterClosed(this, 5);      //5s后重启
        }
        super.onDestroy();
    }


    /**
     * service停掉后自动启动应用
     *
     * @param context
     * @param delayed 延后启动的时间，单位为秒
     */
    private static void startServiceAfterClosed(Context context, int delayed) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayed * 1000, getOperation(context));
    }

    private static PendingIntent getOperation(Context context) {
        Intent intent = new Intent(context, SuperTestService.class);
        PendingIntent operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return operation;
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
        try {
            mDailyTimeTask.cancel();
        }catch (Exception e){
            e.printStackTrace();
            Logger.d("定时器取消" + e.toString());
        }

        try {
            mTimer.schedule(mDailyTimeTask, 10 * Constants.TIME.SECOND, 5 * Constants.TIME.SECOND);
        }catch (Exception e){
            e.printStackTrace();
            Logger.d("定时器设置失败");

        }

    }

    /**
     * 监听器监听初始化
     */
    private void listenerInit(){
        // 监听下载，安装更新的业务APP
        DownloadReceiver.getInstance().setOnDownloadListener(new DownloadIdCallback() {
            @Override
            public void onDownloadListener(final String id, final String filePath) {
                if(id.equals(CommonVariable.strLogReportId)){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean isSuccess = ApkControllerUtils.clientInstall(filePath);
                            if(!isSuccess) {
                                ApkControllerUtils.installApk(getApplicationContext(), new File(filePath));
                            }
                        }
                    }).start();
                }

                if((null != CommonVariable.strU2ApkId && id.equals(CommonVariable.strU2ApkId)) ||
                        (null != CommonVariable.strU2AndroidTestApkId && id.equals(CommonVariable.strU2AndroidTestApkId))){
                    new SimpleTaskHelper(){
                        @Override
                        protected void doInBackground() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            preForU2Task(id, filePath);
                        }
                    }.executeInSerial();
                }

                if(null != SilenceAppMonkeyInfo.getInstance().getUpdateApkId()
                        && id.equals(SilenceAppMonkeyInfo.getInstance().getUpdateApkId())){
                    ServiceNotificationHelper.getInstance(SuperTestApplication.getContext())
                            .notificationCanCancel("Monkey调度", "APK下载完成", 1);
                    Intent intent = new Intent(Constants.Monkey.ACTION_SILENCE_INSTALL_APK);
                    intent.putExtra("filePath", filePath);
                    sendBroadcast(intent);
                }

                int listSize = Constants.UpdateAppValue.appUpdateStringlist.size();
                for(int i = 0; i < listSize; i++){
                    if(id.equals(Constants.UpdateAppValue.appUpdateStringlist.get(i))){
                        Constants.UpdateAppValue.appUpdateStringlist.remove(i);
                        if(filePath.contains("SuperTest.apk")){
                            // 检查到更新，则静默安装
                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    if(ApkControllerUtils.getVersionCode(SuperTestApplication.getContext(), filePath) <
                                            ApkControllerUtils.getPackageVersionCode(SuperTestApplication.getContext(), "com.meizu.testdevVideo")){
                                        ApkControllerUtils.installApk(getApplicationContext(), new File(filePath));

                                    }else{
                                        ServiceNotificationHelper.getInstance(SuperTestApplication.getContext())
                                                .notificationCanCancel("静默更新", "升级APK成功！", 1);
                                        boolean isSuccess = ApkControllerUtils.clientInstall(filePath);
                                        if(!isSuccess) {
                                            ApkControllerUtils.installApk(getApplicationContext(), new File(filePath));
                                        }
                                    }
                                }
                            }).start();

                        }else{
                            ApkControllerUtils.installApk(getApplicationContext(), new File(filePath));
                        }

                        break;
                    }
                }
            }
        });
    }

    private void preForU2Task(String id, String filePath){
        String pkgName = ApkControllerUtils.getPackageName(SuperTestApplication.getContext(), filePath);
        if(ApkControllerUtils.preInstallForApk(SuperTestApplication.getContext(), pkgName, filePath)) {
            boolean isSuccess = ApkControllerUtils.clientInstall(filePath);
            ServiceNotificationHelper.getInstance(SuperTestApplication.getContext())
                    .notificationCanCancel("Monkey调度", "静默安装Apk" + (isSuccess? "成功" : "失败"), 1);
            if(isSuccess){
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (ApkControllerUtils.getVersionCode(SuperTestApplication.getContext(), filePath)
                        == ApkControllerUtils.getPackageVersionCode(SuperTestApplication.getContext(), pkgName)) {
                    Intent intent = new Intent();
                    intent.setAction(Constants.U2TaskConstants.U2_TASK_APK_INSTALL_SUCCESS_BROADCAST_ACTION);
                    intent.putExtra(Constants.U2TaskConstants.U2_TASK_INSTALLED_APK_ID, id);
                    intent.putExtra(Constants.U2TaskConstants.U2_TASK_INSTALLED_APK_PASS_OR_FAIL, isSuccess);
                    sendBroadcast(intent);
                }else{
                    ServiceNotificationHelper.getInstance(SuperTestApplication.getContext())
                            .notificationCanCancel("Monkey调度", "静默安装Apk失败，版本号与安装包不一致", 1);
                }
            }
        }else{
            ServiceNotificationHelper.getInstance(SuperTestApplication.getContext())
                    .notificationCanCancel("Monkey调度", "静默卸载APK失败", 1);
        }
    }

    /**
     * 检测是否安装LogReport，无则下载安装
     */
    private void checkIsInstallLogReport(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                apkMessageMap = PublicMethod.getApkMessage(SuperTestService.this, "com.meizu.logreport");
                if(apkMessageMap == null || Integer.valueOf(apkMessageMap
                        .get(PublicMethodConstant.VERSION_CODE).toString()) < 3000000){
                    File fileDirector = new File(iPublicConstants.LOCAL_MEMORY + "SuperTest/UpdateApk/常用工具");
                    // 不是文件夹，则新建文件夹
                    if(!fileDirector.isDirectory()){
                        fileDirector.delete();
                        fileDirector.mkdirs();
                    }
                    File file = new File(iPublicConstants.LOCAL_MEMORY + "SuperTest/UpdateApk/常用工具/LogReport.apk");
                    if(file.exists()){
                        file.delete();
                    }
                    CommonVariable.strLogReportId = DownloadHelper.getInstance(SuperTestService.this)
                            .download("http://ats.meizu.com/static/upload/user-resources" +
                            "/SuperTest/MediaAppUpdate/常用工具/LogReport.apk", "/SuperTest/UpdateApk/常用工具/", "LogReport.apk");
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

            ++ iCheckTimes;
            if(iCheckTimes > 360){
                String last_clear_time = BaseData.getInstance(SuperTestService.this)
                        .readStringData(SettingPreferenceKey.LSAT_CLEAR_CACHE_TIME);
                long clear_times = Long.parseLong(settingSharedPreferences.getString(SettingPreferenceKey.CLEAR_CACHE,
                        getString(R.string.clear_cache_orign_times)))
                        * 24 * 60 * 60 * 1000;
                if(null == last_clear_time || clear_times < System.currentTimeMillis() - Long.parseLong(last_clear_time)){
                    BaseData.getInstance(SuperTestService.this)
                            .writeStringData(SettingPreferenceKey.LSAT_CLEAR_CACHE_TIME,
                                    String.valueOf(System.currentTimeMillis()));
                    PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "SuperTest/ApkLog");
                    PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "SuperTest/LogReport");
                    PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "SuperTest/Trace");
                    PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "SuperTest/Dropbox");
                }

                iCheckTimes = 0;
            }

            if(MonkeyTableData.isMonkeyStart(getApplicationContext())){
                if(!PublicMethod.isServiceWorked(SuperTestService.this, "com.meizu.testdevVideo.service.MonkeyService")){
                    try {
                        Thread.sleep(2 * Constants.TIME.SECOND);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(MonkeyTableData.isMonkeyStart(getApplicationContext()) && !PublicMethod
                            .isServiceWorked(SuperTestService.this, "com.meizu.testdevVideo.service.MonkeyService")){
                        PublicMethod.killProcess("ps |grep com.android.commands.monkey", "system    ", " ");
                        Logger.file("SuperTest触发唤醒，继续执行Monkey！", Logger.SUPER_TEST);
                        MonkeyUtils.startMonkeyService(SuperTestService.this);
                    }
                }
            }else{
                if((ShellUtils.execCommand("ps|grep com.android.commands.monkey",
                        false, true).successMsg).length() > 0){
                    PublicMethod.killProcess("ps |grep com.android.commands.monkey", "system    ", " ");
                }
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    /**
     * SuperTest接口
     */
    private Binder binder = new ISuperTestAidl.Stub() {

        @Override
        public void exec(String command) throws RemoteException {
            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String execWithResult(String command) throws RemoteException {
            return ShellUtils.execCommand(command, false, true).successMsg;
        }

        @Override
        public void fpsClear() throws RemoteException {
            try {
                GetFps.clear();
            }catch (Exception e){
                Logger.file("fpsClear" + e, Logger.PERFORMS_SERVICE);
            }
        }

        @Override
        public void fpsClearBuffer(String packageName, String activityName) throws RemoteException {
            try {
                GetFps.clearBuffer(packageName + "/" + activityName);
            } catch (Exception e) {
                Logger.file("fpsClearBuffer" + e, Logger.PERFORMS_SERVICE);
            }
        }

        @Override
        public boolean fpsDumpFrameLatency(String packageName, String activityName) throws RemoteException {
            try {
                return GetFps.dumpFrameLatency(packageName + "/" + activityName, true);
            } catch (Exception e) {
                Logger.file("fpsDumpFrameLatency" + e, Logger.PERFORMS_SERVICE);
            }
            return false;
        }

        @Override
        public int fpsGetSM() throws RemoteException {
            try{
                return GetFps.getSM();
            }catch (Exception e){
                Logger.file("fpsGetSM" + e, Logger.PERFORMS_SERVICE);
            }
            return -1;
        }

        @Override
        public void runMonkey(String monkeyCommand) throws RemoteException {
            MonkeyTableData.setMonkeyCommand(getApplicationContext(), monkeyCommand); // 设置命令
            MonkeyTableData.setMonkeyStart(getApplicationContext(), true);   // 开始Monkey
            MonkeyTableData.setMonkeyAction(getApplicationContext(),
                    Constants.Monkey.LABEL_OF_ACTION_JUST_RUN_MONKEY);    // 仅跑Monkey
            MonkeyUtils.startMonkeyService(SuperTestService.this);
        }

        @Override
        public void stopMonkey() throws RemoteException {
            Intent intent = new Intent();
            intent.setAction("action.st.kill.monkey");
            sendBroadcast(intent);
        }
    };

    @Override
    public void changeNotification(boolean isOpen) {
        if(isOpen){
            ServiceNotificationHelper.getInstance(this).notification(NOTIFICATION_ID,
                    this, "Multimedia Tool", "ST Running..");
        }else{
            ServiceNotificationHelper.getInstance(this).notificationCancel(this, NOTIFICATION_ID);
        }
    }
}