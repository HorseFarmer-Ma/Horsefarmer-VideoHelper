package com.meizu.testdevVideo.service;

import android.app.DownloadManager;
import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;

import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.ImageButton;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.broadcast.AudioReceiver;
import com.meizu.testdevVideo.broadcast.MonkeyReceiver;
import com.meizu.testdevVideo.broadcast.ScreenWakeUpReceiver;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.PerformsMonkeyCallBack;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.library.ServiceHelper;
import com.meizu.testdevVideo.broadcast.WifiReceiver;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.PublicMethodConstant;
import com.meizu.testdevVideo.util.download.DownloadHelper;
import com.meizu.testdevVideo.util.download.DownloadIdCallback;
import com.meizu.testdevVideo.util.download.DownloadReceiver;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.sharepreference.PrefWidgetOnOff;
import com.meizu.testdevVideo.util.shell.ShellUtils;
import com.meizu.testdevVideo.library.WindowManagerHelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Monkey服务
 */
public class SuperTestService extends Service {
    private static final String TAG = SuperTestService.class.getSimpleName();
    private SharedPreferences settingSharedPreferences = null;
    private WindowManagerHelper windowManagerHelper = null;
    private ServiceHelper serviceHelper = null;

    private View startView;             // 悬浮按钮视图
    private ImageButton startTestBt, stopBt;      // 图片按钮
    private String monkey_command;
    private Timer mTimer;
    private static final int mId = 1;                    // 通知栏Id

    private Map<String, Object> apkMessageMap;

    public SuperTestService() {}

