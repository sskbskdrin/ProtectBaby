package com.junhao.baby.service;

/**
 * Created by ayke on 2016/10/12 0012.
 */

public interface DeviceStateListener {

    void onConnectStatusChange(int state);

    void onDiscovered();
}
