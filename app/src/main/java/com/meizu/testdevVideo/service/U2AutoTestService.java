package com.meizu.testdevVideo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.meizu.aidl.IU2AutoTestAidl;

import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.broadcast.PerformsReceiver;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.AlarmSetting;
import com.meizu.testdevVideo.library.ServiceNotificationHelper;

import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.library.apkController.ApkControllerUtils;
import com.meizu.testdevVideo.push.android.MPush;
import com.meizu.testdevVideo.task.beans.PerformsPostResultBean;
import com.meizu.testdevVideo.task.beans.PerformsPostStatusBean;
import com.meizu.testdevVideo.task.performs.PerformsResultAnalysis;
import com.meizu.testdevVideo.task.performs.U2AutoTestTaskCallBack;
import com.meizu.testdevVideo.task.performs.U2TaskPreference;
import com.meizu.testdevVideo.task.performs.U2TaskUtils;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.download.DownloadHelper;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.sharepreference.SettingPreference;
import com.meizu.widget.floatingwindow.ViewManager;

import java.io.File;
import java.io.IOException;

public class U2AutoTestService extends Service implements U2AutoTestTaskCallBack{
    private int NOTIFICATION_ID = 103;
    private ViewManager manager;
    private long taskNumber;
    private long currentTaskNumber;
    private boolean isU2TaskMainApkInstall;
    private boolean isU2TaskAndroidTestApkInstall;
    private String u2TaskClassName;
    private int taskId;
    private String taskName;
    private static final int UPDATE_U2TASK_PROGRESS = 100;
    private static final int U2TASK_RUN_FINISH = 200;
    private PerformsPostResultBean performsPostResultBean;
    private boolean isWakeUp = false;   // 存储是否屏幕唤醒
    private PureBean pureBean;
    private String packageName;

    public static void startU2TaskService(Context context) {
        Intent intent = new Intent(context, U2AutoTestService.class);
        context.stopService(intent);
        context.startService(intent);
    }

    public static void stopU2TaskService(Context context) {
        Intent intent = new Intent(context, U2AutoTestService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceNotificationHelper.getInstance(this).notification(NOTIFICATION_ID,
                this, "性能测试", "U2 task running..");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.U2TaskConstants.U2_TASK_BROADCAST_ACTION);
        intentFilter.addAction(Constants.U2TaskConstants.U2_TASK_APK_INSTALL_SUCCESS_BROADCAST_ACTION);
        intentFilter.addAction(Constants.U2TaskConstants.U2_TASK_ALARM_TIME_FINISH);
        intentFilter.addAction(Constants.U2TaskConstants.U2_TASK_STOP_TASK);
        registerReceiver(PerformsReceiver.getInstance(), intentFilter);
        PerformsReceiver.getInstance().setOnU2CallBack(this);
        manager = ViewManager.getInstance(SuperTestApplication.getContext());
        manager.showFloatBall();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pureBean = new PureBean();
        isWakeUp = SettingPreference.getInstance(SuperTestApplication.getContext()).isWakeUp();
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        killProcess();              // 关闭服务，同时杀掉U2TASK进程
        U2TaskPreference.setU2TaskStatus(SuperTestApplication.getContext(), false);
        SettingPreference.getInstance(SuperTestApplication.getContext()).setWakeUp(isWakeUp);
        ServiceNotificationHelper.getInstance(this).notificationCancel(this, NOTIFICATION_ID);
        // 隐藏各个悬浮窗
        manager.hideProgressBar();
        manager.hideFloatBall();
        manager.hideTaskList();
        manager.cancelCallBackHandler();
        unregisterReceiver(PerformsReceiver.getInstance());
    }

    // 收到任务，执行
    private void runU2TaskLocal(String taskJson, int taskId){
        isWakeUp = SettingPreference.getInstance(SuperTestApplication.getContext()).isWakeUp();
        // 清除测试结果数据
        boolean isChooseAppType = isSetPackageName();
        if(isChooseAppType){
            manager.hideFloatBall();
            manager.hideTaskList();
            manager.showTaskProgressBar();
            U2TaskPreference.setU2TaskStatus(SuperTestApplication.getContext(), true);   //任务标志位
            SettingPreference.getInstance(SuperTestApplication.getContext()).setWakeUp(true);
            PublicMethod.wakeUpAndUnlock(SuperTestApplication.getContext());
            U2TaskUtils.clearTestData();
            initParma(taskId);
            initData();
            JSONArray taskJsonArray = JSONArray.parseArray(taskJson);
            taskNumber = taskJsonArray.size();
            for(int i = 0; i < taskNumber; i++){
                if(i == taskNumber - 1){
                    u2TaskClassName = u2TaskClassName + taskJsonArray.get(i);
                }else{
                    u2TaskClassName = u2TaskClassName + taskJsonArray.get(i) + ",";
                }
            }
            Logger.file("收到测试任务id = " + String.valueOf(taskId) + "，案例数量为：" + String.valueOf(taskNumber), Logger.U2TASK);
            downloadApk();
        }else{
            SettingPreference.getInstance(SuperTestApplication.getContext()).setWakeUp(isWakeUp);
            ToastHelper.addToast("未设置业务，下发任务失败", SuperTestApplication.getContext());
            Logger.file("未选择业务，下发任务失败", Logger.U2TASK);
        }

    }

