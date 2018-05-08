package com.junhao.baby.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.junhao.baby.R;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */

public class InputDialog extends Dialog {

    private TextView mTitleView;
    private EditText mContentView;

    private OnClickListener mCancelListener;
    private OnClickListener mOkListener;

    public InputDialog(@NonNull Context context) {
        super(context, R.style.ios_dialog_style);
        setContentView(R.layout.dialog_input_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        mTitleView = (TextView) findViewById(R.id.dialog_title);
        mContentView = (EditText) findViewById(R.id.dialog_content);

        findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCancelListener != null) {
                    mCancelListener.onClick(InputDialog.this, v.getId());
                }
                dismiss();
            }
        });
        findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCancelListener != null) {
                    mCancelListener.onClick(InputDialog.this, v.getId());
                }
                dismiss();
            }
        });
        findViewById(R.id.dialog_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOkListener != null) {
                    mOkListener.onClick(InputDialog.this, v.getId());
                }
            }
        });
    }

    @NonNull
    @Override
    public LayoutInflater getLayoutInflater() {
        Context context = getContext();
        if (context instanceof Activity) {
            return ((Activity) context).getLayoutInflater();
        }
        return super.getLayoutInflater();
    }

    public void setInputType(int type) {
        mContentView.setInputType(type);
    }

    public void setInputLength(int length) {
        InputFilter[] filters = {new InputFilter.LengthFilter(length)};
        mContentView.setFilters(filters);
    }

    @Override
    public void setTitle(@Nullable CharSequence title) {
        mTitleView.setText(title);
    }

    public void setMessage(@Nullable CharSequence content) {
        setMessage(content, Gravity.CENTER);
    }

    public void setMessage(@Nullable CharSequence content, int gravity) {
        mContentView.setGravity(gravity);
        mContentView.setText(content);
    }

    public void setOnClickOkListener(OnClickListener listener) {
        mOkListener = listener;
    }

    public void setOnClickCancelListener(OnClickListener listener) {
        mCancelListener = listener;
    }

    public abstract static class OnClickOkListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            onClickOk(dialog, ((InputDialog) dialog).mContentView.getText().toString().trim());
        }

        public abstract void onClickOk(DialogInterface dialog, String content);
    }
}
