package com.junhao.baby.db;

import android.content.Context;

import com.junhao.baby.BabyApp;
import com.junhao.baby.bean.DeviceBean;

/**
 * Created by sskbskdrin on 2018/三月/11.
 */

public class DeviceDao extends BaseDao<DeviceBean> {

    private static DeviceDao mInstance;

    private DeviceDao(Context context, Class<DeviceBean> clazz) {
        super(context, clazz);
    }

    public static void init() {

    }

    public static DeviceDao getInstance() {
        if (mInstance == null) {
            synchronized (DeviceDao.class) {
                if (mInstance == null) {
                    mInstance = new DeviceDao(BabyApp.getContext(), DeviceBean.class);
                }
            }
        }
        return mInstance;
    }
}
