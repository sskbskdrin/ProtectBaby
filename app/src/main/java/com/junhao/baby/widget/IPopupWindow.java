package com.junhao.baby.widget;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

/**
 * Created by ex-keayuan001 on 2017/9/5.
 */
public class IPopupWindow extends PopupWindow implements View.OnTouchListener {
    protected View mRootView;
    private FrameLayout mParentView;
    private int mBackgroundColor = 0;

    public IPopupWindow(View contentView) {
        super(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                .MATCH_PARENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setAnimationStyle(0);
        setTouchInterceptor(this);
        super.setBackgroundDrawable(new ColorDrawable(0));
    }

    @Override
    public void setContentView(View contentView) {
        if (contentView != null) {
            mRootView = contentView;
            mParentView = new FrameLayout(contentView.getContext());
            mParentView.addView(contentView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout
                    .LayoutParams.MATCH_PARENT);
            super.setContentView(mParentView);
        }
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        mParentView.setBackground(background);
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    @Override
    public void showAsDropDown(View anchor) {
        showAsDropDown(anchor, 0, 0);
    }

    @Override
    public void showAsDropDown(View anchor, int x, int y) {
        showAsDropDown(anchor, x, y, Gravity.TOP | Gravity.START);
    }

    @Override
    public void showAsDropDown(View anchor, int x, int y, int gravity) {
//        mRootView.startAnimation(AnimationUtils.loadAnimation(anchor.getContext(), R.anim
//                .slide_in_top));
        ValueAnimator colorAnim = ObjectAnimator.ofInt(mParentView, "backgroundColor",
                0x00ffffff, mBackgroundColor);
        colorAnim.setDuration(300);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(0);
        colorAnim.start();

        int[] a = new int[2];
        anchor.getLocationInWindow(a);

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mRootView.getLayoutParams();
        lp.gravity = gravity;
        lp.leftMargin = x + a[0];
        lp.topMargin = y + a[1];
        View newAnchor = anchor.getRootView();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            super.showAsDropDown(newAnchor, 0, 0);
        } else if (Build.VERSION.SDK_INT < 24) {
            showAtLocation(newAnchor, Gravity.TOP | Gravity.START, 0, 0);
        } else {
            showAtLocation(newAnchor, Gravity.TOP | Gravity.START, 0, 0);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if ((x < mRootView.getLeft() || x >= mRootView.getRight()) || (y < mRootView.getTop()
            ) || (y >= mRootView
                    .getBottom())) {
                dismiss();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            dismiss();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T getView(int id) {
        return (T) mRootView.findViewById(id);
    }
}
