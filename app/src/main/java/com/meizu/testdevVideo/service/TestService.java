package com.meizu.testdevVideo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

import perflib.cmdparse.GetFPS;

public class TestService extends Service {
    private Timer testTime = null;
    private TimerTask fpsTimeTask = null;
    public TestService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        testTime = new Timer();
        fpsTimeTask = new TimerTask() {
            @Override
            public void run() {
                Log.e("TestService", "123456");
                GetFPS.clear();
                GetFPS.clearBuffer("com.meizu.media.video/com.meizu.media.video.VideoMainActivity");
                GetFPS.dumpFrameLatency("com.meizu.media.video/com.meizu.media.video.VideoMainActivity",true);
                double fps = GetFPS.getFrameRate();
                Log.e("TestService", String.valueOf(fps));
            }
        };
        testTime.schedule(fpsTimeTask, 0, 100);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if(testTime != null){
            testTime = null;
        }

        if(fpsTimeTask != null){
            fpsTimeTask = null;
        }
        super.onDestroy();
    }
}
