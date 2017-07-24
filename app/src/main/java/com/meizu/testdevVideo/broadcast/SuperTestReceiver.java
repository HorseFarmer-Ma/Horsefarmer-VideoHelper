package com.meizu.testdevVideo.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.interports.SuperTestCallBack;
import com.meizu.testdevVideo.library.AlarmSetting;
import com.meizu.testdevVideo.library.ToastHelper;
import com.meizu.testdevVideo.service.SuperTestService;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.Logger;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;

import java.util.Calendar;

/**
 * 接收开机广播
 * Created by mxm on 2016/8/30.
 */
public class SuperTestReceiver extends BroadcastReceiver {
    private static String TAG = SuperTestReceiver.class.getSimpleName();
    private static SuperTestCallBack mSuperTestCallBack;

    public static void setSuperTestNotification(SuperTestCallBack superTestCallBack){
        mSuperTestCallBack = superTestCallBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "触发SuperTestService后台监听器！!");
        String action = intent.getAction();

        if(action.equals("android.intent.action.BOOT_COMPLETED")){
            if(MonkeyTableData.isMonkeyStart(context)){
                // 还有不到2分钟就到定时停止monkey时间或定时停止时间已过当前时间，设置2分钟以后停止monkey
                long monkeyStopTime = MonkeyTableData.getMonkeyStopTime(context);
                if(monkeyStopTime < System.currentTimeMillis() + 2 * Constants.TIME.SECONDS_OF_MINUTE * Constants.TIME.MILLS_OF_SECOND){
                    ToastHelper.addToast("将在大约2分钟后停止Monkey，收集报告", context);
//                    AlarmSetting.getInstance().setOnceAlarm(context,
//                            Constants.Monkey.ACTION_KILL_MONKEY, System.currentTimeMillis()
//                                    + 2 * Constants.TIME.SECONDS_OF_MINUTE * Constants.TIME.MILLS_OF_SECOND);
                }else{
                    AlarmSetting.getInstance().setOnceAlarm(context,
                            Constants.Monkey.ACTION_KILL_MONKEY, monkeyStopTime);
                }
            }

            // 定时跑，执行时长不等于0的话，才设置定时任务，当天定时循环时间
            if(MonkeyTableData.getAlarmRunTime(context) != 0){
                Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
                long currentHourMinuteToMills = (c.get(Calendar.HOUR_OF_DAY)  * Constants.TIME.MINUTES_OF_HOUR + c.get(Calendar.MINUTE))
                        * Constants.TIME.SECONDS_OF_MINUTE * Constants.TIME.MILLS_OF_SECOND;
                long alarmStartTime = MonkeyTableData.getAlarmStartTime(context);
                long leaveTime = alarmStartTime - currentHourMinuteToMills;

                long setAlarmStartTime = leaveTime > 0 ? (System.currentTimeMillis() + leaveTime)
                        : (System.currentTimeMillis() + leaveTime + PublicMethod.getDayMills());

                AlarmSetting.getInstance().setRepeatAlarm(context,
                        Constants.Monkey.ACTION_SET_MONKEY_RUN_REPEAT_TASK, setAlarmStartTime);
            }

            wakeUpSuperTestService(context, "启动手机，触发启动服务");
        }


        if(action.equals("st.action.monkey.service.destroy")
                || action.equals(Intent.ACTION_USER_PRESENT)
                || action.equals(Intent.ACTION_PACKAGE_RESTARTED)){
            wakeUpSuperTestService(context, "服务意外销毁，启动服务");
        }

        if(action.equals("st.action.control.notification")){
            mSuperTestCallBack.changeNotification(intent.getBooleanExtra("isOpen", true));
        }
    }

    /**
     * 重启服务函数
     * @param context 上下文
     * @param wakeUpReason 重启原因
     */
    private synchronized void wakeUpSuperTestService(Context context, String wakeUpReason){
        if(!PublicMethod.isServiceWorked(context, "com.meizu.testdevVideo.service.SuperTestService")){
            Logger.file(wakeUpReason, Logger.SUPER_TEST);
            Intent service = new Intent(context, SuperTestService.class);
            context.startService(service);
        }
    }
}
