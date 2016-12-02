package com.meizu.testdevVideo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.meizu.testdevVideo.constant.CommonVariable;

import java.util.TimerTask;

import perflib.cmdparse.GetFPS;

public class GetFpsService extends Service {
    private String packageActivityName = null;
    public GetFpsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        packageActivityName = intent.getStringExtra("packageActivityName");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 采集FPS定时器
     */
    TimerTask mTimeTask = new TimerTask() {
        @Override
        public void run() {
            Log.e("GetFpsService", "执行Fps计算");
            GetFPS.clear();
            GetFPS.clearBuffer("com.meizu.media.video/com.meizu.media.video.VideoMainActivity");
            GetFPS.dumpFrameLatency("com.meizu.media.video/com.meizu.media.video.VideoMainActivity",true);
            CommonVariable.Fps = GetFPS.getFrameRate();
        }
    };
}
