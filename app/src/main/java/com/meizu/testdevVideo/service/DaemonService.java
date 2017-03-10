package com.meizu.testdevVideo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.meizu.testdevVideo.constant.Constants;
import com.meizu.testdevVideo.library.ServiceNotificationHelper;
import com.meizu.testdevVideo.util.PublicMethod;
import com.meizu.testdevVideo.util.sharepreference.MonkeyTableData;
import com.meizu.testdevVideo.util.shell.ShellUtils;

import java.util.Timer;
import java.util.TimerTask;

public class DaemonService extends Service {
    private Timer mTimer;

    private static final int NOTIFICATION_ID = 103;

    public DaemonService() {
    }


    @Override
    public void onCreate() {
//        ServiceNotificationHelper.getInstance(this).notification(
//                NOTIFICATION_ID, this, "SuperTest", "进程守护服务");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
//        ServiceNotificationHelper.getInstance(this)
//                .notificationCancel(this, NOTIFICATION_ID);

        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }

        if(mDaemonTask != null){
            mDaemonTask.cancel();
            mDaemonTask = null;
        }
        super.onDestroy();
    }

    /**
     * 定时器初始化
     */
    private void timerInit() {
        mTimer = new Timer();
        mTimer.schedule(mDaemonTask, 10 *
                Constants.TIME.MINUTE, Constants.TIME.MINUTE);
    }

    /**
     * 定时器
     */
    private TimerTask mDaemonTask = new TimerTask() {
        @Override
        public void run() {
            if (!PublicMethod.isServiceWorked(DaemonService.this,
                    "com.meizu.testdevVideo.service.SuperTestService")) {
                startService(new Intent(DaemonService.this, SuperTestService.class));
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
