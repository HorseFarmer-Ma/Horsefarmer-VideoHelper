package com.meizu.testdevVideo.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.interports.iPublicConstants;

import java.io.IOException;


public class UiautomatorService extends Service {
    NotificationManager NotificationManager;
    public UiautomatorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showButtonNotify();
        Bundle bundle=intent.getExtras();  // 从Intent中获得Bundle对象
        final String fileName = bundle.getString("fileName");   // 从Bundle中获得数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 执行案例，包名默认“com.meizu.test”，类名默认“Sanity”
                try {
                    Runtime.getRuntime().exec(CommonVariable.performsCommand
                            .replace("jarPath", iPublicConstants.LOCAL_MEMORY
                                    + "SuperTest/JarDownload/" + fileName + ".jar")
                            .replace("className", "com.meizu.test.Sanity"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 通知栏
     */
    private void showButtonNotify() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("正在执行自动化脚本")//设置通知栏标题
                .setTicker("开始执行！") //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                        //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.mipmap.ic_app);//设置通知小ICON

        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(0, notify);
        this.NotificationManager = mNotificationManager;
    }
}
