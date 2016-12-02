package com.meizu.testdevVideo.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.interports.PerformsCaseCompleteCallBack;
import com.meizu.testdevVideo.interports.PerformsJarDownloadCallBack;
import com.meizu.testdevVideo.broadcast.PerformsReceiver;
import com.meizu.testdevVideo.constant.GetPerformsParams;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublic;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.library.WindowManagerHelper;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.adapter.data.listview.Fps;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.download.DownloadHelper;
import com.meizu.testdevVideo.util.download.DownloadReceiver;
import com.meizu.testdevVideo.util.sendReport.PerformsReportSend;
import com.meizu.testdevVideo.util.sendReport.ReportSendCallBack;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.shell.ShellUtils;
import com.meizu.testdevVideo.util.wifi.WifiUtil;

import net.tsz.afinal.http.AjaxParams;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



public class PerformsTestService extends Service {

    private String TAG = PerformsTestService.class.getSimpleName();
    private View startView;                  // 悬浮按钮视图
    private WindowManagerHelper windowManagerHelper = null;
    private TextView txt_test_type;
    private Timer mTimer;
    private TimerTask mFpsTimeTask;  // 采集Fps
    private int checkJarTime = 2 * 60 * 1000;    // 定时检测是否生成了jar包的时间间隔
    private int checkUiautomatorRunOrNot = 1 * 60 * 1000;    // 延时检测Uiautomator进程是否存在的时间
    private String strDownloadJarId;    // 下载jar包的Id
    private TimerTask checkStopTheServiceOrNotTask;
    private boolean isSendComplete = false;
    private int taskType;         // 任务类型：0 本地任务  1 云端任务

