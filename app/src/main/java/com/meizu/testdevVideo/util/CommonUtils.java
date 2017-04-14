package com.meizu.testdevVideo.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.animation.AccelerateInterpolator;

import com.meizu.widget.viewpage.FixedSpeedScroller;

import java.lang.reflect.Field;

/**
 * Created by maxueming on 2016/12/30.
 */
public class CommonUtils {

    private static FixedSpeedScroller mScroller = null;
    /**
     * 设置ViewPager的滑动时间
     * @param context
     * @param viewpager ViewPager控件
     * @param DurationSwitch 滑动延时
     */
    public static void controlViewPagerSpeed(Context context, ViewPager viewpager, int DurationSwitch) {
        try {
            Field mField;

            mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);

            mScroller = new FixedSpeedScroller(context,
                    new AccelerateInterpolator());
            mScroller.setmDuration(DurationSwitch);
            mField.set(viewpager, mScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
