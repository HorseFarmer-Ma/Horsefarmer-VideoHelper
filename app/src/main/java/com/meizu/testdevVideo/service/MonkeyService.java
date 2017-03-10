package com.meizu.testdevVideo.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
//import android.text.TextUtils;
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

import com.meizu.testdevVideo.interports.iPublicConstants;
import com.meizu.testdevVideo.library.PostCallBack;
import com.meizu.testdevVideo.library.PostUploadHelper;
//import com.meizu.testdevVideo.library.ServiceNotificationHelper;

import com.meizu.testdevVideo.library.WindowManagerHelper;
import com.meizu.testdevVideo.util.PublicMethod;
//import com.meizu.testdevVideo.util.sharepreference.BaseData;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
//import com.meizu.testdevVideo.util.sharepreference.PrefWidgetOnOff;
import com.meizu.testdevVideo.util.shell.ShellUtil;
import com.meizu.testdevVideo.util.shell.ShellUtils;
//import com.meizu.testdevVideo.util.zip.ZipUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

//import java.sql.Time;
//import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MonkeyService extends Service {

//    private static final int NOTIFICATION_ID = 102;

    private static final String TAG = "MonkeyService";
    private static final String ACTION_MONKEY = "com.meizu.testdevVideo.service.action.MONKEY";
    private static final String IS_MTK_PHONE = "isMtkPhone";
