package com.junhao.baby.service;

import android.bluetooth.BluetoothDevice;

/**
 * Created by ayke on 2016/10/27 0027.
 */

public interface BluetoothScanListener {
	void onScanResult(BluetoothDevice device);
}
