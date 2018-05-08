package com.junhao.baby.widget;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.animation.LinearInterpolator;

import java.util.Random;

/**
 * Created by sskbskdrin on 2018/四月/8.
 */

public class BottleDrawable extends Drawable implements Animatable {
    /**
     * 振幅
     */
    private int A = 4;
    /**
     * 波形的颜色
     */
    private int waveColor = 0xFFFC6498;

    /**
     * 初相
     */
    private float φ = (float) (Math.PI / 2);

    /**
     * 波形移动的速度
     */
    private float waveSpeed = 5f;

    /**
     * 角速度
     */
    private double ω;

    /**
     * 开始位置相差多少个周期
     */
    private double startPeriod;

    /**
     * 是否直接开启波形
     */
    private boolean waveStart = true;

    private Path path;
    private Paint paint;
    private Paint pointPaint;

    private static final int SIN = 0;
    private static final int COS = 1;

    private int waveType;

    private static final int TOP = 0;
    private static final int BOTTOM = 1;

    private int waveFillType;

    private ValueAnimator valueAnimator;

    private int mWidth;
    private int mHeight;

    private int top;
    int top_;
    private int line;
    private int bottom;
    private float scale;
    private Random random;

    private PointF[] points;

    private Bitmap bgBitmap;

    public BottleDrawable(Bitmap bitmap) {
        setBitmap(bitmap);
        initPaint();
        random = new Random();
        points = new PointF[3];
        points[0] = new PointF(80 * scale, top);
        points[1] = new PointF(100 * scale, top);
        points[2] = new PointF(mWidth - 80 * scale, top);
        initAnimation();
    }

    private void initPaint() {
        path = new Path();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(waveColor);

        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    private void setBitmap(Bitmap bitmap) {
        bgBitmap = bitmap;
        mWidth = bgBitmap.getWidth();
        mHeight = bgBitmap.getHeight();
        scale = mHeight / 276f;
        top = (int) (20f * scale);
        top_ = (int) (32f * scale);
        bottom = mHeight - top;
        ω = 4 * Math.PI / mWidth;
    }

    @Override
    protected boolean onLevelChange(int level) {
        if (level < 0) {
            level = 0;
        } else if (level > 10000) {
            level = 10000;
        }
        line = bottom - (bottom - top) * level / 10000;
        return true;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.translate((getBounds().width() - mWidth) / 2, (getBounds().height() - mHeight) / 2);
        if (bgBitmap != null) {
            canvas.drawBitmap(bgBitmap, 0, 0, paint);
        }
        fillBottom(canvas);
        drawPoint(canvas);
    }

    private void drawPoint(Canvas canvas) {
        points[0].y += 0.9f;
        points[1].y += 1.5f;
        points[2].y += 0.5f;
        for (PointF p : points) {
            if (p.y > line) {
                p.y = top;
            }
        }
        pointPaint.setColor(0xFFFECA60);
        canvas.drawCircle(points[0].x, points[0].y, 10, pointPaint);
        pointPaint.setColor(0xFF7DC866);
        canvas.drawCircle(points[1].x, points[1].y, 15, pointPaint);
        pointPaint.setColor(0xFF70D3ED);
        canvas.drawCircle(points[2].x, points[2].y, 8, pointPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    /**
     * 填充波浪下面部分
     */
    private void fillBottom(Canvas canvas) {
        φ -= waveSpeed / 100;
        float y = line;

        path.reset();

        int offset = getOffsetX((int) y);
        path.moveTo(offset, y);
        for (float x = offset; x <= mWidth - offset; x += 5) {
            y = (float) (A * Math.cos(ω * x + φ + Math.PI * startPeriod) + line);
            path.lineTo(x, y);
        }
        addTop(path, (int) y);
        path.close();

        canvas.drawPath(path, paint);
    }

    private void addTop(Path path, int y) {
        if (y > mHeight - (30 * scale)) {
            addBottom(path, y);
        } else if (y > 30 * scale) {
            addMiddle(path, y);
        } else {
            path.lineTo(mWidth - 44 * scale, y);
            path.lineTo(mWidth - 44 * scale, 30 * scale);
            path.lineTo(mWidth - 64 * scale, 30 * scale);
            addMiddle(path, (int) (32 * scale));
            path.lineTo(64 * scale, 30 * scale);
            path.lineTo(44 * scale, 30 * scale);
            path.lineTo(44 * scale, y);
        }
    }

    private void addMiddle(Path path, int y) {
        if (y > mHeight - (30 * scale)) {
            addBottom(path, y);
        } else {
            int offsetX = getOffsetX(y);
            path.lineTo(mWidth - offsetX, y);//y=30 x=64 y=236 x=20
            path.lineTo(mWidth - 20 * scale, mHeight - 30 * scale);
            addBottom(path, (int) (mHeight - 30 * scale));
            path.lineTo(20 * scale, mHeight - 30 * scale);
            path.lineTo(offsetX, y);
        }
    }

    private void addBottom(Path path, int y) {
        path.lineTo(mWidth - 20 * scale, y);
        path.quadTo(mWidth - 20 * scale, mHeight - 20 * scale, mWidth - 30 * scale, mHeight - 20
                * scale);
        path.lineTo(30 * scale, mHeight - 20 * scale);
        path.quadTo(20 * scale, mHeight - 20 * scale, 20 * scale, y);
    }

    private int getOffsetX(int y) {
        if (y > mHeight - (30 * scale)) {
            return (int) ((20 + 256 - y) * scale);
        } else if (y > 30 * scale) {
            return (int) ((20 + (246 - y) / 216f * 44) * scale);
        } else {
            return (int) (45 * scale);
        }
    }

    private void initAnimation() {
        valueAnimator = ValueAnimator.ofInt(0, mWidth);
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                /**
                 * 刷新页面调取onDraw方法，通过变更φ 达到移动效果
                 */
                invalidateSelf();
            }
        });
        if (waveStart) {
            valueAnimator.start();
        }
    }

    @Override
    public void start() {
        if (!valueAnimator.isRunning()) {
            valueAnimator.start();
        }
    }

    @Override
    public void stop() {
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
    }

    @Override
    public boolean isRunning() {
        return valueAnimator.isRunning();
    }
}