    // 判断是否已经设置包名
    private boolean isSetPackageName(){
        return !TextUtils.isEmpty(packageName = BaseData.getInstance(getApplicationContext()).readStringData(SettingPreferenceKey.MONKEY_PACKAGE));
    }

    private void initData(){
        performsPostResultBean = new PerformsPostResultBean();
        performsPostResultBean.setDeviceType(PerformsData.getInstance(getApplicationContext()).readStringData(iPerformsKey.deviceType));
        performsPostResultBean.setImei(PerformsData.getInstance(getApplicationContext()).readStringData(iPerformsKey.imei));
        performsPostResultBean.setAppType(BaseData.getInstance(getApplicationContext()).readStringData(SettingPreferenceKey.APP_TYPE));
        performsPostResultBean.setAppVersion(ApkControllerUtils.getPackageVersion(getApplicationContext(), packageName));
        performsPostResultBean.setSystemVersion(PerformsData.getInstance(getApplicationContext()).readStringData(iPerformsKey.systemVersion));
        performsPostResultBean.setBaseBand(PerformsData.getInstance(getApplicationContext()).readStringData(iPerformsKey.baseBand));
        performsPostResultBean.setKernel(PerformsData.getInstance(getApplicationContext()).readStringData(iPerformsKey.kernel));
        performsPostResultBean.setPkg(packageName);
        performsPostResultBean.setType(Constants.U2TaskConstants.U2_TASK_TYPE_OF_SEND_REPORT);
        performsPostResultBean.setTaskId(String.valueOf(taskId));
        performsPostResultBean.setTask(Constants.MpushTaskLabel.START_PERFORMS_TEST);
    }

    /**
     * 下载APK
     */
    private void downloadApk(){
        File u2ApkFile = new File(iPublicConstants.LOCAL_MEMORY + "/SuperTest/PerformsTest/TestCase/Test.apk");
        File u2ApkAndroidTestFile = new File(iPublicConstants.LOCAL_MEMORY + "/SuperTest/PerformsTest/TestCase/TestAndroidTest.apk");
        if(u2ApkFile.exists()){
            u2ApkFile.delete();
        }
        if(u2ApkAndroidTestFile.exists()){
            u2ApkAndroidTestFile.delete();
        }
        CommonVariable.strU2ApkId = DownloadHelper.getInstance(U2AutoTestService.this)
                .download("http://172.16.177.71/repos/repos/multimedia_apk/app/" +
                        "build/outputs/apk/app-debug.apk", "/SuperTest/PerformsTest/TestCase/", "Test.apk");
        CommonVariable.strU2AndroidTestApkId = DownloadHelper.getInstance(U2AutoTestService.this)
                .download("http://172.16.177.71/repos/repos/multimedia_apk/app/build/" +
                        "outputs/apk/app-debug-androidTest-unaligned.apk", "/SuperTest/PerformsTest/TestCase/", "TestAndroidTest.apk");
        taskName = "下载Apk..";
        handler.sendEmptyMessage(UPDATE_U2TASK_PROGRESS);
    }

    /**
     * 参数初始化
     */
    private void initParma(int taskId){
        this.taskId = taskId;
        currentTaskNumber = 0;
        isU2TaskAndroidTestApkInstall = false;
        isU2TaskMainApkInstall = false;
        u2TaskClassName = "";
        taskName = "";
    }

    /**
     * 结束任务处理函数
     */
    private void finishU2Test(){
        manager.hideTaskList();
        manager.hideProgressBar();
        manager.showFloatBall();
        U2TaskPreference.setU2TaskStatus(SuperTestApplication.getContext(), false);
        SettingPreference.getInstance(SuperTestApplication.getContext()).setWakeUp(isWakeUp);
    }

