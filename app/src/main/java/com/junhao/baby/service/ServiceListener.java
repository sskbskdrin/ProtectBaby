package com.junhao.baby.service;

/**
 * Created by Administrator on 2016/7/29 0029.
 * 绑定接口
 */
public interface ServiceListener extends DeviceStateListener {

    void onAvailableData(byte[] data);

    void onAvailableData(String data);
}