    @Override
    public void onCreate() {
        long startTime = SystemClock.currentThreadTimeMillis();
        serviceInit();
        registerBroadcastInit();
        timerInit();
        listenerInit();
        checkIsInstallLogReport();
        Log.e(TAG, "onCreate初始化时间为" + (SystemClock.currentThreadTimeMillis() - startTime));
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        monkey_command = MonkeyTableData.getInstance(this).readStringData("monkey_command");

        if(!PrefWidgetOnOff.getInstance(this).readBooleanData("isMonkeyFloating")){
            if (MonkeyTableData.getInstance(this).readBooleanData("isStart")){
                serviceHelper.showButtonNotify("SuperTest", "SuperTest running", "Start running", mId);
                runMonkeyThread();
                saveLog("不使用悬浮按钮\n");
            }
        }else {
            saveLog("使用悬浮按钮\n");
            monkey_Init();
            if(MonkeyTableData.getInstance(this).readBooleanData("isStart")){
                serviceHelper.showButtonNotify("SuperTest", "SuperTest running", "Start running", mId);
                saveLog("之前意外停止，重新开始Monkey任务\n");
                stopBt.setVisibility(View.VISIBLE);
                startTestBt.setVisibility(View.GONE);
                runMonkeyThread();   // 跑monkey
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        saveLog("服务执行onDestroy\n");
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }

        if(mMonkeyTimeTask != null){
            mMonkeyTimeTask.cancel();
            mMonkeyTimeTask = null;
        }

        if(mRegisterTask != null){
            mRegisterTask.cancel();
            mRegisterTask = null;
        }

        if(startView != null){
            startView = null;
        }

        if(windowManagerHelper != null){
            windowManagerHelper = null;
        }

        unregisterReceiver(mReceiver);
        unregisterReceiver(WifiReceiver.getInstance());
        unregisterReceiver(AudioReceiver.getInstance());
        unregisterReceiver(DownloadReceiver.getInstance());
        unregisterReceiver(MonkeyReceiver.getInstance());
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
        serviceHelper = ((serviceHelper == null)? new ServiceHelper(this) : serviceHelper);
        windowManagerHelper = ((windowManagerHelper == null)? new WindowManagerHelper(getApplication()) : windowManagerHelper);
        PublicMethod.mute(this);
        PublicMethod.lockWifi(settingSharedPreferences, this);
        startView = LayoutInflater.from(this).inflate(R.layout.monkey_start, null);
        startTestBt = (ImageButton) startView.findViewById(R.id.start);
        stopBt = (ImageButton) startView.findViewById(R.id.stop);
    }

    /**
     * 注册广播
     */
    private void registerBroadcastInit(){
        IntentFilter downloadFilter = new IntentFilter();
        downloadFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloadFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        IntentFilter netFilter = new IntentFilter();
        netFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        netFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        netFilter.addAction("action.st.wifi.bind");
        IntentFilter killMonkeyFliter = new IntentFilter();
        killMonkeyFliter.addAction("action.st.kill.monkey");
        IntentFilter audioFliter = new IntentFilter();
        audioFliter.addAction("android.media.VOLUME_CHANGED_ACTION");
        IntentFilter monkeyFliter = new IntentFilter();
        IntentFilter wakeUpFliter = new IntentFilter();
        wakeUpFliter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(WifiReceiver.getInstance(), netFilter);
        registerReceiver(AudioReceiver.getInstance(), audioFliter);
        registerReceiver(mReceiver, killMonkeyFliter);
        registerReceiver(MonkeyReceiver.getInstance(), monkeyFliter);
        registerReceiver(ScreenWakeUpReceiver.getInstance(), wakeUpFliter);
        registerReceiver(DownloadReceiver.getInstance(), downloadFilter);
    }

    /**
     * 定时器初始化
     */
    private void timerInit(){
        mTimer = new Timer();
        mTimer.schedule(mMonkeyTimeTask, 10 * Constants.TIME.MINUTE, 10 * Constants.TIME.MINUTE);
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
                        || id.equals(CommonVariable.strGalleryId)){
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
            }
        });

        // 设置执行性能测试monkey监听
        MonkeyReceiver.getInstance().setPerformsMonkeyListener(new PerformsMonkeyCallBack() {
            @Override
            public void startMonkey(String packageName) {
                PublicMethod.mute(getApplicationContext());
                Intent monkeyIntent = new Intent(SuperTestService.this, MonkeyProcessService.class);
                monkeyIntent.putExtra(MonkeyProcessService.packageName, packageName);
                startService(monkeyIntent);
            }
        });
    }

    /**
     * 悬浮窗口初始化
     */
    public void monkey_Init(){
        saveLog("启动悬浮窗_初始化开始\n");
        windowManagerHelper.createView(startView, WindowManager.LayoutParams.WRAP_CONTENT, true, true);
        stopBt.setVisibility(View.GONE);
        startTestBt.setVisibility(View.VISIBLE);
        stopBt.setOnLongClickListener(new Button.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                killMonkey();
                return true;
            }
        });

        startTestBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceHelper.showButtonNotify("SuperTest", "SuperTest running", "Start running", mId);
                MonkeyTableData.getInstance(getApplicationContext()).writeBooleanData("isStart", true);
                startTestBt.setVisibility(View.GONE);
                stopBt.setVisibility(View.VISIBLE);
                runMonkeyThread();
            }
        });
        saveLog("启动悬浮窗_初始化完成\n");
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
     * 跑monkey线程
     */
    private void runMonkeyThread(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                saveLog("执行Monkey指令\n");
                PublicMethod.mute(getApplicationContext());
                try {
                    Runtime.getRuntime().exec(monkey_command);
                } catch (IOException e){
                    e.printStackTrace();
                    saveLog("哎呀。意外出错\n");
                }
                return null;
            }
        }.execute();
    }

    /**
     * 杀monkey
     */
    private void killMonkey(){
        PublicMethod.killProcess("ps |grep com.android.commands.monkey", "system    ", " ");
        // 停止monkey
        if(PublicMethod.isServiceWorked(this, "com.meizu.testdevVideo.service.MonkeyProcessService")){
            Intent monkeyIntent = new Intent(SuperTestService.this, MonkeyProcessService.class);
            stopService(monkeyIntent);
        }

        if(MonkeyTableData.getInstance(this).readBooleanData("isStart")){
            serviceHelper.cancelShowNotify(mId);
            // 停止LogReport收集日志
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(settingSharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_MTK_SET, true)){
                        try {
                            if(CommonVariable.snLabel.contains("71") || CommonVariable.snLabel.contains("76") || CommonVariable.snLabel.contains("86")
                                    || CommonVariable.snLabel.contains("96")){
                                if(settingSharedPreferences.getBoolean(SettingPreferenceKey.CATCH_LOG_TYPE, true)){
                                    Runtime.getRuntime().exec(CommonVariable.stopCatLogBroadcast.replace("%d", "1"));
                                }else{
                                    Runtime.getRuntime().exec(CommonVariable.stopCatLogBroadcast.replace("%d", "7"));
                                }
                            }else{
                                if(settingSharedPreferences.getBoolean(SettingPreferenceKey.CATCH_LOG_TYPE, true)){
                                    Runtime.getRuntime().exec(CommonVariable.mtkLogBroadcast.replace("%s", "stop").replace("%d", "1"));
                                }else{
                                    Runtime.getRuntime().exec(CommonVariable.mtkLogBroadcast.replace("%s", "stop").replace("%d", "7"));
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        if(PrefWidgetOnOff.getInstance(this).readBooleanData("isMonkeyFloating")){
            windowManagerHelper.removeView(startView);
        }

        PrefWidgetOnOff.getInstance(SuperTestService.this).writeBooleanData("isMonkeyFloating", false);
        MonkeyTableData.getInstance(this).writeBooleanData("isStart", false);
        saveLog("正常停止monkey\n");
    }

    /**
     * 注册杀monkey接收广播事件
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("action.st.kill.monkey")) {
                killMonkey();
            }
        }
    };

    /**
     * 定时器
     */
    private TimerTask mMonkeyTimeTask = new TimerTask() {
        @Override
        public void run() {
            boolean isStart = MonkeyTableData.getInstance(getApplicationContext()).readBooleanData("isStart");
            boolean isMonkeyProcessLiving = (ShellUtils.execCommand("ps|grep com.android.commands.monkey",
                    false, true).successMsg).length() > 0;
            if(isStart && !isMonkeyProcessLiving){
                saveLog("触发定时任务，monkey进程被意外杀死\n");
                runMonkeyThread();
            }
        }
    };

    /**
     * 定时器
     */
    private TimerTask mRegisterTask = new TimerTask() {
        @Override
        public void run() {
            if(!PerformsData.getInstance(SuperTestService.this).readBooleanData(iPerformsKey.isRegister)){
                Log.e("SuperTestService", "启动注册服务");
                Intent registerIntent = new Intent(SuperTestService.this, RegisterAppService.class);
                startService(registerIntent);
            }
        }
    };

    /**
     * 保存Log到SuperTestService_Log.txt
     * @param log
     */
    private void saveLog(String log){
        PublicMethod.saveStringToFileWithoutDeleteSrcFile(PublicMethod.getSystemTime() + log,
                "SuperTestService_Log", iPublicConstants.LOCAL_MEMORY + "SuperTest/ApkLog/");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}