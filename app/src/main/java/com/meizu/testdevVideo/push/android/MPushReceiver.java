/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     ohun@live.cn (夜色)
 */


package com.meizu.testdevVideo.push.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.SystemClock;

import com.meizu.testdevVideo.SuperTestApplication;
import com.meizu.testdevVideo.library.AlarmSetting;
import com.meizu.testdevVideo.service.SuperTestService;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.log.Logger;
import com.mpush.api.Constants;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

/**
 * Created by yxx on 2016/2/14.
 *
 * @author ohun@live.cn
 */
public final class MPushReceiver extends BroadcastReceiver {
    public static final String ACTION_HEALTH_CHECK = "com.mpush.HEALTH_CHECK";
    public static final String ACTION_NOTIFY_CANCEL = "com.mpush.NOTIFY_CANCEL";
    public static int delay = Constants.DEF_HEARTBEAT;
    public static int delayDefined = 5 * 1000;    // 5秒钟检测一次
    public static State STATE = State.UNKNOWN;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_HEALTH_CHECK.equals(action)) {            //处理心跳
            AlarmSetting.getInstance().setOnceAlarm(context, MPushReceiver.ACTION_HEALTH_CHECK, System.currentTimeMillis() + delayDefined);
//            Logger.d("心跳检测开始");
            if (MPush.I.checkInit(SuperTestApplication.getContext()).hasStarted()) {
                if (MPush.I.checkInit(SuperTestApplication.getContext()).client.isRunning()) {
                    if (MPush.I.checkInit(SuperTestApplication.getContext()).client.healthCheck()) {
//                        Logger.d("MPushService已启动，正在执行，健康检查完毕");
                    }else{
                        Logger.d("healthCheck检查不通过, Fuck");
//                        MPushStart.stopMpush();
                        MPushStart.startMush();
                    }
                }else{
                    Logger.d("MPushService已启动，但client未执行，Fuck");
                    Logger.file("MPushService已启动，但client未执行，Fuck", Logger.MPUSH);
//                    MPushStart.stopMpush();
                    MPushStart.startMush();
                }
            }else {
                // 重启服务
                Logger.d("hasStarted检测失败，重启服务");
//                MPushStart.stopMpush();
                MPushStart.startMush();
            }

            if(!PublicMethod.isServiceWorked(context, "com.meizu.testdevVideo.push.android.MPushService")){
                MPush.I.checkInit(SuperTestApplication.getContext()).startPush();
            }

            // 拉起SuperTestService服务
            if(!PublicMethod.isServiceWorked(context, "com.meizu.testdevVideo.service.SuperTestService")){
                context.startService(new Intent(context, SuperTestService.class));
            }
//            startAlarm(context, delay);
            // 自定义，60秒后检查心跳
        } else if (CONNECTIVITY_ACTION.equals(action)) {//处理网络变化
            if (hasNetwork(context)) {
                if (STATE != State.CONNECTED) {
                    STATE = State.CONNECTED;
                    if (MPush.I.checkInit(SuperTestApplication.getContext()).hasStarted()) {
                        MPush.I.checkInit(SuperTestApplication.getContext()).onNetStateChange(true);
                        MPush.I.resumePush();
                    } else {
                        MPush.I.checkInit(SuperTestApplication.getContext()).startPush();
                    }
                }
            } else {
                if (STATE != State.DISCONNECTED) {
                    STATE = State.DISCONNECTED;
                    MPush.I.checkInit(SuperTestApplication.getContext()).onNetStateChange(false);

                    //MPush.I.pausePush();
                    //cancelAlarm(context);//防止特殊场景下alarm没被取消
                }
            }

            if(!PublicMethod.isServiceWorked(context, "com.meizu.testdevVideo.push.android.MPushService")){
                MPush.I.checkInit(SuperTestApplication.getContext()).startPush();
            }
        } else if (ACTION_NOTIFY_CANCEL.equals(action)) {//处理通知取消
            Notifications.I.clean(intent);
        }
    }

    static void startAlarm(Context context, int delay) {
        Intent it = new Intent(MPushReceiver.ACTION_HEALTH_CHECK);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pi);
        MPushReceiver.delay = delay;
    }

    static void cancelAlarm(Context context) {
        Intent it = new Intent(MPushReceiver.ACTION_HEALTH_CHECK);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    public static boolean hasNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }
}