    // 如果id设置为0,会导致不能设置为前台
    private static final int NOTIFICATION_ID = 101;
    private static final Class<?>[] mSetForegroundSignature = new Class[] { boolean.class };
    private static final Class<?>[] mStartForegroundSignature = new Class[] { int.class , Notification.class };
    private static final Class<?>[] mStopForegroundSignature = new Class[] { boolean.class };
    private NotificationManager mNM;
    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    private boolean mReflectFlg = false;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        CommonVariable.isPerformsStart = true;
        IntentFilter performsFilter = new IntentFilter();
        performsFilter.addAction("action.st.performs.test.over");
        performsFilter.addAction("action.st.kill.performs");
        registerReceiver(PerformsReceiver.getInstance(), performsFilter);
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    /**
     * 初始化函数
     */
    private void init(){
        mTimer = new Timer();
        String testType = PerformsData.getInstance(this).readStringData(iPerformsKey.testType);
        PerformsData.getInstance(this).writeBooleanData(iPerformsKey.isStart, true);
        String windowTypeText = "未知测试类型";
        if(testType.equals("memory")){
            windowTypeText = "内存测试";
        }else if(testType.equals("purebackstage")){
            windowTypeText = "纯净后台";
        }else if(testType.equals("framerate")){
            windowTypeText = "帧率测试";
        }else if(testType.equals("starttime")){
            windowTypeText = "启动时间";
        }

        windowManagerHelper = new WindowManagerHelper(this);
        // 设置悬浮按钮
        Window_Init();
        txt_test_type.setText(getResources().getString(R.string.performs_test_type) + windowTypeText);

        // 下载jar包
        saveLogLocal("下载JAR包");
        strDownloadJarId = DownloadHelper.getInstance(this).download(iPublic.PERFORMS_TESTCASE_DOWNLOAD_URL,
                "/SuperTest/PerformsTest/TestCase/", iPublic.PERFORMS_JAR_NAME);
        // 设置下载jar包监听定时器
        mTimer.schedule(mJarTimeTask, checkJarTime, checkJarTime);

        // 设置jar包下载监听
        DownloadReceiver.getInstance().setOnPerformsJarDownloadListener(new PerformsJarDownloadCallBack() {
            @Override
            public void onDownLoadComplete(String id, String path){
                if(id.equals(strDownloadJarId)){
                    Log.d(TAG, "下载JAR包完成");
                    saveLogLocal("下载JAR包完成");
                    PublicMethod.killProcess("ps|grep uiautomator", "system    ", " ");

                    final String uiCommand = "/system/bin/sh /data/data/com.meizu.testdevVideo/files/uitest/a5/uiautomator runtest "
                            + iPublic.PERFORMS_TESTCASE_PATH + iPublic.PERFORMS_JAR_NAME
                            + " -c " + PerformsData.getInstance(getApplicationContext()).readStringData(iPerformsKey.doPackageName);
                    Log.e(TAG, "执行的指令为：\n" + uiCommand);

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            try {
                                Runtime.getRuntime().exec(uiCommand);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                }
            }
        });

        try {
            mStartForeground = PerformsTestService.class.getMethod("startForeground" , mStartForegroundSignature);
            mStopForeground = PerformsTestService.class.getMethod("stopForeground" , mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            mStartForeground = mStopForeground = null;
        }

        try {
            mSetForeground = getClass().getMethod( "setForeground", mSetForegroundSignature);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException( "OS doesn't have Service.startForeground OR Service.setForeground!");
        }

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_app)
                .setContentTitle("SuperTest")
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentText("Performs testing running..");
        Notification notification = builder.build();
        startForegroundCompat(NOTIFICATION_ID, notification);
        ToastHelper.addToast("请耐心等待，环境已准备，即将执行", this);

        // 执行检测Uiautomator有无运行，没有则停止服务的定时器
        checkStopTheServiceOrNotTask = new TimerTask() {
            @Override
            public void run() {

                if(TextUtils.isEmpty(ShellUtils.execCommand("ps|grep uiautomator", false, true).successMsg) && isSendComplete){
                    if(PublicMethod.isServiceWorked(PerformsTestService.this, "com.meizu.testdevVideo.service.MonkeyProcessService")){
                        Intent intent = new Intent(PerformsTestService.this, MonkeyProcessService.class);
                        stopService(intent);
                    }

                    saveLogLocal("检测不到运行的Uiautomator任务，正常停止运行");
                    stopSelf();
                }

            }
        };

        // 定时检测是否没有uiautomator进程了，没有的话就杀掉服务
        mTimer.schedule(checkStopTheServiceOrNotTask, checkUiautomatorRunOrNot, checkUiautomatorRunOrNot);

        PerformsReceiver.getInstance().setOnCaseCompleteListener(new PerformsCaseCompleteCallBack() {
            @Override
            public void onCaseComplete() {
                isSendComplete = false;
                Log.d(TAG, "案例测试完成，可以发报告啦！");
                saveLogLocal("案例测试完成，可以发报告啦！");
                AjaxParams params = GetPerformsParams.getInstance().getPerformsParamsByPre(getApplicationContext());
                String testType = getPreferenceValue(iPerformsKey.testType, false);
                String fileName = getPreferenceValue(iPerformsKey.stepValueFileName, false);
                String filePath = "";    // 生成的文件路径
                String result = getPreferenceValue(iPerformsKey.result, true);
                saveLogLocal("案例执行结果：" + result);
                String caseName = getPreferenceValue(iPerformsKey.caseName, true);
                if("framerate".equals(testType)){
                    filePath = iPublic.PERFORMS_FPS_RESULT + fileName;
                }else if("memory".equals(testType)){
                    filePath = iPublic.PERFORMS_MEMORY_RESULT + fileName;
                }else if("starttime".equals(testType)){
                    filePath = iPublic.PERFORMS_TIME_RESULT + fileName;
                }else if("purebackstage".equals(testType)){
                    filePath = PublicMethod.getFileDirNewDataPath(iPublic.PERFORMS_PURE_BACKGROUND_RESULT) + "/dumpsys/dumpsys_batterystats";
                }else{
                    saveLogLocal("发送报告，该测试类型不支持");
                    ToastHelper.addToast("发送报告，该测试类型不支持", getApplicationContext());
                }

                PerformsData.getInstance(getApplicationContext()).writeStringData(iPerformsKey.stepValueFilePath, filePath);

                if(result.contains("true")){
                    if(!TextUtils.isEmpty(filePath)){
                        String report = PublicMethod.readFile(filePath);
                        if(!TextUtils.isEmpty(report)){
                            report = report.replace("\"", "");
                        }else{
                            report = "本地路径找不到报告！";
                        }
                        params.put("\"" + iPerformsKey.stepValue + "\"", "\"" + report + "\"");
                        saveLogLocal("报告路径为：" + caseName + "\n" + filePath);
                    }else{
                        saveLogLocal("抱歉，找不到报告，也许是测试类型不支持，请联系maxueming@meizu.com添加");
                        params.put("\"" + iPerformsKey.stepValue + "\"", "\"抱歉，找不到报告，也许是测试类型不支持，请联系maxueming@meizu.com添加\"");
                    }
                }else{
                    saveLogLocal("报告路径为：" + caseName + "\n案例执行失败，故没有报告");
                    params.put("\"" + iPerformsKey.stepValue + "\"", "\"案例执行过程失败\"");
                }

                params.put("\"" + iPerformsKey.packageName + "\"", getPreferenceValue(iPerformsKey.packageName, true));
                params.put("\"" + iPerformsKey.caseName + "\"", caseName);
                params.put("\"" + iPerformsKey.result + "\"", result);

                saveLogLocal("WIFI是否连接：" + String.valueOf(WifiUtil.isWifiConnected(PerformsTestService.this)));

                // 发送报告，回调接口，判断是否发送报告结束，方便停止测试
                PerformsReportSend.getInstance().sendReport(getApplicationContext(), params, new ReportSendCallBack() {
                    @Override
                    public void isSendComplete(boolean isComplete) {
                        isSendComplete = isComplete;
                    }
                });
            }
        });
    }


    /**
     * 获取PreferenceValue数据
     * @param key
     * @return
     */
    private String getPreferenceValue(String key, boolean add){
        if(add){
            return ("\"" + PerformsData.getInstance(getApplicationContext()).readStringData(key) + "\"");
        }else{
            return (PerformsData.getInstance(getApplicationContext()).readStringData(key));
        }

    }


    /**
     * 保存服务LOG到本地
     * @param log
     */
    private void saveLogLocal(String log){
        PublicMethod.saveStringToFileWithoutDeleteSrcFile("\n" + PublicMethod.getSystemTime() + log,
                "Performs_Log", iPublic.LOCAL_MEMORY + "SuperTest/ApkLog/");
    }


    /**
     * 悬浮窗口初始化
     */
    public void Window_Init(){
        saveLogLocal("设置悬浮按钮");
        startView = LayoutInflater.from(this).inflate(R.layout.performs_running, null);   // 获取开始执行视图
        txt_test_type = (TextView) startView.findViewById(R.id.txt_test_type);
        windowManagerHelper.createView(startView, WindowManager.LayoutParams.WRAP_CONTENT, true, false);   // 加载视图
    }


    /**
     * 判断是否成功下载JAR包的定时器
     */
    TimerTask mJarTimeTask = new TimerTask() {
        @Override
        public void run() {
            File file = new File(iPublic.PERFORMS_TESTCASE_PATH + iPublic.PERFORMS_JAR_NAME);
            if(!file.exists()){
                // 下载jar包
                strDownloadJarId = DownloadHelper.getInstance(getApplicationContext())
                        .download(iPublic.PERFORMS_TESTCASE_DOWNLOAD_URL,
                        "/SuperTest/PerformsTest/TestCase/", iPublic.PERFORMS_JAR_NAME);
            }else{
                if (mJarTimeTask != null){
                    mJarTimeTask.cancel();
                    mJarTimeTask = null;
                }
            }
        }
    };


    // 停止定时器
    private void stopTimer(){
        if (mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
        if (mFpsTimeTask != null){
            mFpsTimeTask.cancel();
            mFpsTimeTask = null;
        }
        if (checkStopTheServiceOrNotTask != null){
            checkStopTheServiceOrNotTask.cancel();
            checkStopTheServiceOrNotTask = null;
        }
        if (mJarTimeTask != null){
            mJarTimeTask.cancel();
            mJarTimeTask = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CommonVariable.isPerformsStart = false;
        saveLogLocal("服务被销毁了");
        stopTimer();
        PerformsData.getInstance(this).writeBooleanData(iPerformsKey.isStart, false);
        if(null != windowManagerHelper){
            windowManagerHelper.removeView(startView);
        }
        stopForegroundCompat(NOTIFICATION_ID);
        unregisterReceiver(PerformsReceiver.getInstance());
    }

    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    void startForegroundCompat(int id, Notification notification) {
        if (mReflectFlg) {
            // If we have the new startForeground API, then use it.
            if (mStartForeground != null) {
                mStartForegroundArgs[0] = Integer.valueOf(id);
                mStartForegroundArgs[1] = notification;
                invokeMethod( mStartForeground, mStartForegroundArgs);
                return;
            }

            // Fall back on the old API.
            mSetForegroundArgs[0] = Boolean. TRUE;
            invokeMethod( mSetForeground, mSetForegroundArgs);
            mNM.notify(id, notification);
        } else {
           /*
           * 还可以使用以下方法，当 sdk大于等于5时，调用sdk现有的方法startForeground设置前台运行，
           * 否则调用反射取得的 sdk level 5（对应Android 2.0）以下才有的旧方法setForeground设置前台运行
           */
            if (Build.VERSION. SDK_INT >= 5) {
                startForeground(id, notification);
            } else {
                // Fall back on the old API.
                mSetForegroundArgs[0] = Boolean. TRUE;
                invokeMethod( mSetForeground, mSetForegroundArgs);
                mNM.notify(id, notification);
            }
        }
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    void stopForegroundCompat(int id) {
        if (mReflectFlg) {
            // If we have the new stopForeground API, then use it.
            if ( mStopForeground != null) {
                mStopForegroundArgs[0] = Boolean. TRUE;
                invokeMethod( mStopForeground, mStopForegroundArgs);
                return;
            }

            // Fall back on the old API. Note to cancel BEFORE changing the
            // foreground state, since we could be killed at that point.
            mNM.cancel(id);
            mSetForegroundArgs[0] = Boolean. FALSE;
            invokeMethod(mSetForeground, mSetForegroundArgs);
        } else {
           /*
           * 还可以使用以下方法，当 sdk大于等于5时，调用 sdk现有的方法stopForeground停止前台运行， 否则调用反射取得的 sdk
           * level 5（对应Android 2.0）以下才有的旧方法setForeground停止前台运行
           */
            if (Build.VERSION.SDK_INT >= 5) {
                stopForeground(true);
            } else {
                // Fall back on the old API. Note to cancel BEFORE changing the
                // foreground state, since we could be killed at that point.
                mNM.cancel(id);
                mSetForegroundArgs[0] = Boolean. FALSE;
                invokeMethod(mSetForeground, mSetForegroundArgs);
            }
        }
    }

    void invokeMethod(Method method, Object[] args) {
        try {
            method.invoke(this, args);
        } catch (InvocationTargetException e) {
            Log.w("ApiDemos", "Unable to invoke method", e);
        } catch (IllegalAccessException e) {
            Log.w("ApiDemos", "Unable to invoke method", e);
        }
    }
}
