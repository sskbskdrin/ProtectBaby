package com.junhao.baby.activity;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.junhao.baby.R;
import com.junhao.baby.base.ViewHolder;
import com.junhao.baby.bean.Device;
import com.junhao.baby.bean.DeviceBean;
import com.junhao.baby.bean.Item;
import com.junhao.baby.service.ServiceManager;
import com.junhao.baby.utils.CommonUtils;
import com.junhao.baby.utils.ToastUtil;
import com.junhao.baby.widget.IPopupWindow;
import com.junhao.baby.widget.InputDialog;
import com.junhao.baby.widget.ListPopupWindow;
import com.junhao.baby.widget.SwitchButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */

public class AlertActivity extends CommonActivity<DeviceBean> {
    private final List<DeviceBean> mList = new ArrayList<>();

    private ServiceManager.Callback mCallback = new ServiceManager.Callback() {

        @Override
        public void call(char key, byte[] data, String value) {
            notifyDataSetChanged();
        }
    };

    @Override
    protected void initData() {
        mList.add(new DeviceBean());
        mList.add(new DeviceBean());
        mList.add(new DeviceBean());
        mList.add(new DeviceBean());
    }

    @Override
    protected void initView() {
        setTitle("报警通知");
        setLogoImage(R.mipmap.alert_logo);
        setTipText("请选择各设备报警通知的方式");
        ServiceManager.getInstance().addCallback("TGFP", mCallback);
        ServiceManager.getInstance().sendCommand(ServiceManager.CMD_SYNC_STATE);
    }

    @Override
    protected List<DeviceBean> getList() {
        return mList;
    }