//    private static final String MTK_LOG_PATH = iPublicConstants.LOCAL_MEMORY + "mtklog/mobilelog/";
//    private static final String LOGREPORT_PATH = iPublicConstants.PERFORMS_PURE_BACKGROUND_RESULT;
//    private static final String MONKEY_LOG_PATH = iPublicConstants.LOCAL_MEMORY + "SuperTest/MonkeyLog/monkey";
//    private static final String DIVIDE_LINE = "/";
//    private static final String EMAIL = "email";
//    private static final String MODULE = "module";
//    private static final String MONKEY_FLIE = "file";
    private boolean isKeepWakeUp = false;

    // 发送报告相关
    private int tryTimes = 5;       // 发送报告尝试次数
    private static Map<String, String> monkeyParams = null;
    private static Map<String, File> file = null;
    private boolean isSendOver = false;
    private SharedPreferences settingSharedPreferences = null;
    private SharedPreferences.Editor editor = null;
    private boolean isMtkPhone;

    // Monkey执行相关
    private Timer timer;
    private WindowManagerHelper windowManagerHelper = null;
    private String monkey_command = null;
    private View startView;
    private ImageButton startTestBt, stopBt;
    private boolean isUseFloatBtn;
    private boolean isStartMonkey;


    public static void startActionMonkeyReport(Context context, boolean isMtkPhone) {
        Intent intent = new Intent(context, MonkeyService.class);
        intent.setAction(ACTION_MONKEY);
        intent.putExtra(IS_MTK_PHONE, isMtkPhone);
        context.startService(intent);
    }

    public static void stopActionMonkeyReport(Context context) {
        Intent intent = new Intent(context, MonkeyService.class);
        context.stopService(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
//        ServiceNotificationHelper.getInstance(this).
//                notification(NOTIFICATION_ID, this, "MonkeyService", "Monkey服务进行中...");

        settingSharedPreferences = ((settingSharedPreferences ==  null)?
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) : settingSharedPreferences);

        timer = new Timer();
        timer.schedule(mMonkeyTimeTask, Constants.TIME.MINUTE, 10 * Constants.TIME.SECOND);

        editor = (editor == null) ? settingSharedPreferences.edit() : editor;
        isKeepWakeUp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(SettingPreferenceKey.KEEP_WAKEUP, false);
        keepWakeUp(true);

        // moneky相关
        monkey_command = MonkeyTableData.getInstance(getApplicationContext()).readStringData("monkey_command");
        isUseFloatBtn = settingSharedPreferences.getBoolean(SettingPreferenceKey.MONKEY_FLOAT_BTN, true);
        isStartMonkey = MonkeyTableData.getInstance(getApplicationContext()).readBooleanData("isStart");

        IntentFilter killMonkeyFliter = new IntentFilter();
        killMonkeyFliter.addAction("action.st.kill.monkey");
        registerReceiver(monkeyReceiver, killMonkeyFliter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = null;
        if(null != intent){
            action = intent.getAction();
            isMtkPhone = intent.getBooleanExtra(IS_MTK_PHONE, true);
        }

        if (null != action && ACTION_MONKEY.equals(action)) {
            handleActionMonkey();
        }
        return START_STICKY;
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
    }

    /**
     * 悬浮窗口初始化
     */
    public void monkey_Init(){
        PublicMethod.saveLog(TAG, "启动悬浮窗_初始化开始");
        windowManagerHelper = new WindowManagerHelper(this);
        startView = LayoutInflater.from(this).inflate(R.layout.monkey_start, null);
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
     * 定时器
     */
    private TimerTask mMonkeyTimeTask = new TimerTask() {
        @Override
        public void run() {
            boolean isStart = MonkeyTableData.getInstance(getApplicationContext()).readBooleanData("isStart");
            boolean isMonkeyProcessLiving = (ShellUtils.execCommand("ps|grep com.android.commands.monkey",
                    false, true).successMsg).length() > 0;
            if(isStart && !isMonkeyProcessLiving){
                PublicMethod.saveLog(TAG, "触发定时任务，monkey进程被意外杀死");
                onMonkeyServiceListener.monkeyServiceListener();
            }else if(!isStart && isMonkeyProcessLiving){
                killMonkey();
            }
        }
    };

    /**
     * 跑monkey
     */
    private void runMonkeyThread(){
        MonkeyTableData.getInstance(getApplicationContext()).writeBooleanData("isStart", true);
        PublicMethod.mute(getApplicationContext());
        PublicMethod.lockWifi(settingSharedPreferences, getApplicationContext());

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    PublicMethod.saveLog(TAG, "执行Monkey指令");
                    Runtime.getRuntime().exec(monkey_command);
                } catch (IOException e){
                    e.printStackTrace();
                    PublicMethod.saveLog(TAG, "意外出错");
                }
                return null;
            }
        }.execute();
    }

    /**
     * 杀monkey
     */
    private void killMonkey(){
        if(isUseFloatBtn){
            windowManagerHelper.removeView(startView);
        }
        PublicMethod.saveLog(TAG, "正常停止monkey");
        MonkeyTableData.getInstance(this).writeBooleanData("isStart", false);
        PublicMethod.killProcess("ps |grep com.android.commands.monkey", "system    ", " ");
        sendMonkeyReport();
    }

    /**
     * 发送报告
     */
    private void sendMonkeyReport() {
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


//                    PublicMethod.saveLog(TAG, "产生报告路径" + MONKEY_LOG_PATH);
//                    File monkeyFile = new File(MONKEY_LOG_PATH + ".zip");
//                    File monkeyOrignFile = new File(MONKEY_LOG_PATH);
//
//                    // 清除monkey日志
//                    if(monkeyFile.exists()){
//                        PublicMethod.saveLog(TAG, "存在旧monkey日志压缩包，删除中...");
//                        monkeyFile.delete();
//                    }
//
//                    // 清除monkey原始日志
//                    if(monkeyOrignFile.exists()){
//                        PublicMethod.saveLog(TAG, "存在旧monkey日志文件夹，删除中...");
//                        PublicMethod.deleteDirectory(MONKEY_LOG_PATH);
//                    }
//
//                    String newLogReport = PublicMethod.getFileDirNewDataPath(LOGREPORT_PATH);
//                    if(null != newLogReport){
//                        PublicMethod.saveLog(TAG, "拷贝LogReport日志");
//                        PublicMethod.copyLogReportFolder(newLogReport, MONKEY_LOG_PATH);
//                    }
//
//                    // MTK手机需要复制MTK日志
//                    if(isMtkPhone){
//                        String newMtkLog = PublicMethod.getNewMtkLogPath(MTK_LOG_PATH);
//                        if(null != newMtkLog){
//                            PublicMethod.saveLog(TAG, "MTK手机，拷贝MTK日志");
//                            PublicMethod.copyFolder(newMtkLog, MONKEY_LOG_PATH + DIVIDE_LINE
//                                    + "MtkLog" + DIVIDE_LINE + newMtkLog.replace(MTK_LOG_PATH, ""));
//                        }
//                    }
//
//                    // 压缩文件
//                    try {
//                        if(new File(MONKEY_LOG_PATH).exists()){
//                            PublicMethod.saveLog(TAG, "压缩LOG文件");
//                            ZipUtil.zip(MONKEY_LOG_PATH + DIVIDE_LINE, MONKEY_LOG_PATH + ".zip");
//                            PublicMethod.deleteDirectory(MONKEY_LOG_PATH);
//                            PublicMethod.saveLog(TAG, "压缩完毕，并删除旧MONKEY日志文件夹");
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    if(null == monkeyParams){
//                        monkeyParams = new HashMap<String, String>();
//                        monkeyParams.put(MODULE, BaseData.getInstance(getApplicationContext())
//                                .readStringData(SettingPreferenceKey.APP_TYPE));   // 写入业务类型
//                        monkeyParams.put(EMAIL, BaseData.getInstance(getApplicationContext())
//                                .readStringData(SettingPreferenceKey.EMAIL_ADDRESS).split("@")[0]);   // 写入邮件名
//                    }
//
//                    if(null == file){
//                        file = new HashMap<String, File>();
//                        File logFile = new File(MONKEY_LOG_PATH + ".zip");
//                        if(logFile.exists()){
//                            file.put(MONKEY_FLIE, logFile);
//                        }
//                    }
//                    sendReport();

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

                    Log.e("MonkeyService", data);
                    if(!isSuccess && tryTimes > 0){
                        PublicMethod.saveLog(TAG, "发送失败，尝试重发");
                        --tryTimes;
                        sendReport();
                    }else if(!isSuccess && 0 >= tryTimes){
                        PublicMethod.saveLog(TAG, "发送失败，code不等于200，尝试多次重发不成功，发送失败");
                        isSendOver = true;
                        stopSelf();
                    }else if(isSuccess && 0 < tryTimes){
                        if(0 == status){
                            PublicMethod.saveLog(TAG, "发送成功");
                            isSendOver = true;
                            stopSelf();
                        }else{
                            PublicMethod.saveLog(TAG, "发送失败，返回数据为：status = " + status + "，message = " + message);
                            --tryTimes;
                            sendReport();
                        }
                    }else if(isSuccess && 0 >= tryTimes) {
                        PublicMethod.saveLog(TAG, "发送失败，服务器返回status状态不对，尝试多次重发失败");
                        isSendOver = true;
                        stopSelf();
                    }else{
                        PublicMethod.saveLog(TAG, "其他未知发送情况");
                        isSendOver = true;
                        stopSelf();
                    }
                }
            });
        } catch (IOException e) {
            PublicMethod.saveLog(TAG, "发送异常，接口不通，延时10秒，再次尝试");
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            sendReport();
            e.printStackTrace();
        }
    }

    private void keepWakeUp(boolean isKeepWakeUp){
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

    private static OnMonkeyServiceListener onMonkeyServiceListener;

    public static void setOnMonkeyServiceListener(OnMonkeyServiceListener serviceListener){
        onMonkeyServiceListener = serviceListener;
    }

    public interface OnMonkeyServiceListener{
        void monkeyServiceListener();
    }

    @Override
    public void onDestroy() {

//        ServiceNotificationHelper.getInstance(this).notificationCancel(this, NOTIFICATION_ID);

        if(timer != null){
            timer.cancel();
            timer = null;
        }

        if(mMonkeyTimeTask != null){
            mMonkeyTimeTask.cancel();
            mMonkeyTimeTask = null;
        }

        if(!isSendOver){
            PublicMethod.saveLog(TAG, "服务被意外杀死\n");
        }else{
            PublicMethod.saveLog(TAG, "正常退出服务，耶！\n");
        }

        keepWakeUp(isKeepWakeUp);     // 恢复开关
        unregisterReceiver(monkeyReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
