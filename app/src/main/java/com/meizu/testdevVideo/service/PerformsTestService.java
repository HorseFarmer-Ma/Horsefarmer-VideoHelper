package com.meizu.testdevVideo.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.PerformsCaseCompleteCallBack;
import com.meizu.testdevVideo.interports.PerformsJarDownloadCallBack;
import com.meizu.testdevVideo.broadcast.PerformsReceiver;
import com.meizu.testdevVideo.constant.GetPerformsParams;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.PostUploadHelper;
import com.meizu.testdevVideo.library.ServiceNotificationHelper;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.library.WindowManagerHelper;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.download.DownloadHelper;
import com.meizu.testdevVideo.util.download.DownloadReceiver;
import com.meizu.testdevVideo.util.sendReport.PerformsReportSend;
import com.meizu.testdevVideo.util.sendReport.ReportSendCallBack;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.shell.ShellUtils;
import com.meizu.testdevVideo.util.task.PerformsPushTaskMethod;
import com.meizu.testdevVideo.util.wifi.WifiUtil;

import net.tsz.afinal.http.AjaxParams;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PerformsTestService extends Service {

    private String TAG = PerformsTestService.class.getSimpleName();
    private View startView;
    private WindowManagerHelper windowManagerHelper = null;
    private TextView txt_test_type;
    private Timer mTimer;
    private String strDownloadJarId;
    private TimerTask checkStopTheServiceOrNotTask;
    private TimerTask mJarTimeTask;
    private boolean isSendComplete = false;

    // 任务类型：0 本地任务 | 1 云端任务
    private int taskType;
    // 任务主Id
    private String taskId;
    // 任务总数
    private int starttime_task_number = 0;
    private int framerate_task_number = 0;
    private int memory_task_number = 0;
    private int purebackstage_task_number = 0;
    // 任务执行到的个数
    private int starttime_task_run_number = 0;
    private int framerate_task_run_number = 0;
    private int memory_task_run_number = 0;
    private int purebackstage_task_run_number = 0;
    // 执行的类名+方法名
    private String doPackageName = null;
    // 任务Json，由push推送收到广播后传过来
    private JSONObject taskPushJson = null;
    // 存储任务状态
    private Map<String, String> taskStatusParams = new HashMap<String, String>();

    // 如果id设置为0,会导致不能设置为前台
    private static final int NOTIFICATION_ID = 101;
    private SharedPreferences.Editor editor = null;
    private SharedPreferences settingSharedPreferences = null;



    @Override
    public void onCreate(){
        super.onCreate();
        broadcastInit();
        onCreateInit();
        ServiceNotificationHelper.getInstance(this).notification(NOTIFICATION_ID,
                this, "性能测试", "性能测试执行中...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            jpushTaskInit(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        onStartCommandInit();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CommonVariable.isPerformsStart = false;
        saveLogLocal("服务被销毁了");
        keepWakeUp(false);   // 关闭屏幕唤醒
        stopTimer();
        PerformsData.getInstance(this).writeBooleanData(iPerformsKey.isStart, false);
        if(null != windowManagerHelper){
            windowManagerHelper.removeView(startView);
        }
        checkStatusFinally();
        ServiceNotificationHelper.getInstance(this).notificationCancel(this, NOTIFICATION_ID);
        unregisterReceiver(PerformsReceiver.getInstance());
    }

    /**
     * onCreate初始化
     */
    private void onCreateInit(){
        mTimer = new Timer();
        CommonVariable.isPerformsStart = true;
        PerformsData.getInstance(this).writeBooleanData(iPerformsKey.isStart, true);
        Window_Init();

    }

    /**
     * onStartCommand初始化
     */
    private void onStartCommandInit(){
        PerformsData.getInstance(this).writeBooleanData(iPerformsKey.isPurebackstageSet, false);
        String title = getResources().getString(R.string.performs_test_type)
                + PerformsData.getInstance(this).readStringData(iPerformsKey.testType);
        txt_test_type.setText(title);
        downLoadJar();
        sendResultInit();
    }


    /**---------------------- 编写整合完毕 ---------------------------*/

    /**
     * Post Task status
     */
    private void postTaskStatus() throws MalformedURLException {
        String testType = PerformsData.getInstance(this).readStringData(iPerformsKey.testType);   // 当前测试类型
        String status = null;
        if(testType.equals(iPerformsKey.starttime) && starttime_task_number !=0){
            status = (starttime_task_run_number < starttime_task_number)? iPerformsKey.taskRunStatus : iPerformsKey.taskFinishStatus;
        }

        if(testType.equals(iPerformsKey.framerate) && framerate_task_number != 0){
            status = (framerate_task_run_number < framerate_task_number)? iPerformsKey.taskRunStatus : iPerformsKey.taskFinishStatus;
        }

        if(testType.equals(iPerformsKey.memory) && memory_task_number != 0){
            status = (memory_task_run_number < memory_task_number)? iPerformsKey.taskRunStatus : iPerformsKey.taskFinishStatus;
        }

        if(testType.equals(iPerformsKey.purebackstage) && purebackstage_task_number != 0){
            status = (purebackstage_task_run_number < purebackstage_task_number)? iPerformsKey.taskRunStatus : iPerformsKey.taskFinishStatus;
        }

        sendTaskStatus(taskId, testType, status, "0");
    }

    /**
     * 服务销毁时，检查任务状态并查看是否需要上报
     */
    private void checkStatusFinally(){
        if(1 == taskType){
            if(starttime_task_run_number != starttime_task_number){
                try {
                    sendTaskStatus(taskId, iPerformsKey.starttime, iPerformsKey.taskFailStatus, "1");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            if(framerate_task_run_number != framerate_task_number){
                try {
                    sendTaskStatus(taskId, iPerformsKey.framerate, iPerformsKey.taskFailStatus, "1");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            if(memory_task_run_number != memory_task_number){
                try {
                    sendTaskStatus(taskId, iPerformsKey.memory, iPerformsKey.taskFailStatus, "1");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            if(purebackstage_task_run_number != purebackstage_task_number){
                try {
                    sendTaskStatus(taskId, iPerformsKey.purebackstage, iPerformsKey.taskFailStatus, "1");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }

            // 全部任务执行完毕，则发送执行完毕的命令
            if(starttime_task_run_number == starttime_task_number && framerate_task_run_number == framerate_task_number
                    && memory_task_run_number == memory_task_number && purebackstage_task_run_number == purebackstage_task_number){
                try {
                    sendTaskStatus(taskId, iPerformsKey.purebackstage, iPerformsKey.taskFinishStatus, "1");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *
     * 发送任务状态函数
     * @param taskId 总任务ID
     * @param testType 测试类型
     * @param status 单个任务状态
     * @param allTaskState 是否完成全部任务
     * @throws MalformedURLException
     */
    private void sendTaskStatus(String taskId, String testType, String status, String allTaskState) throws MalformedURLException {
        taskStatusParams.clear();    // 清除之前字段存储信息
        taskStatusParams.put(iPerformsKey.taskId, taskId);
        taskStatusParams.put(iPerformsKey.testType, testType);
        taskStatusParams.put(iPerformsKey.status, status);
        taskStatusParams.put(iPerformsKey.allTaskState, allTaskState);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PostUploadHelper.getInstance().submitPostData(iPublicConstants.PERFORMS_POST_TASK_STATUS_URL, taskStatusParams, new PostCallBack() {
                        @Override
                        public void resultCallBack(boolean isSuccess, int resultCode, String data) {
                            Log.d(TAG, "isSuccess：" + isSuccess);
                            Log.d(TAG, "resultCode：" + resultCode);
                            Log.d(TAG, "result：" + data);
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 云端推送任务初始化
     * @param intent 传送的intent值
     */
    private void jpushTaskInit(Intent intent) throws JSONException {
        if(null != intent){
            taskType = intent.getIntExtra("taskType", 0);
            switch (taskType){
                case 0:      // 本地任务
                    saveLogLocal("收到本地任务");
                    doPackageName = PerformsData.getInstance(getApplicationContext()).readStringData(iPerformsKey.doPackageName);
                    break;
                case 1:      // 云端任务，写进测试包名，测试类型
                    saveLogLocal("收到云端任务");
                    taskPushJson = new JSONObject(intent.getStringExtra(iPerformsKey.taskPushJson));
                    // 预先初始化
                    PerformsData.getInstance(this).writeStringData(iPerformsKey.testTime,
                            String.valueOf(System.currentTimeMillis()));

                    String appType = PerformsPushTaskMethod.getAppType(taskPushJson);
                    PerformsData.getInstance(this).writeStringData(iPerformsKey.appType, appType);
                    saveLogLocal("测试应用类型：" + appType);

                    String appVersion = PublicMethod.getAppVersion(this, PerformsPushTaskMethod.getAppType(taskPushJson));
                    PerformsData.getInstance(this).writeStringData(iPerformsKey.appVersion, appVersion);
                    saveLogLocal("测试应用版本号：" + appVersion);

                    taskId = PerformsPushTaskMethod.getTaskIdFromJson(taskPushJson);
                    saveLogLocal("taskId：" + taskId);
                    starttime_task_number = PerformsPushTaskMethod.getStarttimeNumber(taskPushJson);
                    saveLogLocal("starttime_task_number：" + starttime_task_number);
                    framerate_task_number = PerformsPushTaskMethod.getFramerateNumber(taskPushJson);
                    saveLogLocal("framerate_task_number：" + framerate_task_number);
                    memory_task_number = PerformsPushTaskMethod.getMemoryNumber(taskPushJson);
                    saveLogLocal("memory_task_number：" + memory_task_number);
                    purebackstage_task_number = PerformsPushTaskMethod.getPurebackstageNumber(taskPushJson);
                    saveLogLocal("purebackstage_task_number：" + purebackstage_task_number);
                    chooseTask(false);
                    break;
                default:
                    break;
            }
        }else{
            stopSelf();
        }
    }

    /**
     * 执行测试顺序：启动时间→帧率测试→内存测试→纯净后台
     * 选择任务，首次初始化isRun选择false，与本地进行兼容
     * @param isRun 是否执行
     */
    private void chooseTask(boolean isRun) throws JSONException {
        boolean isDestroyService = false;   // 判断是否销毁服务

        // 任务全部执行完毕，先于下面的判断代码执行，一旦执行完毕，停止服务
        if(purebackstage_task_run_number == purebackstage_task_number && framerate_task_run_number == framerate_task_number
                && starttime_task_run_number == starttime_task_number && memory_task_run_number == memory_task_number){
            isDestroyService = true;
            saveLogLocal("云端任务执行完毕，正常停止服务");
            stopSelf();
        }else if(starttime_task_run_number < starttime_task_number){
            // 检测启动时间任务是否执行完毕
            saveLogLocal("存在启动时间任务，执行条目：" + starttime_task_run_number);
            doPackageName = PerformsPushTaskMethod.getStarttimeCase(taskPushJson)[starttime_task_run_number];
            PerformsData.getInstance(this).writeStringData(iPerformsKey.testType, iPerformsKey.starttime);
            starttime_task_run_number ++;
        }else if(framerate_task_run_number < framerate_task_number && starttime_task_run_number == starttime_task_number){
            // 检测帧率测试任务是否执行完毕
            saveLogLocal("存在帧率测试任务，执行条目：" + framerate_task_run_number);
            doPackageName = PerformsPushTaskMethod.getFramerateCase(taskPushJson)[framerate_task_run_number];
            PerformsData.getInstance(this).writeStringData(iPerformsKey.testType, iPerformsKey.framerate);
            framerate_task_run_number ++;
        }else if(memory_task_run_number < memory_task_number && framerate_task_run_number == framerate_task_number
                && starttime_task_run_number == starttime_task_number){
            // 检测内存测试任务是否执行完毕
            saveLogLocal("存在内存测试任务，执行条目：" + memory_task_run_number);
            doPackageName = PerformsPushTaskMethod.getMemoryCase(taskPushJson)[memory_task_run_number];
            PerformsData.getInstance(this).writeStringData(iPerformsKey.testType, iPerformsKey.memory);
            memory_task_run_number ++;
        }else if(purebackstage_task_run_number < purebackstage_task_number && framerate_task_run_number == framerate_task_number
                && starttime_task_run_number == starttime_task_number && memory_task_run_number == memory_task_number){
            // 检测纯净后台任务是否执行完毕
            saveLogLocal("存在纯净后台任务，执行条目：" + purebackstage_task_run_number);
            doPackageName = PerformsPushTaskMethod.getPurebackstageCase(taskPushJson)[purebackstage_task_run_number];
            PerformsData.getInstance(this).writeStringData(iPerformsKey.testType, iPerformsKey.purebackstage);
            purebackstage_task_run_number ++;
        }

        // 若上述代码没有结束服务，证明存在测试任务，设置悬浮窗标题并继续执行
        if(isRun && !isDestroyService){
            String title = getResources().getString(R.string.performs_test_type)
                    + PerformsData.getInstance(this).readStringData(iPerformsKey.testType);
            Message msg = new Message();
            msg.what = 100;
            msg.obj = title;
            handler.sendMessage(msg);
        }
    }

    // 创建属于主线程的handler
    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    txt_test_type.setText((String)msg.obj);
                    runUiAutomator(doPackageName);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 初始化函数
     */
    private void sendResultInit(){
        PerformsReceiver.getInstance().setOnCaseCompleteListener(new PerformsCaseCompleteCallBack() {
            @Override
            public void onCaseComplete() {
                isSendComplete = false;
                // 如果发现标志位isPurebackstageSet为True，则重置为false
                if(PerformsData.getInstance(PerformsTestService.this).readBooleanData(iPerformsKey.isPurebackstageSet)){
                    PerformsData.getInstance(PerformsTestService.this).writeBooleanData(iPerformsKey.isPurebackstageSet, false);
                }
                Log.d(TAG, "案例测试完成，可以发报告啦！");
                saveLogLocal("案例测试完成，可以发报告啦！");
                AjaxParams params = GetPerformsParams.getInstance().getPerformsParamsByPre(getApplicationContext());
                String testType = getPreferenceValue(iPerformsKey.testType, false);
                String fileName = getPreferenceValue(iPerformsKey.stepValueFileName, false);
                String filePath = "";    // 生成的文件路径
                String result = getPreferenceValue(iPerformsKey.result, true);
                saveLogLocal("案例执行结果：" + result);
                String caseName = getPreferenceValue(iPerformsKey.caseName, true);
                if(iPerformsKey.framerate.equals(testType)){
                    filePath = iPublicConstants.PERFORMS_FPS_RESULT + fileName;
                }else if(iPerformsKey.memory.equals(testType)){
                    filePath = iPublicConstants.PERFORMS_MEMORY_RESULT + fileName;
                }else if(iPerformsKey.starttime.equals(testType)){
                    filePath = iPublicConstants.PERFORMS_TIME_RESULT + fileName;
                }else if(iPerformsKey.purebackstage.equals(testType)){
                    filePath = PublicMethod.getFileDirNewDataPath(iPublicConstants.PERFORMS_PURE_BACKGROUND_RESULT) + "/dumpsys/dumpsys_batterystats";
                }else{
                    saveLogLocal("发送报告，该测试类型不支持");
                    ToastHelper.addToast("发送报告，该测试类型不支持", getApplicationContext());
                }

                PerformsData.getInstance(getApplicationContext()).writeStringData(iPerformsKey.stepValueFilePath, filePath);

                if(result.contains("true")){
                    if(!TextUtils.isEmpty(filePath)){
                        String report = PublicMethod.readFile(filePath);
                        if(testType.equals(iPerformsKey.purebackstage) && !TextUtils.isEmpty(report)){
                            report = report.replace("\"", "");
                            String packageName = getPreferenceValue(iPerformsKey.packageName, false);
                            if(report.contains("Proc " + packageName)){
                                int startNum= report.indexOf("Proc " + packageName);
                                report = report.substring(startNum-280, startNum+230);
                                saveLogLocal("正常获取到日志");
                            }else{
                                saveLogLocal("日志中找不到Proc_packageName");
                                report = "";
                            }
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
                params.put("\"" + iPerformsKey.taskId + "\"", taskId);

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
     * 广播初始化
     */
    private void broadcastInit(){
        IntentFilter performsFilter = new IntentFilter();
        performsFilter.addAction("action.st.performs.test.over");
        performsFilter.addAction("action.st.kill.performs");
        registerReceiver(PerformsReceiver.getInstance(), performsFilter);
    }

    /**
     * 悬浮窗口初始化
     */
    public void Window_Init(){
        saveLogLocal("设置悬浮按钮");
        windowManagerHelper = new WindowManagerHelper(this);
        startView = LayoutInflater.from(this).inflate(R.layout.performs_running, null);   // 获取开始执行视图
        txt_test_type = (TextView) startView.findViewById(R.id.txt_test_type);
        windowManagerHelper.createView(startView, WindowManager.LayoutParams.WRAP_CONTENT, true, false);   // 加载视图
    }

    /**
     * 执行Uiautomator指令
     * @param doTaskClassName 执行的用例
     */
    private void runUiAutomator(String doTaskClassName){
        // 排除执行纯净后台GPRS用例然后没插卡的情况
        if(!doTaskClassName.contains("GprsBlackTest") || PublicMethod.hasSimCard(PerformsTestService.this)){
            final String uiCommand = "/system/bin/sh /data/data/com.meizu.testdevVideo/files/uitest/a5/uiautomator runtest "
                    + iPublicConstants.PERFORMS_TESTCASE_PATH + iPublicConstants.PERFORMS_JAR_NAME
                    + " -c " + doTaskClassName;
            Log.d(TAG, "执行的指令为：\n" + uiCommand);

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
        }else{
            isSendComplete = true;
        }
    }


    /**
     * 下载JAR包
     */
    private void downLoadJar(){
        saveLogLocal("下载JAR包");
        strDownloadJarId = DownloadHelper.getInstance(this).download(iPublicConstants.PERFORMS_TESTCASE_DOWNLOAD_URL,
                "/SuperTest/PerformsTest/TestCase/", iPublicConstants.PERFORMS_JAR_NAME);
        // 设置下载jar包监听定时器
        jarDownLoadCheck();
        // 设置jar包下载监听
        DownloadReceiver.getInstance().setOnPerformsJarDownloadListener(new PerformsJarDownloadCallBack() {
            @Override
            public void onDownLoadComplete(String id, String path){
                if(id.equals(strDownloadJarId)){
                    saveLogLocal("下载JAR包完成");
                    // 杀掉存在的UiAutomator进程
                    PublicMethod.killProcess("ps|grep uiautomator", "system    ", " ");
                    // 下载Jar包完成后，启用定时器，监听是否存在UiAutomator进程
                    UiautomatorTimerInit();
                    // 执行Uiautomator
                    runUiAutomator(doPackageName);
                    PublicMethod.wakeUpAndUnlock(PerformsTestService.this);   // 亮一下屏幕
                }
            }
        });
    }

    /**
     * 初始化UiautomatorTimer定时检测器
     */
    private void UiautomatorTimerInit(){
        // 执行检测Uiautomator有无运行，没有则停止服务的定时器
        checkStopTheServiceOrNotTask = new TimerTask() {
            @Override
            public void run() {
                checkDealUiProcess();
            }
        };
        // 定时检测是否没有uiautomator进程了，没有的话就杀掉服务，15秒检测一下
        // 如果本次任务在15秒内还没起来就可能直接跑到下个任务了
        mTimer.schedule(checkStopTheServiceOrNotTask, 20 * Constants.TIME.SECOND, 15 * Constants.TIME.SECOND);
    }

    /**
     * 检测有无Uiautomator任务
     * 本地的则直接判断并退出，平台的判断有无剩下任务后执行退出
     */
    private void checkDealUiProcess(){
        if(TextUtils.isEmpty(ShellUtils.execCommand("ps|grep uiautomator", false, true).successMsg) && isSendComplete
                && !PerformsData.getInstance(PerformsTestService.this).readBooleanData(iPerformsKey.isPurebackstageSet)){

            switch (taskType){
                case 0:        // 本地任务
                    saveLogLocal("检测不到运行的本地任务，正常停止运行");
                    stopSelf();
                    break;
                case 1:        // 平台下发任务
                    // 检测任务状态并上报
                    try {
                        postTaskStatus();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    // 判断是否还有其他任务没有执行完
                    try {
                        chooseTask(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
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
     * 停止定时器
     */
    private void stopTimer(){
        if (mTimer != null){
            mTimer.cancel();
            mTimer = null;
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

    /**
     * 判断是否成功下载JAR包的定时器
     */
    private void jarDownLoadCheck(){
        mJarTimeTask = new TimerTask() {
            @Override
            public void run() {
                File file = new File(iPublicConstants.PERFORMS_TESTCASE_PATH + iPublicConstants.PERFORMS_JAR_NAME);
                if(!file.exists()){
                    // 下载jar包
                    strDownloadJarId = DownloadHelper.getInstance(getApplicationContext())
                            .download(iPublicConstants.PERFORMS_TESTCASE_DOWNLOAD_URL,
                                    "/SuperTest/PerformsTest/TestCase/", iPublicConstants.PERFORMS_JAR_NAME);
                }else{
                    if (mJarTimeTask != null){
                        mJarTimeTask.cancel();
                        mJarTimeTask = null;
                    }
                }
            }
        };
        mTimer.schedule(mJarTimeTask, 2 * Constants.TIME.MINUTE, 2 * Constants.TIME.MINUTE);
    }

    private void keepWakeUp(boolean isKeepWakeUp){
        settingSharedPreferences = ((settingSharedPreferences ==  null)?
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) : settingSharedPreferences);
        editor = (editor == null) ? settingSharedPreferences.edit() : editor;
        editor.remove(SettingPreferenceKey.KEEP_WAKEUP);
        editor.putBoolean(SettingPreferenceKey.KEEP_WAKEUP, isKeepWakeUp);
        editor.apply();
    }

    /**
     * 保存服务LOG到本地
     * @param log
     */
    private void saveLogLocal(String log){
        PublicMethod.saveStringToFileWithoutDeleteSrcFile("\n" + PublicMethod.getSystemTime() + log,
                "PerformTestLog", iPublicConstants.LOCAL_MEMORY + "SuperTest/ApkLog/");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
