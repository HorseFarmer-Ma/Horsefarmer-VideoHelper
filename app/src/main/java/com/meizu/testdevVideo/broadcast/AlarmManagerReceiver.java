package com.meizu.testdevVideo.broadcast;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.constant.SettingPreferenceKey;
import com.meizu.testdevVideo.interports.iPerformsKey;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.sharepreference.PerformsData;
import com.meizu.testdevVideo.util.wifi.WifiUtil;

import java.io.IOException;

/**
 * 纯净后台需要做特殊处理
 * Created by maxueming on 2016/12/14.
 */
public class AlarmManagerReceiver extends BroadcastReceiver{
    private AlarmManager alarmManager = null;
    private PendingIntent operation = null;
    private Intent alarmIntent = null;
    private String TAG = AlarmManagerReceiver.class.getSimpleName();
    private SharedPreferences settingSharedPreferences = null;
    private SharedPreferences.Editor editor = null;
    private Context mContext = null;
    private Handler mHandler = null;



    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = (mContext == null)? context : mContext;
        String action = intent.getAction();

        // 设置闹钟
        if(action.equals("st.action.set.performs.alarm")){
            // 设置纯净后台标志位为true，标志着熄屏
            PublicMethod.saveLog("纯净后台，设置休眠");
            PerformsData.getInstance(context).writeBooleanData(iPerformsKey.isPurebackstageSet, true);
            long alarmTime = intent.getIntExtra("alarmTime", 0);
            PublicMethod.saveLog("纯净后台，休眠时长：" + alarmTime);
            Log.d(TAG, "设置闹钟：时间" + alarmTime);
            String packageName = intent.getStringExtra("packageName");
            PublicMethod.saveLog("纯净后台，设置包名：" + packageName);
            Log.d(TAG, "设置闹钟：包名" + packageName);
            String step = intent.getStringExtra("step");
            PublicMethod.saveLog("纯净后台，设置步骤：" + step);
            Log.d(TAG, "设置闹钟：步骤" + step);
            // 设置各项参数
            PerformsData.getInstance(context).writeStringData(iPerformsKey.caseName, step);
            PerformsData.getInstance(context).writeStringData(iPerformsKey.packageName, packageName);

            // 获取AlarmManager对象,创建Intent对象，action为android.intent.action.ALARM_RECEIVER
            alarmManager = (null == alarmManager)? (AlarmManager) context.getSystemService(Context.ALARM_SERVICE) : alarmManager;
            alarmIntent = (null == alarmIntent)? new Intent("android.intent.action.ALARM_RECEIVER") : alarmIntent;
            operation = (null == operation)? PendingIntent.getBroadcast(context, 0, alarmIntent, 0) : operation;
            long startAlarmTime = System.currentTimeMillis() + alarmTime;
            Log.d(TAG, String.valueOf(startAlarmTime));
            // 适配版本，Android 4.3以后的用setExact
            if(PublicMethod.isKitKatOrLater()){
                Log.d(TAG, "KitKatOrLater!");
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, startAlarmTime, operation);
            }else{
                Log.d(TAG, "Not KitKatOrLater!");
                alarmManager.set(AlarmManager.RTC_WAKEUP, startAlarmTime, operation);
            }
        }

        // 收到闹钟
        if(action.equals("android.intent.action.ALARM_RECEIVER")){
            Log.d(TAG, "设定时间到达，唤醒屏幕...");
            // 亮屏
            PublicMethod.wakeUpAndUnlock(context);
            // 设置屏幕唤醒开关为开
            PublicMethod.saveLog("纯净后台，时间到达，唤醒屏幕");
            settingSharedPreferences = ((settingSharedPreferences ==  null)?
                    PreferenceManager.getDefaultSharedPreferences(context) : settingSharedPreferences);
            editor = (editor == null) ? settingSharedPreferences.edit() : editor;
            editor.remove(SettingPreferenceKey.KEEP_WAKEUP);
            editor.remove(SettingPreferenceKey.LOCK_WIFI_TYPE);
            editor.putBoolean(SettingPreferenceKey.KEEP_WAKEUP, true);
            // 强制连接特定WIFI进行发送报告
            editor.putString(SettingPreferenceKey.LOCK_WIFI_TYPE, "MZ-Inweb-Test");
            editor.apply();
            PublicMethod.lockWifi(settingSharedPreferences, context);
            mHandler = (mHandler == null)? new Handler() : mHandler;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Runtime.getRuntime().exec("am broadcast -a com.meizu.logreport.adb_cmd --ei action 1 --ei type 0 --ez zip false");
                        Thread.sleep(8 * Constants.TIME.SECOND);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    long currentTime = System.currentTimeMillis();
                    while (System.currentTimeMillis() - currentTime < 10 * Constants.TIME.MINUTE){
                        // 未连接WIFI
                        if(!WifiUtil.isWifiConnected(mContext)){
                            try {
                                Thread.sleep(10 * Constants.TIME.SECOND);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if(!PublicMethod.isServiceWorked(mContext, "com.meizu.testdevVideo.service.WifiLockService")){
                                PublicMethod.lockWifi(settingSharedPreferences, mContext);
                            }
                        }else {
                            PublicMethod.saveLog("纯净后台，执行完毕，发送收集结果广播");
                            try {
                                Runtime.getRuntime().exec("am broadcast -a action.st.performs.test.over" +
                                        " --es fileName " + "pure" + " --es pkg " + PerformsData.getInstance(mContext).readStringData(iPerformsKey.packageName)
                                        + " --es caseName " + PerformsData.getInstance(mContext).readStringData(iPerformsKey.caseName) + " --es result true");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }

                    if(!WifiUtil.isWifiConnected(mContext)){
                        PublicMethod.saveLog("纯净后台，WiFi连接不上，请检查周围网络情况");
                    }
                }
            }).start();
        }
    }

}
