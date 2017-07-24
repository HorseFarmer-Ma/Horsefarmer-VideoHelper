package com.meizu.widget.floatingwindow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import com.meizu.testdevVideo.R;
import com.meizu.testdevVideo.library.DensityUtil;

/**
 * 悬浮球View
 * Created by maxueming on 2017/6/6.
 */
public class FloatBall extends View {

    public int width;
    public int height;
    //默认显示的文本
    private String text = "性能";
    //是否在拖动
    private boolean isDrag;
    private Paint ballPaint;
    private Paint ballPaint2;
    private Paint textPaint;
//    private Bitmap bitmap;

    public FloatBall(Context context) {
        super(context);
        init(context);
    }

    public FloatBall(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FloatBall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        width = DensityUtil.dip2px(context, 50);
        height = DensityUtil.dip2px(context, 50);

        ballPaint = new Paint();
        ballPaint.setColor(context.getResources().getColor(R.color.ThemeColor));
        ballPaint.setAntiAlias(true);

        ballPaint2 = new Paint();
        ballPaint2.setColor(context.getResources().getColor(R.color.dark_red));
        ballPaint2.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setTextSize(DensityUtil.dip2px(context, 15));
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);

//        Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.ic_choose_app);
//        //将图片裁剪到指定大小
//        bitmap = Bitmap.createScaledBitmap(src, width, height, true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isDrag) {
            canvas.drawCircle(width / 2, height / 2, width / 2, ballPaint);
        } else {
            canvas.drawCircle(width / 2, height / 2, width / 2, ballPaint2);
        }
        float textWidth = textPaint.measureText(text);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float dy = -(fontMetrics.descent + fontMetrics.ascent) / 2;
        canvas.drawText(text, width / 2 - textWidth / 2, height / 2 + dy, textPaint);
    }

    //设置当前移动状态
    public void setDragState(boolean isDrag) {
        this.isDrag = isDrag;
        invalidate();
    }

}
