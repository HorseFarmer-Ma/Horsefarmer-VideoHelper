package com.meizu.testdevVideo.library;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/**
 * 悬浮窗协助函数
 * Created by mxm on 2016/8/28.
 */
public class WindowManagerHelper {
    private WindowManager wm = null;       // 悬浮窗管理
    private WindowManager.LayoutParams wmParams = null;     // 悬浮窗参数
    private Context mContext;

    public WindowManagerHelper(Context context){
        this.mContext = context;
    }

    /**
     * 创建悬浮框按钮 设置悬浮框的大小、位置、透明度等各项参数
     * @param v:视图
     * @param i：视图的宽度大小
     */
    public void createView(View v, int i, boolean isScreenOn, boolean isTouchable) {
        int mFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mFlags = (isScreenOn? (mFlags | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) : mFlags);
        mFlags = (isTouchable? mFlags : (mFlags | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE));
        wmParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                mFlags, PixelFormat.TRANSPARENT);

        // 设置window type
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 调整悬浮窗显示的停靠位置为右侧置顶
        wmParams.gravity = Gravity.END | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        // 设置悬浮窗口长宽数据
        wmParams.width = i;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // 设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.alpha = 10;

        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.addView(v, wmParams);
    }

    /**
     * 重新设置悬浮框属性 LayoutParams.FLAG_NOT_TOUCHABLE：没有焦点,无法点击
     */
    public void upDateView(View v, int i, boolean isScreenOn, boolean isTouchable) {
        int mFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mFlags = (isScreenOn? (mFlags | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) : mFlags);
        mFlags = (isTouchable? mFlags : (mFlags | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE));
        wmParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                mFlags, PixelFormat.TRANSPARENT);

        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.END | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = i;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.alpha = 10;

        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.addView(v, wmParams);
    }

    /**
     * 改变View
     * @param oldView：旧视图
     * @param newView：新视图
     * @param width：宽度
     * @param isScreenOn：保持亮屏
     */
    public void changeView(View oldView, View newView, int width, boolean isScreenOn) {
        wm.removeView(oldView);
        wm.removeView(newView);
        upDateView(oldView, width, isScreenOn, false);
        createView(newView, width, isScreenOn, true);
    }

    /**
     * 移除通知栏
     * @param v：视图
     */
    public void removeView(View v){
        wm.removeView(v);
    }

}