    private void runU2Task(String className){
        try {
            Runtime.getRuntime().exec(String.format(Constants.U2TaskConstants.U2_TASK_COMMAND, className));
        } catch (IOException e) {
            Logger.e("执行U2任务出错==>" + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 主进程函数
     */
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case UPDATE_U2TASK_PROGRESS:
                    manager.setProgressBar(taskName + "  " + "任务总数：" + String.valueOf(taskNumber), currentTaskNumber, taskNumber);
                    if(taskNumber == currentTaskNumber){
                        handler.sendEmptyMessage(U2TASK_RUN_FINISH);
                    }
                    break;
                case U2TASK_RUN_FINISH:
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    postTaskStatus(iPerformsKey.taskFinishStatus, String.valueOf(taskId));
                    finishU2Test();
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * U2AutoTestService接口
     */
    private Binder binder = new IU2AutoTestAidl.Stub(){

        // 发送报告，并更新任务进度
        @Override
        public void sendReport(String caseName, String className, String steps,
                               String expectation, boolean result, String testTime, String fromPage, String exception) throws RemoteException {
            dealPeport(caseName, className, steps, expectation, result, testTime, fromPage, exception);
        }

        @Override
        public void setSleepAlarmTask(long alarmTime, boolean isWakeUp) throws RemoteException {
            Logger.d("设置休眠时间==>" + String.valueOf(alarmTime));
            Logger.file("纯净后台，设置休眠", Logger.U2TASK);
            SettingPreference.getInstance(SuperTestApplication.getContext()).setWakeUp(isWakeUp);
            PublicMethod.wakeUpAndUnlock(SuperTestApplication.getContext());
            AlarmSetting.getInstance().setOnceAlarm(SuperTestApplication.getContext(),
                    Constants.U2TaskConstants.U2_TASK_ALARM_TIME_FINISH, System.currentTimeMillis() + alarmTime);
        }
    };

    /**
     * 处理报告
     */
    private void dealPeport(String caseName, String className, String steps,
                            String expectation, boolean result, String testTime, String fromPage, String exception){
        if(-1 != this.taskId){
            Logger.d("收到测试案例结束的上报==>" + caseName);
            if(null != className && !className.contains("purebackstage")){
                send(caseName, className, steps, expectation, result, testTime, exception);
            }else if(result && null != className && className.contains(iPerformsKey.purebackstage) && fromPage.equals("u2Task")){
                // 结果正确，暂存值
                pureBean.clear();
                pureBean.setPureCaseName(caseName);
                pureBean.setPureClassName(className);
                pureBean.setPureSteps(steps);
                pureBean.setPureExpectation(expectation);
                pureBean.setPureTestTime(testTime);
                pureBean.setPureException(exception);
                Logger.file("纯净后台，暂存pureBean值", Logger.U2TASK);
            }else if(!result && null != className && className.contains(iPerformsKey.purebackstage) && fromPage.equals("u2Task")){
                // 结果不正确，直接发送
                send(caseName, className, steps, expectation, false, testTime, exception);
            }else if(fromPage.equals("alarm")){
                Logger.file("纯净后台，闹钟时间到达可以发送了", Logger.U2TASK);
                send(pureBean.getPureCaseName(), pureBean.getPureClassName(), pureBean.getPureSteps(),
                        pureBean.getPureExpectation(), true, pureBean.getPureTestTime(), pureBean.getPureException());
            }
        }
    }

    /**
     * 发送报告
     */
    private void send(String caseName, String className, String steps,
                      String expectation, boolean result, String testTime, String exception){
        performsPostResultBean.setTestTime(testTime);
        performsPostResultBean.setTestType(U2TaskUtils.getTestType(className));
        performsPostResultBean.setResult(String.valueOf(result));
        performsPostResultBean.setCaseStep(steps);
        if(result){
            if(className.contains(iPerformsKey.purebackstage)){
                String filePath = PublicMethod.getFileDirNewDataPath(iPublicConstants.PERFORMS_PURE_BACKGROUND_RESULT);
                if(filePath != null){
                    filePath = filePath + "/dumpsys/dumpsys_batterystats";
                    String text = PublicMethod.readFile(filePath);
                    if(null != text && text.contains("Proc " + packageName)){
                        int startNum = text.indexOf("Proc " + packageName);
                        text = text.substring(startNum - 280, startNum + 230);
                        try {
                            text = PerformsResultAnalysis.getClearBackGround(text);
                            Logger.file("纯净后台文件解析成功", Logger.U2TASK);
                        }catch (Exception e){
                            Logger.file("解析纯净后台文件失败", Logger.U2TASK);
                            text = " ";
                        }
                    }else{
                        Logger.file("日志中找不到Proc_packageName", Logger.U2TASK);
                        text = " ";
                    }
                    performsPostResultBean.setResultFile(text);
                }else{
                    Logger.file("找不到纯净后台文件", Logger.U2TASK);
                    performsPostResultBean.setResultFile("");
                }
            }else{
                performsPostResultBean.setResultFile(PublicMethod.readFile(U2TaskUtils
                        .getTestTypeResultPath(className) + caseName + ".txt"));
            }
        }else{
            performsPostResultBean.setResultFile("");
        }

        performsPostResultBean.setException(exception);
        performsPostResultBean.setExpectation(expectation);
        // 发送报告
        MPush.I.sendPush(JSON.toJSONString(performsPostResultBean).getBytes(Constants.UTF_8));
        Logger.d(JSON.toJSONString(performsPostResultBean));
        currentTaskNumber++;
        handler.sendEmptyMessage(UPDATE_U2TASK_PROGRESS);
    }

    /**
     * 上报任务状态
     */
    private void postTaskStatus(String status, String taskId){
        PerformsPostStatusBean performsPostStatusBean = new PerformsPostStatusBean();
        performsPostStatusBean.setTaskId(taskId);
        performsPostStatusBean.setStatus(status);
        performsPostStatusBean.setTaskType("PerformsTestStatus");
        performsPostStatusBean.setTestType("u2Task");
        performsPostStatusBean.setTask(Constants.MpushTaskLabel.POST_TASK_RUNNING_STATU);
        Logger.d("上报U2Task任务状态的JSON串==>" + JSON.toJSONString(performsPostStatusBean));
        MPush.I.sendPush(JSON.toJSONString(performsPostStatusBean).getBytes(Constants.UTF_8));
        this.taskId = -1;     // 重置任务状态，防止本地调试干扰
    }

    private void killProcess(){
        try {
            Runtime.getRuntime().exec("am force-stop com.meizu.testdev.multimedia");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void runU2Task(String taskJson, int taskId) {
        runU2TaskLocal(taskJson, taskId);
    }

    @Override
    public void installApkFinish(String id, boolean isSuccess) {
        if(id.equals(CommonVariable.strU2ApkId)){
            isU2TaskMainApkInstall = isSuccess;
            Logger.file("安装TestApk", Logger.U2TASK);
            taskName = "安装TestApk成功！";
            handler.sendEmptyMessage(UPDATE_U2TASK_PROGRESS);
        }

        if(id.equals(CommonVariable.strU2AndroidTestApkId)){
            isU2TaskAndroidTestApkInstall = isSuccess;
            Logger.file("安装AndroidTestApk", Logger.U2TASK);
            taskName = "安装AndroidTestApk成功！";
            handler.sendEmptyMessage(UPDATE_U2TASK_PROGRESS);
        }

        // 安装完毕APK。执行案例
        if(isU2TaskMainApkInstall && isU2TaskAndroidTestApkInstall){
            Logger.file("安装完毕APK，开始执行案例", Logger.U2TASK);
            runU2Task(u2TaskClassName);
            taskName = "执行中..";
            handler.sendEmptyMessage(UPDATE_U2TASK_PROGRESS);
        }
    }

    @Override
    public void alarmFinish() {
        Logger.file("闹钟时间到达，回调接口alarmFinish", Logger.U2TASK);
        dealPeport(null, null, null, null, true, null, "alarm", null);
    }

    @Override
    public void stopTask() {
        killProcess();
        postTaskStatus(iPerformsKey.taskFailStatus, String.valueOf(taskId));
        finishU2Test();
    }
}


/**
 * 纯净后台暂存变量
 */
class PureBean{

    private String pureCaseName, pureClassName, pureSteps, pureExpectation, pureTestTime, pureException;

    public void clear(){
        pureCaseName = null;
        pureClassName = null;
        pureSteps = null;
        pureExpectation = null;
        pureTestTime = null;
        pureException = null;
    }

    public String getPureCaseName() {
        return pureCaseName;
    }

    public void setPureCaseName(String pureCaseName) {
        this.pureCaseName = pureCaseName;
    }

    public String getPureClassName() {
        return pureClassName;
    }

    public void setPureClassName(String pureClassName) {
        this.pureClassName = pureClassName;
    }

    public String getPureSteps() {
        return pureSteps;
    }

    public void setPureSteps(String pureSteps) {
        this.pureSteps = pureSteps;
    }

    public String getPureExpectation() {
        return pureExpectation;
    }

    public void setPureExpectation(String pureExpectation) {
        this.pureExpectation = pureExpectation;
    }

    public String getPureTestTime() {
        return pureTestTime;
    }

    public void setPureTestTime(String pureTestTime) {
        this.pureTestTime = pureTestTime;
    }

    public String getPureException() {
        return pureException;
    }

    public void setPureException(String pureException) {
        this.pureException = pureException;
    }
}
