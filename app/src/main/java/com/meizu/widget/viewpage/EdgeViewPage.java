package com.meizu.widget.viewpage;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by maxueming on 2016/11/10.
 */
public class EdgeViewPage extends ViewPager implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.70f;     // 1 → 0 最大至最小
    private static final float MIN_ALPHA = 0.5f;      // 1 → 0 不透明至透明

    private String TAG = EdgeViewPage.class.getSimpleName();

    public EdgeViewPage(Context context) {
        super(context);
    }

    public EdgeViewPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
    }


//    @Override
//    public void transformPage(View page, float position) {
//        if (position <= 0) {
//            //从右向左滑动为当前View
//
//            //设置旋转中心点；
//            ViewHelper.setPivotX(page, page.getMeasuredWidth());
//            ViewHelper.setPivotY(page, page.getMeasuredHeight() * 0.5f);
//
//            //只在Y轴做旋转操作
//            ViewHelper.setRotationY(page, 90f * position);
//        } else if (position <= 1) {
//            //从左向右滑动为当前View
//            ViewHelper.setPivotX(page, 0);
//            ViewHelper.setPivotY(page, page.getMeasuredHeight() * 0.5f);
//            ViewHelper.setRotationY(page, 90f * position);
//        }
//    }



    @Override
    public void transformPage(View page, float position) {
        if (position < -1 || position > 1) {
            page.setAlpha(MIN_ALPHA);
            page.setScaleX(MIN_SCALE);
            page.setScaleY(MIN_SCALE);
        } else if (position <= 1) { // [-1,1]
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            if (position < 0) {
                float scaleX = 1 + 0.1f * position;
                Log.d("google_lenve_fb", "transformPage: scaleX:" + scaleX);
                page.setScaleX(scaleX);
                page.setScaleY(scaleX);
            } else {
                float scaleX = 1 - 0.1f * position;
                page.setScaleX(scaleX);
                page.setScaleY(scaleX);
            }
            page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        }
    }

}
