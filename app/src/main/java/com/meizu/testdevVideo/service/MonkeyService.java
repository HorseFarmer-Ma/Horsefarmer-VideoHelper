package com.meizu.testdevVideo.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;

import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.interports.iPublicConstants;

import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.PostUploadHelper;
import com.meizu.testdevVideo.library.ServiceNotificationHelper;
import com.meizu.testdevVideo.library.SqlAlterHelper;
import com.meizu.testdevVideo.library.WindowManagerHelper;

import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.FailPostRecordData;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.shell.ShellUtil;
import com.meizu.testdevVideo.util.shell.ShellUtils;
import com.meizu.testdevVideo.util.wifi.WifiUtil;
import com.meizu.testdevVideo.util.zip.ZipUtil;

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


public class MonkeyService extends Service {

    private static final int NOTIFICATION_ID = 102;

    private static final String TAG = "MonkeyService";
    private static final String ACTION_MONKEY = "com.meizu.testdevVideo.service.action.MONKEY";
    private static final String ACTION_RETRY_POST_REPORT = "com.meizu.testdevVideo.service.action.retry.post.report";
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

    // 发送报告相关
    private int tryTimes = 10;       // 发送报告尝试次数
    private Map<String, String> monkeyParams = null;
    private Map<String, File> file = null;
    private boolean isSendOver = false;
    private SharedPreferences settingSharedPreferences = null;
    private SharedPreferences.Editor editor = null;
    private boolean isMtkPhone;

    // Monkey执行相关
    private WindowManagerHelper windowManagerHelper = null;
    private String monkey_command = null;
    private View startView;
    private ImageButton startTestBt, stopBt;
    private boolean isUseFloatBtn;
    private boolean isStartMonkey;
    private Process p = null;
    private Timer mTimer;
    private long addMonkeyTimes = 10;   // 初始值
    private long lastAddMonkeyTimes = 0;


    public static void startActionMonkeyReport(Context context, boolean isMtkPhone) {
        Intent intent = new Intent(context, MonkeyService.class);
        intent.setAction(ACTION_MONKEY);
        intent.putExtra(IS_MTK_PHONE, isMtkPhone);
        context.startService(intent);
    }

    public static void startActionRetryPostReport(Context context) {
        Intent intent = new Intent(context, MonkeyService.class);
        intent.setAction(ACTION_RETRY_POST_REPORT);
        context.startService(intent);
    }

    public static void stopActionMonkeyReport(Context context) {
        Intent intent = new Intent(context, MonkeyService.class);
        context.stopService(intent);
    }