    @Override
    protected void getItemView(final ViewHolder holder, final DeviceBean item) {
        SwitchButton mStatusSwitch = holder.getView(R.id.item_common_switch);
        final TextView name = holder.getView(R.id.item_common_name);
        int type;
        if (holder.position() == 0) {
            holder.setImageResource(R.id.item_common_icon, R.mipmap.alert_device_icon);
            holder.setText(R.id.item_common_title, "设备通知方式");
            type = Device.getNotifyType();
        } else if (holder.position() == 1) {
            holder.setImageResource(R.id.item_common_icon, R.mipmap.alert_phone_icon);
            holder.setText(R.id.item_common_title, "手机通知方式");
            type = Device.getPhoneNotifyType();
        } else {
            holder.setImageResource(R.id.item_common_icon, R.mipmap.alert_device_icon);
            TextView option = holder.getView(R.id.item_common_option);
            option.setText("我要修改");
            if (holder.position() == 2) {
                holder.setText(R.id.item_common_title, "报警阀值实时");
                name.setText(Device.getAlertThreshold() + "μSv");
            } else {
                holder.setText(R.id.item_common_title, "报警阀值总值");
                name.setText(Device.getAlertTotalThreshold() + "μSv");
            }
            option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPassword(holder.position() == 3, true);
                }
            });
            showView(true, holder.getView(R.id.item_common_icon), holder.getView(R.id
                    .item_common_title), name, option);
            return;
        }
        boolean status = type != Device.TYPE_NONE;
        mStatusSwitch.setCheckedNoEvent(status);
        name.setText(Device.getItemByType(type).toCharSequence());
        Drawable drawable = CommonUtils.getDrawable(this, R.mipmap.arrow_down);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        name.setCompoundDrawables(null, null, status ? drawable : null, null);
        name.setOnClickListener(status ? new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showListWindow(holder.position(), name);
            }
        } : null);
        mStatusSwitch.setTintColor(0xffffffff);
        mStatusSwitch.setBackColorRes(R.drawable.switch_status_drawable);
        int range = CommonUtils.dp2px(mContext, 2);
        mStatusSwitch.setThumbMargin(range, range, range, range);
        mStatusSwitch.setThumbRadius(CommonUtils.dp2px(mContext, 12));
        mStatusSwitch.setBackRadius(CommonUtils.dp2px(mContext, 16));
        mStatusSwitch.setThumbColorRes(R.color.white);
        mStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (holder.position() == 0) {
                    boolean voice = Device.isVoice();
                    boolean vibrator = Device.isVibrator();
                    if (isChecked) {
                        ServiceManager.getInstance().sendCommand(ServiceManager.CMD_SET_VOICE);
                        ServiceManager.getInstance().sendCommand(ServiceManager.CMD_SET_VIBRATOR);
                    } else {
                        if (voice) {
                            ServiceManager.getInstance().sendCommand(ServiceManager.CMD_SET_VOICE);
                        }
                        if (vibrator) {
                            ServiceManager.getInstance().sendCommand(ServiceManager
                                    .CMD_SET_VIBRATOR);
                        }
                    }
                    Device.setVoice(isChecked);
                    Device.setVibrator(isChecked);
                } else {
                    Device.setPhoneVoice(isChecked);
                    Device.setPhoneVibrator(isChecked);
                }
                notifyDataSetChanged();
            }
        });
        showView(true, holder.getView(R.id.item_common_icon), holder.getView(R.id
                        .item_common_title), name,
                mStatusSwitch);
    }

    private void showListWindow(final int position, View view) {
        ListPopupWindow<Item> window = new ListPopupWindow<>(mContext, Device.DEVICE_STATE_LIST);
        window.setCurrentSelect(position == 0 ? Device.getPosition() : Device.getPhonePosition());
        window.setOnSelectListener(new ListPopupWindow.OnSelectListener<Item>() {
            @Override
            public void onSelect(IPopupWindow window, Item select) {
                if (position == 0) {
                    boolean voice = Device.isVoice();
                    boolean vibrator = Device.isVibrator();
                    if ((voice && select.type == Device.TYPE_VIBRATOR) || (!voice && select.type
                            != Device
                            .TYPE_VIBRATOR)) {
                        ServiceManager.getInstance().sendCommand(ServiceManager.CMD_SET_VOICE);
                    }
                    if ((vibrator && select.type == Device.TYPE_VOICE) || (!vibrator && select
                            .type != Device
                            .TYPE_VOICE)) {
                        ServiceManager.getInstance().sendCommand(ServiceManager.CMD_SET_VIBRATOR);
                    }
                    Device.setVoice(select.type != Device.TYPE_VIBRATOR);
                    Device.setVibrator(select.type != Device.TYPE_VOICE);
                } else {
                    Device.setPhoneVoice(select.type != Device.TYPE_VIBRATOR);
                    Device.setPhoneVibrator(select.type != Device.TYPE_VOICE);
                }
                notifyDataSetChanged();
            }
        });
        int[] location = new int[2];
        view.getLocationInWindow(location);
        window.showAsDropDown(view, location[0], 0);
    }

    private void showPassword(final boolean isTotal, final boolean isPassword) {
        final InputDialog inputDialog = new InputDialog(this);
        if (isPassword) {
            inputDialog.setTitle("请输入4位数密码");
            inputDialog.setInputLength(4);
        } else {
            inputDialog.setInputLength(5);
            inputDialog.setTitle(isTotal ? "修改报警阀值总值" : "修改实时报警阀值");
            String value = String.valueOf(isTotal ? Device.getAlertTotalThreshold() : Device
                    .getAlertThreshold());
            if (value.length() >= 5 && value.charAt(4) == '.') {
                value = value.substring(0, 4);
            }
            inputDialog.setMessage(value);
        }
        inputDialog.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputDialog.setOnClickOkListener(new InputDialog.OnClickOkListener() {
            @Override
            public void onClickOk(DialogInterface dialog, String content) {
                if (isPassword) {
                    if ("8888".equals(content)) {
                        dialog.dismiss();
                        showPassword(isTotal, false);
                    } else {
                        ToastUtil.show(mContext, "密码错误");
                    }
                } else {
                    if (TextUtils.isEmpty(content)) {
                        ToastUtil.show(mContext, "数值不能为空");
                        return;
                    }
                    if (content.length() < 5 && content.indexOf('.') < 0) {
                        content += ".";
                    }
                    while (content.length() < 5) {
                        content += "0";
                    }
                    if (isTotal) {
                        Device.setAlertTotalThreshold(content);
                        ServiceManager.getInstance().sendCommand("# st " + content + " 0 $");
                    } else {
                        Device.setAlertThreshold(content);
                        ServiceManager.getInstance().sendCommand("# sa " + content + " 0 $");
                    }
                    dialog.dismiss();
                }
            }
        });
        inputDialog.show();
    }

}
