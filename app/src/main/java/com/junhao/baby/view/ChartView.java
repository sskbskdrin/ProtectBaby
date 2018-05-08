package com.junhao.baby.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.junhao.baby.R;
import com.junhao.baby.bean.StatisticsBean;
import com.junhao.baby.utils.CommonUtils;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by sskbskdrin on 2018/三月/2.
 */

public class ChartView extends View {
    private Paint mPaint;
    private Paint mTextPaint;
    private Paint mLinePaint;
    private Paint mPointPaint;

    private static int TEXT_R;

    private float[] mCoordinatePX;
    private String[] mCoordinateX;
    private int mCoordinateYCount = 5;
    private String mCoordinateUnitY = "";
    private double mMaxY;
    private double[] mData;
    private Item[] mValue;

    private int mWidth = 0;
    private int mHeight = 0;

    private int[] mLineY;
    private double[] mLineValueY;

    public ChartView(Context context) {
        this(context, null);
    }

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TEXT_R = CommonUtils.dp2px(context, 40);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        mPaint.setColor(0x60ffffff);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setPathEffect(new CornerPathEffect(28));
        mLinePaint.setColor(Color.CYAN);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(3);

        mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointPaint.setColor(Color.YELLOW);
        mPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_mini));
//        setData(new double[]{3, 5, 7, 8, 9, 4}, new float[]{0, 0.2f, 0.4f, 0.6f, 0.8f, 1f}, new
// String[]{"", "2.",
//                "3.", "4.", "5.", "6."});
    }

    public void setData(List<StatisticsBean.Data> list) {
        if (list == null) {
            return;
        }
        mData = new double[list.size()];
        mCoordinatePX = new float[list.size()];
        mCoordinateX = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            mData[i] = list.get(i).value;
            mCoordinatePX[i] = list.get(i).percent;
            mCoordinateX[i] = list.get(i).nameX;
        }
        mMaxY = 0;
        for (double temp : mData) {
            if (temp > mMaxY) {
                mMaxY = temp;
            }
        }
        if (mMaxY == 0) {
            mMaxY = 0.1f;
        }
        mWidth = 0;
        invalidate();
    }

    public void setData(double[] value, float[] index, String[] indexName) {
        mData = value;
        mCoordinatePX = index;
        mCoordinateX = indexName;
        for (double temp : value) {
            if (temp > mMaxY) {
                mMaxY = temp;
            }
        }
        if (mMaxY == 0) {
            mMaxY = 0.1f;
        }
        invalidate();
    }

    public void setCoordinate(double[] value, String unitY, String[] index) {
        mData = value;
        mCoordinateX = index;
        mCoordinateUnitY = unitY;
        for (double temp : value) {
            if (temp > mMaxY) {
                mMaxY = temp;
            }
        }
        if (mMaxY == 0) {
            mMaxY = 0.1f;
        }
        invalidate();
    }

    private void initPosition() {
        mLineY = new int[mCoordinateYCount];
        mLineValueY = new double[mCoordinateYCount];
        int spaceY = (mHeight - TEXT_R) / (mCoordinateYCount - 1);
        int mTopLine = spaceY * 2 / 3;
        float delta = spaceY * (mCoordinateYCount - 1);

        double stepY = mMaxY / (mCoordinateYCount - 1);
        for (int i = 0; i < mCoordinateYCount; i++) {
            mLineY[i] = mTopLine + i * spaceY;
            mLineValueY[i] = stepY * (mCoordinateYCount - i - 1);
        }
        mValue = new Item[mCoordinateX.length];
        for (int i = 0; i < mValue.length; i++) {
            Item item = new Item((mWidth - TEXT_R) * mCoordinatePX[i] + TEXT_R / 2, (float)
                    (mTopLine + delta * (mMaxY - mData[i]) / mMaxY), mData[i]);
            item.x += i == 0 ? 15 : i == mValue.length - 1 ? -15 : 0;
            mValue[i] = item;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getMeasuredWidth();
        if (mWidth != width) {
            mWidth = width;
            mHeight = getMeasuredHeight();
            if (mWidth == 0) {
                return;
            }
            initPosition();
        }
        if (mValue == null || mValue.length == 0) {
            return;
        }
        drawCoordinate(canvas);
        drawLine(canvas);
    }

    private void drawLine(Canvas canvas) {
        Path path = new Path();
        path.moveTo(mValue[0].x, mValue[0].y);
        for (int i = 1; i < mValue.length; i++) {
            path.lineTo(mValue[i].x, mValue[i].y);
        }
        canvas.drawPath(path, mLinePaint);
        DecimalFormat format = new DecimalFormat("#####0.000");
        for (int i = 0; i < mCoordinateX.length; i++) {
            if (TextUtils.isEmpty(mCoordinateX[i])) {
                continue;
            }
            drawText(canvas, format.format(mValue[i].value), mValue[i].x, mValue[i].y - 12,
                    AlignMode.BOTTOM_CENTER, mTextPaint);
            canvas.drawCircle(mValue[i].x, mValue[i].y, 10, mLinePaint);
            canvas.drawCircle(mValue[i].x, mValue[i].y, 8, mPointPaint);
        }
    }

    private void drawCoordinate(Canvas canvas) {
        DecimalFormat format = new DecimalFormat("#####0.000");
        for (int i = 0; i < mCoordinateYCount; i++) {
            drawDash(canvas, TEXT_R / 2, mLineY[i], mWidth - TEXT_R / 2, mLineY[i]);
//            drawText(canvas, format.format(mLineValueY[i]) + mCoordinateUnitY, 0, mLineY[i],
//                    AlignMode.LEFT_CENTER, mTextPaint);
        }

        for (int i = 0; i < mValue.length; i++) {
            if (TextUtils.isEmpty(mCoordinateX[i])) {
                continue;
            }
            float w = mTextPaint.measureText(mCoordinateX[i]);
//            if (mValue[i].x + w > mWidth) {
//                drawText(canvas, mCoordinateX[i], mWidth - TEXT_R / 2, mHeight, AlignMode
//                                .RIGHT_BOTTOM,
//                        mTextPaint);
//            } else {
            drawText(canvas, mCoordinateX[i], mValue[i].x, mHeight, AlignMode
                            .BOTTOM_CENTER,
                    mTextPaint);
//            }
        }
    }

    private void drawDash(Canvas canvas, int x0, int y0, int x1, int y1) {
        DashPathEffect pathEffect = new DashPathEffect(new float[]{30, 30}, 0);
        mPaint.setPathEffect(pathEffect);
        Path path = new Path();
        path.moveTo(x0, y0);
        path.lineTo(x1, y1);
        canvas.drawPath(path, mPaint);
    }

    private class Item {
        float x;
        float y;
        double value;

        Item(float x, float y, double value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }
    }

    public enum AlignMode {
        LEFT_TOP, LEFT_CENTER, LEFT_BOTTOM, TOP_CENTER, RIGHT_TOP, RIGHT_CENTER, RIGHT_BOTTOM,
        BOTTOM_CENTER, CENTER
    }

    private static void drawText(Canvas canvas, String text, float x, float y, AlignMode mode,
                                 Paint paint) {
        Rect r = new Rect();
        paint.getTextBounds(text, 0, text.length(), r);
        int height = r.height();
        y -= r.bottom;
        switch (mode) {
            case LEFT_TOP:
                paint.setTextAlign(Align.LEFT);
                y += height;
                break;
            case LEFT_CENTER:
                paint.setTextAlign(Align.LEFT);
                y += height / 2;
                break;
            case LEFT_BOTTOM:
                paint.setTextAlign(Align.LEFT);
                break;
            case TOP_CENTER:
                paint.setTextAlign(Align.CENTER);
                y += height;
                break;
            case RIGHT_TOP:
                paint.setTextAlign(Align.RIGHT);
                y += height;
                break;
            case RIGHT_CENTER:
                paint.setTextAlign(Align.RIGHT);
                y += height / 2;
                break;
            case RIGHT_BOTTOM:
                paint.setTextAlign(Align.RIGHT);
                break;
            case BOTTOM_CENTER:
                paint.setTextAlign(Align.CENTER);
                break;
            case CENTER:
                paint.setTextAlign(Align.CENTER);
                y += height / 2;
                break;
        }
        canvas.drawText(text, x, y, paint);
    }
}
