package com.junhao.baby.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.junhao.baby.R;

/**
 * Created by ex-keayuan001 on 17/5/26.
 */

public class ToastUtil {
    private static Toast mToast;

    public static void show(Context context, String text, boolean isLong) {
        if (context != null) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(context, text, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

    public static void show(Context context, String text) {
        show(context, text, false);
    }

    public static void show(Context context, int resId) {
        if (context != null) {
            show(context, context.getString(resId));
        }
    }

    public static void showTip(Context context, String title, String content) {
        if (context != null) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = new Toast(context);
            View view = View.inflate(context, R.layout.dialog_tip, null);
            TextView titleView = (TextView) view.findViewById(R.id.dialog_title);
            titleView.setText(title);
            TextView contentView = (TextView) view.findViewById(R.id.dialog_content);
            if (TextUtils.isEmpty(content)) {
                contentView.setVisibility(View.GONE);
            } else {
                contentView.setText(content);
            }
            mToast.setGravity(Gravity.CENTER, 0, 0);
            mToast.setView(view);
            mToast.show();
        }
    }
}
