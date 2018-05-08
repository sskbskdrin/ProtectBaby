package com.junhao.baby.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.junhao.baby.R;

/**
 * Created by ex-keayuan001 on 2017/10/24.
 *
 * @author ex-keayuan001
 */
public class LoadingDialog extends Dialog {
    private TextView mContentView;
    private Context mContext;
    private boolean isBack;

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.dialog);
        mContext = context;
        setContentView(R.layout.dialog_loading_layout);
        setCancelable(false);
        mContentView = (TextView) findViewById(R.id.dialog_loading_content);
        mContentView.setText("");
        setCanceledOnTouchOutside(false);
    }

    public void setMessage(@Nullable String content) {
        mContentView.setText(content);
    }

    public void setBack(boolean back) {
        isBack = back;
    }

    @Override
    public void onBackPressed() {
        if (!isBack) {
            super.onBackPressed();
        } else {
            if (mContext instanceof Activity) {
                ((Activity) mContext).onBackPressed();
            }
        }

    }
}
