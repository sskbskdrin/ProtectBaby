package com.junhao.baby.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;

import com.junhao.baby.R;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */

public class TipDialog extends Dialog {
    public TipDialog(@NonNull Context context) {
        super(context, R.style.ios_dialog_style);
        setContentView(R.layout.dialog_tip);
    }

}
