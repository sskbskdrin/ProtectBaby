package com.junhao.baby.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.junhao.baby.R;
import com.junhao.baby.base.ViewHolder;
import com.junhao.baby.bean.DeviceBean;
import com.junhao.baby.bean.Item;
import com.junhao.baby.bean.User;
import com.junhao.baby.service.ServiceManager;
import com.junhao.baby.utils.CommonUtils;
import com.junhao.baby.widget.IPopupWindow;
import com.junhao.baby.widget.ListPopupWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */

public class HistoryActivity extends CommonActivity<DeviceBean> {
    private final List<DeviceBean> mList = new ArrayList<>();
    private final List<Item> TYPE = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        setMenuText("清空设备数据");
        setTitle("历史数据");
        setLogoImage(R.mipmap.about_logo);
    }

    @Override
    protected void initData() {
        mList.add(new DeviceBean());
        TYPE.add(new Item("月"));
        TYPE.add(new Item("周"));
        TYPE.add(new Item("日"));
    }

    @Override
    protected List<DeviceBean> getList() {
        return mList;
    }

    @Override
    protected void getItemView(ViewHolder holder, DeviceBean item) {
        holder.setImageResource(R.id.item_common_icon, R.mipmap.device_hard_icon);
        holder.setText(R.id.item_common_title, "历史数据显示方式");

        TextView name = holder.getView(R.id.item_common_name);
        Drawable drawable = CommonUtils.getDrawable(this, R.mipmap.arrow_down);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        name.setCompoundDrawables(null, null, drawable, null);
        name.setText(User.getInstance().getHistoryType());
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListPopupWindow<Item> window = new ListPopupWindow<>(mContext, TYPE);
                window.setCurrentSelect(getSelect(User.getInstance().getHistoryType()));
                window.setOnSelectListener(new ListPopupWindow.OnSelectListener<Item>() {
                    @Override
                    public void onSelect(IPopupWindow window, Item select) {
                        User.getInstance().setHistoryType(select.name);
                        notifyDataSetChanged();
                    }
                });
                window.showAsDropDown(v, 0, v.getHeight() / 2);
            }
        });
        showView(true, holder.getView(R.id.item_common_icon), holder.getView(R.id
                .item_common_title), holder.getView
                (R.id.item_common_name));
    }

    public int getSelect(String name) {
        for (int i = 0; i < TYPE.size(); i++) {
            if (TYPE.get(i).name.equals(name)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    protected void onClickMenu() {
        ServiceManager.getInstance().sendCommand(ServiceManager.CMD_CLEAR_DATA);
    }
}
