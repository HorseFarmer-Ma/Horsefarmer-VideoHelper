package com.meizu.testdevVideo.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.constant.CommonVariable;
import com.meizu.testdevVideo.activity.MainActivity;
import com.meizu.testdevVideo.util.shell.ShellUtils;
import com.meizu.testdevVideo.library.ToastHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenRecordService extends Service {
    NotificationManager NotificationManager;
    public ScreenRecordService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        showButtonNotify();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ToastHelper.addToast("开始录制屏幕，按home键返回桌面", getApplicationContext());
        // 录制屏幕指令：--time-limit 10 表示录制10秒
        new Thread(new Runnable() {
            @Override
            public void run() {
                ShellUtils.execCommand("screenrecord --time-limit "
                        + CommonVariable.screen_record_times + " /sdcard/" + getCurrentSystemTime() + ".mp4", false, false);

                ToastHelper.addToast("屏幕录制结束", getApplicationContext());
                Intent mIntent = new Intent(getApplicationContext(), ScreenRecordService.class);
                getApplicationContext().stopService(mIntent);   // 停止服务
            }   //
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy(){
        ToastHelper.addToast("视频录制完毕", getApplicationContext());
        this.NotificationManager.cancel(0);
        super.onDestroy();
    }


    /**
     * 获取当前时间
     * @return time
     */
    private String getCurrentSystemTime(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");   // 格式化当前时间
        String time = dateFormat.format(new Date());    // 获取当前时间
        return time;
    }

    /**
     * 通知栏
     */
    private void showButtonNotify() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("正在录制屏幕")//设置通知栏标题
                .setContentText("点我结束录制")
                .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL)) //设置通知栏点击意图,点击通知栏后消失
                        //  .setNumber(number) //设置通知集合的数量
                .setTicker("开始后台录制屏幕！！！") //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                        //  .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                        //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.mipmap.ic_app);//设置通知小ICON

        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(0, notify);
        this.NotificationManager = mNotificationManager;
    }


    // 通知栏点击意图
    public PendingIntent getDefalutIntent(int flags){
        Intent intent = new Intent(this, MainActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putString("object", "DNS切换");
//        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 1, intent, flags);
        return pendingIntent;
    }
}
