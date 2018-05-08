package com.junhao.baby.activity;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.junhao.baby.R;
import com.junhao.baby.base.ViewHolder;
import com.junhao.baby.bean.Device;
import com.junhao.baby.bean.DeviceBean;
import com.junhao.baby.bean.Item;
import com.junhao.baby.service.ServiceManager;
import com.junhao.baby.utils.CommonUtils;
import com.junhao.baby.widget.IPopupWindow;
import com.junhao.baby.widget.ListPopupWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */
public class BgTimeActivity extends CommonActivity<DeviceBean> {
    private final List<DeviceBean> mList = new ArrayList<>();
    private TextView mTimeView;

    private ServiceManager.Callback mCallback = new ServiceManager.Callback() {
        @Override
        public void call(char key, byte[] data, String value) {
            mTimeView.setText(Device.getBLTime());
            notifyDataSetChanged();
        }
    };

    @Override
    protected void initData() {
        mList.add(new DeviceBean());
    }

    @Override
    protected void initView() {
        setTitle("背光时间");
        setLogoImage(R.mipmap.bg_time_logo);
        setTipText("请选择每次背光持续时间");
        mTimeView = getView(R.id.common_logo);
        mTimeView.setText(Device.getBLTime());
        showView(true, mTimeView);
        ServiceManager.getInstance().addCallback('O', mCallback);
        ServiceManager.getInstance().sendCommand(ServiceManager.CMD_SYNC_STATE);
    }

    @Override
    protected List<DeviceBean> getList() {
        return mList;
    }

    @Override
    protected void getItemView(ViewHolder holder, DeviceBean item) {
        holder.setImageResource(R.id.item_common_icon, R.mipmap.bg_time_icon);
        holder.setText(R.id.item_common_title, "背光持续时间");

        TextView name = holder.getView(R.id.item_common_name);
        Drawable drawable = CommonUtils.getDrawable(this, R.mipmap.arrow_down);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        name.setCompoundDrawables(null, null, drawable, null);
        name.setText(Device.getBLTime());
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListPopupWindow window = new ListPopupWindow<>(mContext, Device
                        .DEVICE_BL_TIME_LIST);
                window.setCurrentSelect(Device.getBLTimePosition());
                window.setOnSelectListener(new ListPopupWindow.OnSelectListener<Item>() {
                    @Override
                    public void onSelect(IPopupWindow window, Item select) {
                        ServiceManager.getInstance().sendCommand("# sl " + select.type + " $");
                        Device.setBackLightTime(select.type);
                        mCallback.call('0', null, null);
                    }
                });
                window.showAsDropDown(v, 0, v.getHeight() / 2);
            }
        });
        showView(true, holder.getView(R.id.item_common_icon), holder.getView(R.id
                .item_common_title), holder.getView
                (R.id.item_common_name));
    }
}
