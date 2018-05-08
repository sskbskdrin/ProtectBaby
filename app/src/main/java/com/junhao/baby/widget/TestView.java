package com.junhao.baby.widget;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.junhao.baby.R;

/**
 * Created by sskbskdrin on 2018/四月/8.
 */

public class TestView extends ImageView {
    public TestView(Context context) {
        this(context, null);
    }

    public TestView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.CENTER);
        setImageDrawable(new BottleDrawable(BitmapFactory.decodeResource(getResources(), R.drawable
                .home_d_total_dosage_down)));
        getDrawable().setLevel(900);
    }
}
