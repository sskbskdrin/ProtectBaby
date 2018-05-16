package com.junhao.baby.activity;

import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.junhao.baby.R;
import com.junhao.baby.base.ViewHolder;
import com.junhao.baby.bean.DeviceBean;
import com.junhao.baby.bean.User;
import com.junhao.baby.service.ServiceManager;
import com.junhao.baby.utils.ToastUtil;
import com.junhao.baby.widget.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import cn.feng.skin.manager.loader.SkinManager;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */

public class DeviceActivity extends CommonActivity<DeviceBean> {
    private static final String TAG = "DeviceActivity";
    private final List<DeviceBean> mList = new ArrayList<>();

    @Override
    protected void initData() {
        String name = User.getInstance().getDeviceName();
        String address = User.getInstance().getDeviceAddress();

        if (!TextUtils.isEmpty(address)) {
            mList.add(new DeviceBean(name, address));
        }
    }

    @Override
    protected void initView() {
        setTitle("我的设备");
        setLogoImage(R.mipmap.device_logo);
        if (mList.size() == 0) {
            setTipText("您还未绑定任何设备，请先添加");
        } else {
            setTipText("您已绑定了" + mList.size() + "个设备");
        }
    }

    @Override
    protected List<DeviceBean> getList() {
        return mList;
    }

    @Override
    protected void getItemView(ViewHolder holder, final DeviceBean item) {
        holder.setImageResource(R.id.item_common_icon, R.mipmap.device_hard_icon);
        holder.setText(R.id.item_common_title, "设备" + (holder.position() + 1));
        holder.setText(R.id.item_common_name, item.name);
        holder.setText(R.id.item_common_option, "解绑设备");
        showView(true, holder.getView(R.id.item_common_icon), holder.getView(R.id.item_common_title), holder.getView
            (R.id.item_common_name), holder.getView(R.id.item_common_line), holder.getView(R.id.item_common_option));
        holder.getView(R.id.item_common_option).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog(mContext);
                dialog.setTitle("解绑设备");
                SpannableString ss = new SpannableString("您确定要解绑以下设备吗？\n" + item.name);
                ss.setSpan(new ForegroundColorSpan(SkinManager.getInstance().getColor(R.color.theme_color)), ss
                    .length() - item.name.length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                dialog.setMessage(ss);
                dialog.setOnClickOkListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        User.getInstance().bindAddress("", "");
                        mList.remove(item);
                        setTipText("您已绑定了" + mList.size() + "个设备");
                        notifyDataSetChanged();
                        dialog.dismiss();
                        ToastUtil.showTip(mContext, "解绑成功", "");
                        ServiceManager.getInstance().unBindDevice();
                    }
                });
                dialog.show();
            }
        });
    }
}
