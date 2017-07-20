package com.meizu.testdevVideo.library;

import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

/**
 * 动画相关
 * Created by maxueming on 2016/12/19.
 */
public class AnimationHelper {

    public static AnimationHelper mInstance;
    public static synchronized AnimationHelper getInstance() {
        if(mInstance == null){
            mInstance = new AnimationHelper();
        }
        return mInstance;
    }

    /**
     * 返回缩放动画实例
     * @param fromX Horizontal scaling factor to apply at the start of the animation
     * @param toX Horizontal scaling factor to apply at the end of the animation
     * @param fromY Vertical scaling factor to apply at the start of the animation
     * @param toY Vertical scaling factor to apply at the end of the animation
     * @param startOffset 延迟开始动画时间
     * @param repeatCount 重复次数
     * @param runTime 执行时间
     * @param fillAfter 执行完是否停留在执行完的状态
     * @param pivotXValue 动画相对于物件的X坐标的开始位置
     * @param pivotYValue 动画相对于物件的Y坐标的开始位置
     * @return animation
     */
    public ScaleAnimation getScaleAnimation(float fromX, float toX, float fromY, float toY,
                                            long startOffset, int repeatCount, long runTime,
                                            boolean fillAfter, float pivotXValue, float pivotYValue){
        ScaleAnimation animation = new ScaleAnimation(fromX, toX, fromY, toY,
                Animation.RELATIVE_TO_SELF, pivotXValue, Animation.RELATIVE_TO_SELF, pivotYValue);
        animation.setDuration(runTime);
        /** 常用方法 */
        animation.setRepeatCount(repeatCount);//设置重复次数
        animation.setFillAfter(fillAfter);//动画执行完后是否停留在执行完的状态
        animation.setStartOffset(startOffset);//执行前的等待时间
        return animation;
    }


}