    public static void stopActionRetryPostReport(Context context) {
        Intent intent = new Intent(context, MonkeyService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceNotificationHelper.getInstance(this).
                notification(NOTIFICATION_ID, this, "MonkeyService", "Monkey Task Running...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = null;
        if(null != intent){
            action = intent.getAction();
        }

        if (null != action && ACTION_MONKEY.equals(action)) {
            isMtkPhone = intent.getBooleanExtra(IS_MTK_PHONE, true);
            settingSharedPreferences = ((settingSharedPreferences ==  null)?
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) : settingSharedPreferences);
            editor = (editor == null) ? settingSharedPreferences.edit() : editor;
            keepWakeUp(true);

            // moneky相关
            monkey_command = MonkeyTableData.getInstance(getApplicationContext()).readStringData("monkey_command");
            isUseFloatBtn = settingSharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_FLOAT_BTN, true);
            isStartMonkey = MonkeyTableData.getInstance(getApplicationContext()).readBooleanData("isStart");

            IntentFilter killMonkeyFliter = new IntentFilter();
            killMonkeyFliter.addAction("action.st.kill.monkey");
            registerReceiver(monkeyReceiver, killMonkeyFliter);
            handleActionMonkey();
        }else if(null != action && ACTION_RETRY_POST_REPORT.equals(action)){
            PublicMethod.saveLog(TAG, "上次发送失败，重试");
            ServiceNotificationHelper.getInstance(this).
                    notification(NOTIFICATION_ID, this, "MonkeyService", "发送报告中...");
            handleActionRetryPostReport();
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    sendReport();
                    return null;
                }
            }.execute();

        }

        return START_STICKY;
    }

    private void handleActionRetryPostReport(){
        // 写入字符串
        monkeyParams = new HashMap<String, String>();
        monkeyParams.put(MONKEY_START_TIME, FailPostRecordData.getInstance(getApplicationContext())
                .readStringData(MONKEY_START_TIME));   // 写入任务开始时间
        monkeyParams.put(MODULE, FailPostRecordData.getInstance(getApplicationContext())
                .readStringData(MODULE));  // 写入业务类型
        monkeyParams.put(EMAIL, FailPostRecordData.getInstance(getApplicationContext())
                .readStringData(EMAIL));   // 写入邮件名
        monkeyParams.put(MONKEY_PACKAGE, FailPostRecordData.getInstance(getApplicationContext())
                .readStringData(MONKEY_PACKAGE));   // 写入monkey业务包名
        monkeyParams.put(MONKEY_MODEL, FailPostRecordData.getInstance(getApplicationContext())
                .readStringData(MONKEY_MODEL));   // 写入机型
        monkeyParams.put(MONKEY_IMEI, FailPostRecordData.getInstance(getApplicationContext())
                .readStringData(MONKEY_IMEI));   // 写入IMEI
        PublicMethod.saveLog(TAG, "Write task_start_time, task_type, email, package_name, model and imei!");

        // 写入文件路径
        file = new HashMap<String, File>();
        File logFile = new File(MONKEY_LOG_PATH + ".zip");
        if(logFile.exists()){
            file.put(MONKEY_FLIE, logFile);
        }else{
            PublicMethod.saveLog(TAG, "LogFile didn't exists");
        }
    }

    private void handleActionMonkey(){
        if(!isUseFloatBtn){
            if (isStartMonkey){
                PublicMethod.saveLog(TAG, "不使用悬浮按钮");
                runMonkeyThread();
            }
        }else {
            PublicMethod.saveLog(TAG, "使用悬浮按钮");
            monkey_Init();
            if(isStartMonkey){
                PublicMethod.saveLog(TAG, "之前意外停止，重新开始Monkey任务");
                stopBt.setVisibility(View.VISIBLE);
                startTestBt.setVisibility(View.GONE);
                runMonkeyThread();
            }
        }

        mTimer = new Timer();
        // 每5秒检测一下monkey进程readline()是否阻塞
        mTimer.schedule(mAddMonkeyTimesTask, 10 * Constants.TIME.SECOND, 5 * Constants.TIME.SECOND);
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
//                    PublicMethod.saveLog(TAG, "readline()阻塞，干掉process重启monkey进程");
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
        }
    };

    /**
     * 悬浮窗口初始化
     */
    public void monkey_Init(){
        PublicMethod.saveLog(TAG, "启动悬浮窗_初始化开始");
        windowManagerHelper = new WindowManagerHelper(this);
        startView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.monkey_start, null);
        startTestBt = (ImageButton) startView.findViewById(R.id.start);
        stopBt = (ImageButton) startView.findViewById(R.id.stop);
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
                MonkeyTableData.getInstance(getApplicationContext()).writeBooleanData("isStart", true);
                startTestBt.setVisibility(View.GONE);
                stopBt.setVisibility(View.VISIBLE);
                runMonkeyThread();
            }
        });
        PublicMethod.saveLog(TAG, "启动悬浮窗_初始化完成");
    }

    /**
     * 跑monkey
     */
    private void runMonkeyThread(){
        MonkeyTableData.getInstance(getApplicationContext()).writeBooleanData("isStart", true);
        PublicMethod.mute(getApplicationContext());
        PublicMethod.lockWifi(settingSharedPreferences, getApplicationContext());

        new Thread(new Runnable() {
            @Override
            public void run() {
                PublicMethod.saveLog(TAG, "执行Monkey指令");
                runMonkey();
            }
        }).start();
    }

    private void runMonkey(){
        // 查看有无在运行的monkey进程，有的话干掉
        int status = 0;
        String text = null;
        BufferedInputStream in = null;
        BufferedReader resultReader  = null;
        try {
            p = Runtime.getRuntime().exec(monkey_command);
            // ------ 关闭输出流和错误流 -------
            p.getOutputStream().close();
            p.getErrorStream().close();

            in = new BufferedInputStream(p.getInputStream());
            resultReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            // 读取出进程的输入缓存
            while((line = resultReader.readLine()) != null){
                text += line + "\n";
                addMonkeyTimes++;
            }
            in.close();
            resultReader.close();
            status = p.waitFor();
            p = null;
//            PublicMethod.saveLog(TAG, String.format("Run shell command: %s,  status: %s", monkey_command, status));
            Thread.sleep(2 * Constants.TIME.SECOND);
            if(MonkeyTableData.getInstance(getApplicationContext()).readBooleanData("isStart")){
                runMonkey();
            }
        } catch (InterruptedException e) {
            PublicMethod.saveLog(TAG, "InterruptedException意外出错");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            PublicMethod.saveLog(TAG, "UnsupportedEncodingException意外出错");
        } catch (IOException e) {
            PublicMethod.saveLog(TAG, "IOException意外出错");
            e.printStackTrace();
        }
    }

    /**
     * 杀monkey
     */
    private void killMonkey(){
        MonkeyTableData.getInstance(this).writeBooleanData("isStart", false);
        if(isUseFloatBtn){
            windowManagerHelper.removeView(startView);
        }
        PublicMethod.saveLog(TAG, "正常停止monkey");
        PublicMethod.killProcess("ps |grep com.android.commands.monkey", "system    ", " ");
        sendMonkeyReport();
    }

    /**
     * 发送报告
     */
    private void sendMonkeyReport() {
        ServiceNotificationHelper.getInstance(this).
                notification(NOTIFICATION_ID, this, "MonkeyService", "发送报告中...");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if(settingSharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_MTK_SET, true)){
                    try {
                        String cpu = ShellUtil.getProperty("ro.hardware");
                        if(null != cpu && cpu.contains("mt")){
                            if(settingSharedPreferences.getBoolean(SettingPreferenceKey.CATCH_LOG_TYPE, true)){
                                Runtime.getRuntime().exec(CommonVariable.mtkLogBroadcast.replace("%s", "stop").replace("%d", "1"));
                            }else{
                                Runtime.getRuntime().exec(CommonVariable.mtkLogBroadcast.replace("%s", "stop").replace("%d", "7"));
                            }
                        }

                        Runtime.getRuntime().exec("am start -n com.meizu.logreport/com.meizu.logreport.activity.MainActivity");
                        Thread.sleep(2000);
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
                            MonkeyTableData.getInstance(getApplicationContext()).readStringData("monkey_command"),
                            PublicMethod.getSystemTime(), settingSharedPreferences.getBoolean(SettingPreferenceKey.MUTE, false),
                            settingSharedPreferences.getBoolean(SettingPreferenceKey.LOCK_WIFI, true),
                            settingSharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_FLOAT_BTN, true));

                    if(BaseData.getInstance(getApplicationContext())
                            .readStringData(SettingPreferenceKey.MONKEY_PACKAGE)
                            .equals(iPublicConstants.PACKET_VIDEO)){
                        PublicMethod.saveLog(TAG, "业务为视频，删除缓存视频，腾出内存空间...");
                        PublicMethod.deleteDirectory(iPublicConstants.LOCAL_MEMORY + "Movies/download");
                    }

                    File monkeyFile = new File(MONKEY_LOG_PATH + ".zip");
                    File monkeyOrignFile = new File(MONKEY_LOG_PATH);

                    // 清除monkey日志
                    if(monkeyFile.exists()){
                        PublicMethod.saveLog(TAG, "存在旧monkey日志压缩包，删除中...");
                        monkeyFile.delete();
                    }

                    // 清除monkey原始日志
                    if(monkeyOrignFile.exists()){
                        PublicMethod.saveLog(TAG, "存在旧monkey日志文件夹，删除中...");
                        PublicMethod.deleteDirectory(MONKEY_LOG_PATH);
                    }

                    String newLogReport = PublicMethod.getFileDirNewDataPath(LOGREPORT_PATH);
                    if(null != newLogReport){
                        PublicMethod.saveLog(TAG, "拷贝LogReport日志");
                        PublicMethod.copyLogReportFolder(newLogReport, MONKEY_LOG_PATH);
                    }

                    // MTK手机需要剪切MTK日志
                    if(isMtkPhone){
                        String newMtkLog = PublicMethod.getNewMtkLogPath(MTK_LOG_PATH);
                        if(null != newMtkLog){
                            PublicMethod.saveLog(TAG, "MTK手机，剪切MTK日志");
                            PublicMethod.cutFolder(newMtkLog, MONKEY_LOG_PATH + DIVIDE_LINE
                                    + "MtkLog" + DIVIDE_LINE + newMtkLog.replace(MTK_LOG_PATH, ""));
                        }
                    }

                    // 压缩文件
                    try {
                        if(new File(MONKEY_LOG_PATH).exists()){
                            PublicMethod.saveLog(TAG, "压缩LOG文件");
                            ZipUtil.zip(MONKEY_LOG_PATH + DIVIDE_LINE, MONKEY_LOG_PATH + ".zip");
                            PublicMethod.deleteDirectory(MONKEY_LOG_PATH);
                            PublicMethod.saveLog(TAG, "压缩完毕，并删除旧MONKEY日志文件夹");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(null == monkeyParams){
                        monkeyParams = new HashMap<String, String>();
                        monkeyParams.put(MONKEY_START_TIME, BaseData.getInstance(getApplicationContext())
                                .readStringData("monkey_start_time"));   // 写入任务开始时间
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
                        PublicMethod.saveLog(TAG, "Write task_start_time, task_type, email, package_name, model and imei!");
                    }

                    if(null == file){
                        file = new HashMap<String, File>();
                        PublicMethod.saveLog(TAG, "填写文件路径");
                        File logFile = new File(MONKEY_LOG_PATH + ".zip");
                        if(logFile.exists()){
                            file.put(MONKEY_FLIE, logFile);
                        }
                    }
                    sendReport();

                    isSendOver = true;
                    stopSelf();

                }else {
                    PublicMethod.saveLog(TAG, "设置中没有开启跑monkey自动抓取日志");
                    isSendOver = true;
                    stopSelf();
                }

                return null;
            }
        }.execute();
    }

    /**
     * 发送报告
     */
    private void sendReport(){
        PublicMethod.saveLog(TAG, "开始发送报告...");
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
                                PublicMethod.saveLog(TAG, "发送失败，尝试重发");
                                --tryTimes;
                                sendReport();
                            }else if(!isSuccess && 0 >= tryTimes){
                                PublicMethod.saveLog(TAG, "发送失败，code不等于200，尝试多次重发不成功，发送失败");
                                isSendOver = true;
                                saveFailPostRecord(false, "发送失败，code不等于200");
                                stopSelf();
                            }else if(isSuccess && 0 < tryTimes){
                                if(0 == status){
                                    PublicMethod.saveLog(TAG, "发送成功");
                                    isSendOver = true;
                                    saveFailPostRecord(true, "发送成功");
                                    stopSelf();
                                }else{
                                    PublicMethod.saveLog(TAG, "发送失败，返回数据为：status = " + status + "，message = " + message);
                                    --tryTimes;
                                    sendReport();
                                }
                            }else if(isSuccess && 0 >= tryTimes) {
                                PublicMethod.saveLog(TAG, "发送失败，服务器返回status状态不对，尝试多次重发失败");
                                isSendOver = true;
                                saveFailPostRecord(false, "发送失败，服务器返回status状态不对");
                                stopSelf();
                            }else{
                                PublicMethod.saveLog(TAG, "其他未知发送情况");
                                isSendOver = true;
                                saveFailPostRecord(false, "其他未知发送情况");
                                stopSelf();
                            }
                        }
                    });
        } catch (IOException e) {
            isSendOver = true;
            PublicMethod.saveLog(TAG, "接口不通，停止发送");
            if(!WifiUtil.isWifiConnected(this)){
                saveFailPostRecord(false, "发送时没有连接WIFI，导致失败");
            }else{
                saveFailPostRecord(false, "IOException，接口不通");
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
        FailPostRecordData.getInstance(getApplicationContext())
                .writeBooleanData(SettingPreferenceKey.IS_MONKEY_REPORT_SEND_SUCCESS, isSuccess);
        FailPostRecordData.getInstance(getApplicationContext())
                .writeStringData(SettingPreferenceKey.MONKEY_REPORT_SEND_FAIL_REASON, failReason);

        FailPostRecordData.getInstance(getApplicationContext())
                .writeStringData(MONKEY_START_TIME, monkeyParams.get(MONKEY_START_TIME));
        FailPostRecordData.getInstance(getApplicationContext())
                .writeStringData(MODULE, monkeyParams.get(MODULE));
        FailPostRecordData.getInstance(getApplicationContext())
                .writeStringData(EMAIL, monkeyParams.get(EMAIL));
        FailPostRecordData.getInstance(getApplicationContext())
                .writeStringData(MONKEY_PACKAGE, monkeyParams.get(MONKEY_PACKAGE));
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
            if (action.equals("action.st.kill.monkey")) {
                killMonkey();
            }
        }
    };


    @Override
    public void onDestroy() {

        ServiceNotificationHelper.getInstance(this).notificationCancel(this, NOTIFICATION_ID);

        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }

        if(mAddMonkeyTimesTask != null){
            mAddMonkeyTimesTask.cancel();
            mAddMonkeyTimesTask = null;
        }

        if(!isSendOver){
            PublicMethod.saveLog(TAG, "服务被意外杀死\n");
            try {
                if(isUseFloatBtn){
                    windowManagerHelper.removeView(startView);
                }
            }catch (Exception io){
                io.printStackTrace();
            }

        }else{
            PublicMethod.saveLog(TAG, "正常退出服务！\n");
        }
        keepWakeUp(false);
        unregisterReceiver(monkeyReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
