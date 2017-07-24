package com.meizu.testdevVideo.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.broadcast.AlarmManagerReceiver;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;

import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;

import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.PostUploadHelper;
import com.meizu.testdevVideo.library.ServiceNotificationHelper;
import com.meizu.testdevVideo.library.SqlAlterHelper;

import com.meizu.testdevVideo.push.android.MPush;
import com.meizu.testdevVideo.task.monkey.MonkeyUtils;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.FailPostRecordData;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.task.beans.PerformsPostStatusBean;
import com.meizu.testdevVideo.util.wifi.WifiUtil;
import com.meizu.testdevVideo.util.zip.ZipUtil;
import com.meizu.widget.floatingwindow.ViewManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MonkeyService extends Service implements AlarmManagerReceiver.AlarmListener{

    private static final int NOTIFICATION_ID = 102;

    private static final String TAG = "MonkeyService";
    private static final String ACTION_MONKEY = "com.meizu.testdevVideo.service.action.MONKEY";
    private static final String ACTION_RETRY_POST_REPORT = "com.meizu.testdevVideo.service.action.retry.post.report";
    private static final String ACTION_SMART_TEST= "com.meizu.testdevVideo.service.action.smart.test";
    private static final String ACTION_JUST_RUN_MONKEY= "com.meizu.testdevVideo.service.action.just.run.monkey";
    private static final String BROADCAST_KILL_MONKEY = "action.st.kill.monkey";
    private static final String IS_MTK_PHONE = "isMtkPhone";
    private static final String MTK_LOG_PATH = iPublicConstants.LOCAL_MEMORY + "mtklog/mobilelog/";
    private static final String LOGREPORT_PATH = iPublicConstants.PERFORMS_PURE_BACKGROUND_RESULT;
    private static final String MONKEY_LOG_PATH = iPublicConstants.LOCAL_MEMORY + "SuperTest/MonkeyLog/monkey";
    private static final String DIVIDE_LINE = "/";
    private static final String EMAIL = "email";
    private static final String MODULE = "module";
    private static final String MONKEY_FLIE = "file";
    private static final String MONKEY_START_TIME = "start";
    private static final String MONKEY_PACKAGE = "package";
    private static final String MONKEY_MODEL = "model";
    private static final String MONKEY_IMEI = "imei";

    private static final int UPDATE_MONKEY_TASK_PROGRESS = 100;
    private static final int SHOW_TASK_PROGRESS = 200;

    private boolean isU2ServiceOn;
    private ViewManager manager;
    private String text;
    private long runtime;
    private long totalTime;

    // 发送报告相关
    private int tryTimes = 5;       // 发送报告尝试次数
    private Map<String, String> monkeyParams = null;
    private Map<String, File> file = null;
    private boolean isSendOver = false;
    private SharedPreferences settingSharedPreferences = null;
    private SharedPreferences.Editor editor = null;
    private boolean isMtkPhone = true;

    // 是否只跑Monkey
    private boolean isJustRunMonkey = false;

    // Monkey执行相关
    private String monkey_command = null;
    private boolean isStartMonkey;
    private Process p = null;
    private Timer mTimer;
    private long addMonkeyTimes = 10;   // 初始值
    private long lastAddMonkeyTimes = 0;
    private String action = null;
    private Thread runMonkeyThread;


    // 执行Monkey
    public static void startActionMonkeyReport(Context context, boolean isMtkPhone) {
        Intent intent = new Intent(context, MonkeyService.class);
        intent.setAction(ACTION_MONKEY);
        intent.putExtra(IS_MTK_PHONE, isMtkPhone);
        context.stopService(intent);
        context.startService(intent);
    }

    // 重发报告
    public static void startActionRetryPostReport(Context context) {
        Intent intent = new Intent(context, MonkeyService.class);
        intent.setAction(ACTION_RETRY_POST_REPORT);
        context.stopService(intent);
        context.startService(intent);
    }

    // 执行智能遍历
    public static void startActionSmartTest(Context context) {
        Intent intent = new Intent(context, MonkeyService.class);
        intent.setAction(ACTION_SMART_TEST);
        context.stopService(intent);
        context.startService(intent);
    }

    // 执行智能遍历
    public static void startActionJustRunMonkey(Context context) {
        Intent intent = new Intent(context, MonkeyService.class);
        intent.setAction(ACTION_JUST_RUN_MONKEY);
        context.stopService(intent);
        context.startService(intent);
    }

    public static void stopMonkeyService(Context context) {
        Intent intent = new Intent(context, MonkeyService.class);
        context.stopService(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        ServiceNotificationHelper.getInstance(this).
                notification(NOTIFICATION_ID, this, "MonkeyService", "Monkey Task Running...");
        isU2ServiceOn = PublicMethod.isServiceWorked(SuperTestApplication.getContext(),
                    "com.meizu.testdevVideo.service.U2AutoTestService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(null != intent){
            action = intent.getAction();
        }

        /**------------------------------------- 执行Monkey -----------------------------------**/
        if (null != action && ACTION_MONKEY.equals(action)) {
            // 设置回调监听，杀死monkey
            if(null != intent){
                isMtkPhone = intent.getBooleanExtra(IS_MTK_PHONE, true);
            }

            IntentFilter killMonkeyFliter = new IntentFilter();
            killMonkeyFliter.addAction(BROADCAST_KILL_MONKEY);
            registerReceiver(monkeyReceiver, killMonkeyFliter);

            AlarmManagerReceiver.setAlarmToKillMonkey(this);

            settingSharedPreferences = ((settingSharedPreferences ==  null)?
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) : settingSharedPreferences);
            editor = (editor == null) ? settingSharedPreferences.edit() : editor;
            keepWakeUp(true);

            // moneky相关
            monkey_command = MonkeyTableData.getMonkeyCommand(getApplicationContext());
            isStartMonkey = MonkeyTableData.isMonkeyStart(getApplicationContext());
            handleActionMonkey();
        }

        /**------------------------------------- 重新发送报告 -----------------------------------**/
        if(null != action && ACTION_RETRY_POST_REPORT.equals(action)){
            Logger.file("上次发送失败，重试", Logger.MONKEY_SERVICE);
            ServiceNotificationHelper.getInstance(this).
                    notification(NOTIFICATION_ID, this, "MonkeyService", "发送报告中...");
            settingSharedPreferences = ((settingSharedPreferences ==  null)?
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) : settingSharedPreferences);
            editor = (editor == null) ? settingSharedPreferences.edit() : editor;
            keepWakeUp(true);
            if(FailPostRecordData.getInstance(getApplicationContext())
                    .readStringData(SettingPreferenceKey.MONKEY_REPORT_SEND_FAIL_REASON)
                    .equals("发送时没有连接WIFI，导致失败") && WifiUtil.isWifiConnected(getApplicationContext())){

                PerformsPostStatusBean performsPostStatusBean = new PerformsPostStatusBean();
                performsPostStatusBean.setTaskId(MonkeyTableData.getMonkeyId(getApplicationContext()));
                performsPostStatusBean.setStatus(iPerformsKey.taskFinishStatus);
                performsPostStatusBean.setTaskType("MonkeyTestStatus");
                performsPostStatusBean.setTestType("monkey");
                performsPostStatusBean.setAllTaskState("1");
                performsPostStatusBean.setTask(Constants.MpushTaskLabel.POST_TASK_RUNNING_STATU);

                MPush.I.sendPush(JSON.toJSONString(performsPostStatusBean).getBytes(Constants.UTF_8));
                // 置任务状态
                MonkeyTableData.setMonkeyId(getApplicationContext(), "0");

            }

            handleActionRetryPostReport();
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    sendReport();
                    return null;
                }
            }.execute();
        }

        /**------------------------------------- 智能遍历 -----------------------------------**/
        if(null != action && ACTION_SMART_TEST.equals(action)){

        }


        /**------------------------------------- 仅跑Monkey -----------------------------------**/
        if(null != action && ACTION_JUST_RUN_MONKEY.equals(action)){
            isJustRunMonkey = true;
            settingSharedPreferences = ((settingSharedPreferences ==  null)?
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) : settingSharedPreferences);
            editor = (editor == null) ? settingSharedPreferences.edit() : editor;
            keepWakeUp(true);
            monkey_command = MonkeyTableData.getMonkeyCommand(getApplicationContext());
            mTimer = new Timer();
            mTimer.schedule(mAddMonkeyTimesTask, 5 * Constants.TIME.SECOND, 5 * Constants.TIME.SECOND);
            IntentFilter killMonkeyFliter = new IntentFilter();
            killMonkeyFliter.addAction(BROADCAST_KILL_MONKEY);
            registerReceiver(monkeyReceiver, killMonkeyFliter);
            runMonkeyThread();
        }

        // “非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务
        // START_STICKY、START_NO_STICKY、START_REDELIVER_INTENT、START_STICKY_COMPATIBILITY
        return START_NOT_STICKY;
    }

    private void handleActionRetryPostReport(){
        // 写入字符串
        monkeyParams = new HashMap<String, String>();
        monkeyParams.put(MONKEY_START_TIME, FailPostRecordData.getInstance(getApplicationContext())
                .readStringData(MONKEY_START_TIME));   // 写入任务开始时间

        monkeyParams.put(MODULE, BaseData.getInstance(getApplicationContext())
                .readStringData(SettingPreferenceKey.APP_TYPE));   // 写入业务类型
        monkeyParams.put(EMAIL, BaseData.getInstance(getApplicationContext())
                .readStringData(SettingPreferenceKey.EMAIL_ADDRESS));   // 写入邮件名
        monkeyParams.put(MONKEY_PACKAGE, BaseData.getInstance(getApplicationContext())
                .readStringData(SettingPreferenceKey.MONKEY_PACKAGE));   // 写入monkey业务包名

        monkeyParams.put(MONKEY_MODEL, FailPostRecordData.getInstance(getApplicationContext())
                .readStringData(MONKEY_MODEL));   // 写入机型
        monkeyParams.put(MONKEY_IMEI, FailPostRecordData.getInstance(getApplicationContext())
                .readStringData(MONKEY_IMEI));   // 写入IMEI
        Logger.file("Write task_start_time, task_type, email, package_name, model and imei!", Logger.MONKEY_SERVICE);

        // 写入文件路径
        file = new HashMap<String, File>();
        File logFile = new File(MONKEY_LOG_PATH + ".zip");
        if(logFile.exists()){
            file.put(MONKEY_FLIE, logFile);
        }else{
            Logger.file("LogFile didn't exists", Logger.MONKEY_SERVICE);
        }
    }

    private void handleActionMonkey(){
        if (isStartMonkey){
            handler.sendEmptyMessageDelayed(SHOW_TASK_PROGRESS, 1000);
            runMonkeyThread();
            mTimer = new Timer();
            // 每5秒检测一下monkey进程readline()是否阻塞
            mTimer.schedule(mAddMonkeyTimesTask, 5 * Constants.TIME.SECOND, 5 * Constants.TIME.SECOND);
        }
    }

    /**
     * 定时器
     */
    private TimerTask mAddMonkeyTimesTask = new TimerTask() {
        @Override
        public void run() {
            // 进程不等于Null才进行判断，此时有monkey进程可做判断
            if(null != p){
                // 累加数值小于10，证明在定时器的一个周期内，累加计数一直没有累加，readline卡死
                if(1 > (addMonkeyTimes - lastAddMonkeyTimes)){
                    PublicMethod.killProcess("ps |grep com.android.commands.monkey", "system    ", " ");
                    // 延时10秒后再检测，等待monkey process起来
                    try {
                        Thread.sleep(10 * Constants.TIME.SECOND);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                lastAddMonkeyTimes = addMonkeyTimes;   // 赋值，储存上一次的数值
            }

            if(action.equals(ACTION_MONKEY)){
                runtime = System.currentTimeMillis() - Long.parseLong(MonkeyTableData
                        .getMonkeyStartTime(SuperTestApplication.getContext()));
                totalTime = MonkeyTableData.getMonkeyStopTime(SuperTestApplication.getContext()) -
                        Long.parseLong(MonkeyTableData.getMonkeyStartTime(SuperTestApplication.getContext()));

                if(runtime > totalTime + 2 * 60 * 1000){
                    // 超过1分钟还在跑Monkey，则停止MONKEY
                    if(MonkeyTableData.isMonkeyStart(SuperTestApplication.getContext())){
                        killMonkey();
                    }
                }

                if(runtime > totalTime){
                    runtime = totalTime;
                }

                setProgress("猴子执行中");
            }
        }
    };

    /**
     * 跑monkey
     */
    private void runMonkeyThread(){
        MonkeyTableData.setMonkeyStart(getApplicationContext(), true);
        PublicMethod.mute(getApplicationContext());
        PublicMethod.lockWifi(settingSharedPreferences, getApplicationContext());
        runMonkeyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                runMonkey();
            }
        });
        runMonkeyThread.start();
    }

    private void runMonkey(){
        Logger.file("执行Monkey", Logger.SUPER_TEST);
        BufferedInputStream in = null;
        BufferedReader resultReader  = null;
        while (MonkeyTableData.isMonkeyStart(getApplicationContext())){
            try {
                p = Runtime.getRuntime().exec(monkey_command);
                // ------ 关闭输出流和错误流 -------
                p.getOutputStream().close();
                p.getErrorStream().close();

                in = new BufferedInputStream(p.getInputStream());
                resultReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                // 读取出进程的输入缓存
                while(resultReader.readLine() != null){
                    Thread.sleep(100);
                    addMonkeyTimes++;
                }
                in.close();
                resultReader.close();
                p.waitFor();
                p = null;
                Log.d(TAG, String.format("Run shell command: %s", monkey_command));
            } catch (InterruptedException e) {
                Log.d(TAG, "Interrupted");
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.d(TAG, "UnsupportedEncodingException");
            } catch (IOException e) {
                Log.d(TAG, "IOException");
                e.printStackTrace();
            }
        }
    }

    /**
     * 杀monkey
     */
    private void killMonkey(){
        if(isJustRunMonkey){
            MonkeyUtils.setStopMonkeyParams();
            PublicMethod.killProcess("ps |grep com.android.commands.monkey", "system    ", " ");
            try {
                Thread.sleep(5 * Constants.TIME.SECOND);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isSendOver = true;
            stopSelf();
        }else{
            if(WifiUtil.isWifiConnected(getApplicationContext())){
                PerformsPostStatusBean performsPostStatusBean = new PerformsPostStatusBean();
                performsPostStatusBean.setTaskId(MonkeyTableData.getMonkeyId(getApplicationContext()));
                performsPostStatusBean.setStatus(iPerformsKey.taskFinishStatus);
                performsPostStatusBean.setTaskType("MonkeyTestStatus");
                performsPostStatusBean.setTestType("monkey");
                performsPostStatusBean.setAllTaskState("1");
                performsPostStatusBean.setTask(Constants.MpushTaskLabel.POST_TASK_RUNNING_STATU);

                MPush.I.sendPush(JSON.toJSONString(performsPostStatusBean).getBytes(Constants.UTF_8));
                // 置任务状态
                MonkeyTableData.setMonkeyId(getApplicationContext(), "0");
            }

            MonkeyUtils.setStopMonkeyParams();
            PublicMethod.killProcess("ps |grep com.android.commands.monkey", "system    ", " ");
            sendMonkeyReport();
        }
    }

    /**
     * 发送报告
     */
    private void sendMonkeyReport() {
        ServiceNotificationHelper.getInstance(this).
                notification(NOTIFICATION_ID, this, "MonkeyService", "发送报告中...");
        setProgress("压缩文件中..");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String cpu = BaseData.getInstance(getApplicationContext()).readStringData("CPU");
                    if(null != cpu && cpu.contains("mt")){
                        Runtime.getRuntime().exec(CommonVariable.mtkLogBroadcast.replace("%s", "stop").replace("%d", "7"));
                    }

                    Runtime.getRuntime().exec("am start -n com.meizu.logreport/com.meizu.logreport.activity.MainActivity");
                    Thread.sleep(4 * 1000);
                    Runtime.getRuntime().exec("input keyevent 4");    // 返回桌面
                    Runtime.getRuntime().exec("am broadcast -a com.meizu.logreport" +
                            ".adb_cmd --ei action 1 --ei type 0 --ez zip false");    // 采集Logreport日志
                    Thread.sleep(8000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

                // 保存数据至数据库
                SqlAlterHelper.getInstance(getApplicationContext()).addData(
                        MonkeyTableData.getInstance(getApplicationContext()).readStringData("strMonkeyType"),
                        MonkeyTableData.getMonkeyCommand(getApplicationContext()),
                        PublicMethod.getSystemTime(), settingSharedPreferences.getBoolean(SettingPreferenceKey.MUTE, false),
                        settingSharedPreferences.getBoolean(SettingPreferenceKey.LOCK_WIFI, false), false);

                String app_package = BaseData.getInstance(getApplicationContext())
                        .readStringData(SettingPreferenceKey.MONKEY_PACKAGE);

                if(!TextUtils.isEmpty(app_package) && app_package.equals(iPublicConstants.PACKET_VIDEO)){
                    Logger.file("业务为视频，删除缓存视频，腾出内存空间...", Logger.MONKEY_SERVICE);
                    PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "Movies/download");
                }else if(!TextUtils.isEmpty(app_package) && app_package.equals(iPublicConstants.PACKET_MUSIC)){
                    Logger.file("业务为音乐，删除缓存音乐，腾出内存空间...", Logger.MONKEY_SERVICE);
                    PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "Music/Download");
                }

//                // 删除截图，影响了图库图片显示
//                PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "Pictures");

                File monkeyFile = new File(MONKEY_LOG_PATH + ".zip");
                File monkeyOrignFile = new File(MONKEY_LOG_PATH);

                // 清除monkey日志
                if(monkeyFile.exists()){
                    Logger.file("存在旧monkey日志压缩包，删除中...", Logger.MONKEY_SERVICE);
                    monkeyFile.delete();
                }

                // 清除monkey原始日志
                if(monkeyOrignFile.exists()){
                    Logger.file("存在旧monkey日志文件夹，删除中...", Logger.MONKEY_SERVICE);
                    PublicMethod.deleteDirectory(MONKEY_LOG_PATH);
                }
                String newLogReport = PublicMethod.getFileDirNewDataPath(LOGREPORT_PATH);
                // 尝试2次，解决部分机型不能正常抓取log的问题
                for(int i = 0; i < 2; i ++){
                    if(1 == i){
                        try {
                            Runtime.getRuntime().exec("am broadcast -a com.meizu.logreport" +
                                    ".adb_cmd --ei action 1 --ei type 0 --ez zip false");    // 采集Logreport日志
                            Logger.file("发现没有抓到Logreport日志，重新抓取一遍！", Logger.MONKEY_SERVICE);

                            try {
                                Thread.sleep(8000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(null == newLogReport){
                        newLogReport = PublicMethod.getFileDirNewDataPath(LOGREPORT_PATH);
                    }

                    if(null != newLogReport){
                        Logger.file("拷贝LogReport日志", Logger.MONKEY_SERVICE);
                        PublicMethod.copyLogReportFolder(newLogReport, MONKEY_LOG_PATH);
                        break;
                    }
                }


                // MTK手机需要剪切MTK日志
                if(isMtkPhone){
                    String newMtkLog = PublicMethod.getNewMtkLogPath(MTK_LOG_PATH);
                    if(null != newMtkLog){
                        Logger.file("MTK手机，剪切MTK日志", Logger.MONKEY_SERVICE);
                        PublicMethod.cutFolder(newMtkLog, MONKEY_LOG_PATH + DIVIDE_LINE
                                + "MtkLog" + DIVIDE_LINE + newMtkLog.replace(MTK_LOG_PATH, ""));
                    }
                }

                // 压缩文件
                try {
                    if(new File(MONKEY_LOG_PATH).exists()){
                        Logger.file("压缩LOG文件", Logger.MONKEY_SERVICE);
                        ZipUtil.zip(MONKEY_LOG_PATH + DIVIDE_LINE, MONKEY_LOG_PATH + ".zip");
                        PublicMethod.deleteDirectory(MONKEY_LOG_PATH);
                        Logger.file("压缩完毕，并删除旧MONKEY日志文件夹", Logger.MONKEY_SERVICE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(null == monkeyParams){
                    monkeyParams = new HashMap<String, String>();
                    monkeyParams.put(MONKEY_START_TIME, MonkeyTableData.getMonkeyStartTime(getApplicationContext()));   // 写入任务开始时间
                    monkeyParams.put(MODULE, BaseData.getInstance(getApplicationContext())
                            .readStringData(SettingPreferenceKey.APP_TYPE));   // 写入业务类型
                    monkeyParams.put(EMAIL, BaseData.getInstance(getApplicationContext())
                            .readStringData(SettingPreferenceKey.EMAIL_ADDRESS));   // 写入邮件名
                    monkeyParams.put(MONKEY_PACKAGE, BaseData.getInstance(getApplicationContext())
                            .readStringData(SettingPreferenceKey.MONKEY_PACKAGE));   // 写入monkey业务包名
                    monkeyParams.put(MONKEY_MODEL, PerformsData.getInstance(getApplicationContext())
                            .readStringData(iPerformsKey.deviceType));   // 写入机型
                    monkeyParams.put(MONKEY_IMEI, PerformsData.getInstance(getApplicationContext())
                            .readStringData(iPerformsKey.imei));   // 写入IMEI
                    Logger.file("Write task_start_time, task_type, email, package_name, model and imei!", Logger.MONKEY_SERVICE);
                }

                if(null == file){
                    file = new HashMap<String, File>();
                    Logger.file("填写文件路径", Logger.MONKEY_SERVICE);
                    File logFile = new File(MONKEY_LOG_PATH + ".zip");
                    if(logFile.exists()){
                        file.put(MONKEY_FLIE, logFile);
                    }
                }
                sendReport();

                isSendOver = true;
                return null;
            }
        }.execute();
    }

    /**
     * 发送报告
     */
    private void sendReport(){
        Logger.file("开始发送报告..", Logger.MONKEY_SERVICE);
        setProgress("正在发送报告..");
        try {
            PostUploadHelper.getInstance().postFile(iPublicConstants.MONKEY_RESULT_POST_URL,
                    monkeyParams, file, new PostCallBack() {
                        @Override
                        public void resultCallBack(boolean isSuccess, int resultCode, String data) {
                            int status = -1;
                            String message = null;
                            JSONObject jsonData;
                            try {
                                jsonData = new JSONObject(data);
                                status = jsonData.optInt("status");
                                message = jsonData.optString("message");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Log.d("MonkeyService", data);
                            if(!isSuccess && tryTimes > 0){
                                Logger.file("发送失败，尝试重发", Logger.MONKEY_SERVICE);
                                --tryTimes;
                                sendReport();
                            }else if(!isSuccess && 0 >= tryTimes){
                                Logger.file("发送失败，code不等于200，尝试多次重发不成功，发送失败", Logger.MONKEY_SERVICE);
                                isSendOver = true;
                                saveFailPostRecord(false, "发送失败，code不等于200");
                                stopSelf();
                            }else if(isSuccess && 0 < tryTimes){
                                if(0 == status){
                                    Logger.file("发送成功", Logger.MONKEY_SERVICE);
                                    isSendOver = true;
                                    saveFailPostRecord(true, "发送成功");
                                    stopSelf();
                                }else{
                                    Logger.file("发送失败，返回数据为：status = " + status + "，message = " + message, Logger.MONKEY_SERVICE);
                                    --tryTimes;
                                    sendReport();
                                }
                            }else if(isSuccess && 0 >= tryTimes) {
                                Logger.file("发送失败，服务器返回status状态不对，尝试多次重发失败", Logger.MONKEY_SERVICE);
                                isSendOver = true;
                                saveFailPostRecord(false, "发送失败，服务器返回status状态不对\n" + status + "\n" + message);
                                stopSelf();
                            }else{
                                Logger.file(message, Logger.MONKEY_SERVICE);
                                isSendOver = true;
                                saveFailPostRecord(false, message);
                                stopSelf();
                            }
                        }
                    });
        } catch (Exception e) {
            isSendOver = true;
            Logger.file(e.toString(), Logger.MONKEY_SERVICE);
            if(!WifiUtil.isWifiConnected(this)){
                saveFailPostRecord(false, "发送时没有连接WIFI，导致失败");
            }else{
                saveFailPostRecord(false, e.toString());
            }
            e.printStackTrace();
            stopSelf();
        }
    }

    /**
     * 保存失败的发送结果
     * @param isSuccess 是否发送成功
     * @param failReason 失败原因
     */
    private void saveFailPostRecord(boolean isSuccess, String failReason){
        if(null != action && ACTION_RETRY_POST_REPORT.equals(action)){
            sendMonkeyRetryBroadcast(isSuccess);
        }

        FailPostRecordData.getInstance(getApplicationContext())
                .writeBooleanData(SettingPreferenceKey.IS_MONKEY_REPORT_SEND_SUCCESS, isSuccess);
        FailPostRecordData.getInstance(getApplicationContext())
                .writeStringData(SettingPreferenceKey.MONKEY_REPORT_SEND_FAIL_REASON, failReason);

        FailPostRecordData.getInstance(getApplicationContext())
                .writeStringData(MONKEY_START_TIME, monkeyParams.get(MONKEY_START_TIME));
        FailPostRecordData.getInstance(getApplicationContext())
                .writeStringData(MONKEY_MODEL, monkeyParams.get(MONKEY_MODEL));
        FailPostRecordData.getInstance(getApplicationContext())
                .writeStringData(MONKEY_IMEI, monkeyParams.get(MONKEY_IMEI));
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
     * 注册杀monkey接收广播事件
     */
    private BroadcastReceiver monkeyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BROADCAST_KILL_MONKEY)) {
                killMonkey();
            }
        }
    };


    /**
     * Monkey重试发送结果
     * @param result 结果
     */
    private void sendMonkeyRetryBroadcast(boolean result){
        Intent intent = new Intent();
        intent.setAction("action.st.send.monkey.over");
        intent.putExtra("result", result);
        sendBroadcast(intent);
    }


    @Override
    public void onDestroy() {
        // 恢复服务按钮
        try {
            if(null != action && action.equals(ACTION_MONKEY) && null != manager && isU2ServiceOn){
                manager.hideProgressBar();
                manager.hideTaskList();
                manager.showFloatBall();
            }

            if(null != action && action.equals(ACTION_MONKEY) && null != manager){
                manager.hideProgressBar();
            }

            ServiceNotificationHelper.getInstance(this).notificationCancel(this, NOTIFICATION_ID);

            if(null != runMonkeyThread){
                runMonkeyThread.interrupt();
            }

            if(mTimer != null){
                mTimer.cancel();
                mTimer = null;
            }

            if(mAddMonkeyTimesTask != null){
                mAddMonkeyTimesTask.cancel();
                mAddMonkeyTimesTask = null;
            }

            if(!isSendOver){
                Logger.file("服务被意外杀死", Logger.MONKEY_SERVICE);
            }else{
                Logger.file("正常退出服务！", Logger.MONKEY_SERVICE);
            }

            keepWakeUp(false);

            if (null != action && (ACTION_MONKEY.equals(action) || ACTION_JUST_RUN_MONKEY.equals(action))){
                unregisterReceiver(monkeyReceiver);
            }
        }catch (Exception e){
            Logger.file("退出monkey服务报错==>" + e.toString(), Logger.MONKEY_SERVICE);
        }


        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAlarmListener() {
        Logger.file("定时时间到，干掉monkey，收集日志", Logger.MONKEY_SERVICE);
        killMonkey();
    }

    /**
     * 设置进度值
     */
    private void setProgress(String text){
        this.text = text;
        handler.sendEmptyMessage(UPDATE_MONKEY_TASK_PROGRESS);
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case UPDATE_MONKEY_TASK_PROGRESS:
                    if(null != manager){
                        manager.setProgressBar(text, runtime, totalTime);
                    }
                    break;
                case SHOW_TASK_PROGRESS:
                    manager = ViewManager.getInstance(SuperTestApplication.getContext());
                    manager.hideFloatBall();
                    manager.hideTaskList();
                    manager.showTaskProgressBar();
                    manager.setProgressBar("初始化", 0, 1);
                    break;
            }
        }
    };
}
