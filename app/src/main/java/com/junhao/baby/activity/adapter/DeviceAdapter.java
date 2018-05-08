package com.junhao.baby.activity.adapter;

import android.content.Context;

import com.junhao.baby.R;
import com.junhao.baby.base.IBaseAdapter;
import com.junhao.baby.base.ViewHolder;
import com.junhao.baby.bean.DeviceBean;

import java.util.List;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */

public class DeviceAdapter extends IBaseAdapter<DeviceBean> {

    public DeviceAdapter(Context context, List<DeviceBean> list) {
        super(context, list, R.layout.item_common);
    }

    @Override
    public void bindViewHolder(ViewHolder holder, DeviceBean item) {

    }
}
