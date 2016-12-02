package com.meizu.testdevVideo.library;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;
import com.meizu.testdevVideo.R;

/**
 * 服务类操作协助函数
 * Created by mxm on 2016/8/28.
 */
public class ServiceHelper {
    private Context mContext;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    public ServiceHelper(Context context){
        this.mContext = context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext);
    }


    /**
     * 通知栏开启
     * @param title：标题
     * @param secondTitle：副标题
     * @param bombBox：状态栏弹框
     * @param id：通知栏Id，根据这个判断是哪个通知栏
     */
    public void showButtonNotify(String title, String secondTitle, String bombBox, int id) {
        mBuilder.setContentTitle(title)//设置通知栏标题
                .setContentText(secondTitle)
                .setTicker(bombBox) //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                        //  .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setDefaults(Notification.DEFAULT_SOUND)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合
                        //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
                .setSmallIcon(R.mipmap.ic_app);//设置通知小ICON

        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(id, notify);
    }

    /**
     * 关闭通知栏
     * @param id：通知栏Id
     */
    public void cancelShowNotify(int id) {
        mNotificationManager.cancel(id);
    }

}
