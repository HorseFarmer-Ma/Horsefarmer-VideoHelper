package com.meizu.testdevVideo.library;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.meizu.testdevVideo.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by mxm on 2016/8/29.
 */
public class FrontServiceHelper {
    private static final Class<?>[] mSetForegroundSignature = new Class[] { boolean.class };
    private static final Class<?>[] mStartForegroundSignature = new Class[] { int.class , Notification.class };
    private static final Class<?>[] mStopForegroundSignature = new Class[] { boolean.class };
    private NotificationManager mNM;
    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    private boolean mReflectFlg = false;
    private Context mContext;

    public FrontServiceHelper(Context context){
        this.mContext = context;
    }

//    /**
//     * 通知栏开启
//     * @param title：标题
//     * @param secondTitle：副标题
//     * @param bombBox：状态栏弹框
//     * @param id：通知栏Id，根据这个判断是哪个通知栏
//     */
//    public void showButtonNotify(Class mClass, String title, String secondTitle, String bombBox, int id) {
//        try {
//            mStartForeground = mClass.getMethod("startForeground", mStartForegroundSignature);
//            mStopForeground = mClass.getMethod("stopForeground", mStopForegroundSignature);
//        } catch (NoSuchMethodException e) {
//            mStartForeground = mStopForeground = null;
//        }
//
//        try {
//            mSetForeground = getClass().getMethod( "setForeground", mSetForegroundSignature);
//        } catch (NoSuchMethodException e) {
//            throw new IllegalStateException( "OS doesn't have Service.startForeground OR Service.setForeground!");
//        }
//
//        Notification.Builder builder = new Notification.Builder(mContext);
//        builder.setSmallIcon(R.mipmap.ic_app)
//                .setContentTitle(title)
//                .setDefaults(Notification.DEFAULT_SOUND)
//                .setContentText(secondTitle)
//                .setTicker(bombBox);
//        Notification notification = builder.build();
//        startForegroundCompat(id, notification);
//    }
//
//    /**
//     * This is a wrapper around the new startForeground method, using the older
//     * APIs if it is not available.
//     */
//    void startForegroundCompat(int id, Notification notification) {
//        if (mReflectFlg) {
//            // If we have the new startForeground API, then use it.
//            if (mStartForeground != null) {
//                mStartForegroundArgs[0] = Integer.valueOf(id);
//                mStartForegroundArgs[1] = notification;
//                invokeMethod( mStartForeground, mStartForegroundArgs);
//                return;
//            }
//
//            // Fall back on the old API.
//            mSetForegroundArgs[0] = Boolean. TRUE;
//            invokeMethod( mSetForeground, mSetForegroundArgs);
//            mNM.notify(id, notification);
//        } else {
//           /*
//           * 还可以使用以下方法，当 sdk大于等于5时，调用 sdk现有的方法startForeground设置前台运行，
//           * 否则调用反射取得的 sdk level 5（对应Android 2.0）以下才有的旧方法setForeground设置前台运行
//           */
//            if (Build.VERSION. SDK_INT >= 5) {
//                startForeground(id, notification);
//            } else {
//                // Fall back on the old API.
//                mSetForegroundArgs[0] = Boolean. TRUE;
//                invokeMethod( mSetForeground, mSetForegroundArgs);
//                mNM.notify(id, notification);
//            }
//        }
//    }
//
//    /**
//     * This is a wrapper around the new stopForeground method, using the older
//     * APIs if it is not available.
//     */
//    private void stopForegroundCompat(int id) {
//        if (mReflectFlg) {
//            // If we have the new stopForeground API, then use it.
//            if ( mStopForeground != null) {
//                mStopForegroundArgs[0] = Boolean. TRUE;
//                invokeMethod( mStopForeground, mStopForegroundArgs);
//                return;
//            }
//
//            // Fall back on the old API. Note to cancel BEFORE changing the
//            // foreground state, since we could be killed at that point.
//            mNM.cancel(id);
//            mSetForegroundArgs[0] = Boolean. FALSE;
//            invokeMethod(mSetForeground, mSetForegroundArgs);
//        } else {
//           /*
//           * 还可以使用以下方法，当 sdk大于等于5时，调用 sdk现有的方法stopForeground停止前台运行， 否则调用反射取得的 sdk
//           * level 5（对应Android 2.0）以下才有的旧方法setForeground停止前台运行
//           */
//            if (Build.VERSION. SDK_INT >= 5) {
//                stopForeground(true);
//            } else {
//                // Fall back on the old API. Note to cancel BEFORE changing the
//                // foreground state, since we could be killed at that point.
//                mNM.cancel(id);
//                mSetForegroundArgs[0] = Boolean. FALSE;
//                invokeMethod(mSetForeground, mSetForegroundArgs);
//            }
//        }
//    }
//
//    void invokeMethod(Method method, Object[] args) {
//        try {
//            method.invoke(this, args);
//        } catch (InvocationTargetException e) {
//            Log. w("ApiDemos", "Unable to invoke method", e);
//        } catch (IllegalAccessException e) {
//            Log. w("ApiDemos" , "Unable to invoke method" , e);
//        }
//    }
//
//    /**
//     * 关闭通知栏
//     * @param id：通知栏Id
//     */
//    public void cancelShowNotify(int id) {
//        mNotificationManager.cancel(id);
//    }

}
