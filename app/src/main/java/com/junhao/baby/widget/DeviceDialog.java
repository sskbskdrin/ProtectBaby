package com.junhao.baby.widget;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.junhao.baby.R;
import com.junhao.baby.base.IBaseAdapter;
import com.junhao.baby.base.ViewHolder;
import com.junhao.baby.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import cn.feng.skin.manager.loader.SkinManager;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */

public class DeviceDialog extends Dialog {

    private DeviceAdapter mAdapter;

    private TextView mTitleView;
    private TextView mContentView;
    private ListView mListView;
    private onClickOption mListener;

    public DeviceDialog(@NonNull Context context) {
        super(context, R.style.ios_dialog_style);
        setContentView(R.layout.dialog_device_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        mTitleView = (TextView) findViewById(R.id.dialog_title);
        mContentView = (TextView) findViewById(R.id.dialog_content);
        findViewById(R.id.dialog_cancel).setVisibility(View.GONE);
        findViewById(R.id.dialog_space).setVisibility(View.GONE);
        mListView = ((ListView) findViewById(R.id.dialog_device_list));
        mAdapter = new DeviceAdapter(getContext(), new ArrayList<BluetoothDevice>());
        mListView.setAdapter(mAdapter);
        findViewById(R.id.dialog_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        findViewById(R.id.dialog_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
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

    public void setList(List<BluetoothDevice> list) {
        SpannableString ss = new SpannableString("搜寻到" + list.size() + "款设备，请绑定");
        ss.setSpan(new ForegroundColorSpan(SkinManager.getInstance().getColor(R.color
                .theme_color)), 3, 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mContentView.setText(ss);

        if (list.size() > 1) {
            mListView.getLayoutParams().height = CommonUtils.dp2px(getContext(), 160);
        } else {
            mListView.getLayoutParams().height = -2;
        }
        mListView.requestLayout();
        mAdapter.updateList(list);
    }

    public void setOnClickOption(onClickOption listener) {
        mListener = listener;
    }

    private class DeviceAdapter extends IBaseAdapter<BluetoothDevice> {

        public DeviceAdapter(Context context, List<BluetoothDevice> list) {
            super(context, list, R.layout.item_common);
        }

        @Override
        public void bindViewHolder(ViewHolder holder, final BluetoothDevice item) {
            holder.setImageResource(R.id.item_common_icon, R.mipmap.device_hard_small_icon);
            holder.setText(R.id.item_common_title, "设备" + (holder.position() + 1));
            holder.setText(R.id.item_common_name, item.getName());
            TextView optionView = holder.getView(R.id.item_common_option);
            optionView.setTextColor(SkinManager.getInstance().getColor(R.color.theme_color));
            optionView.setText("绑定此设备");
            showView(true, holder.getView(R.id.item_common_icon), holder.getView(R.id
                            .item_common_title), holder
                            .getView(R.id.item_common_name), holder.getView(R.id.item_common_line),
                    optionView);
            holder.getView(R.id.item_common_option).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onOption(item);
                    }
                }
            });
        }
    }

    public interface onClickOption {
        void onOption(BluetoothDevice device);
    }
}
