package com.junhao.baby.service;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

import com.junhao.baby.utils.TimerManage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ayke on 2016/10/27 0027.
 */

public class DeviceScanManage {

    private static final long SCAN_PERIOD = 60 * 1000;
    private static final String SCAN_TIMER_TAG = "scan_device";

    private static boolean mScanning = false;
    private static BluetoothAdapter.LeScanCallback mCallback;

    public static void startScan(final Activity context, final BluetoothScanListener listener) {
        startScan(context, SCAN_PERIOD, listener);
    }

    public static void startScan(final Context context, long time, final BluetoothScanListener listener) {
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context
            .BLUETOOTH_SERVICE);
        final List<BluetoothDevice> mList = new ArrayList<>();
        if (mCallback == null) {
            mCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (!mList.contains(device)) {
                        mList.add(device);
                        if (listener != null) {
                            listener.onScanResult(device);
                        }
                    }
                }
            };
        }
        scanLeDevice(bluetoothManager.getAdapter(), time, mCallback);
    }

    public static void stopScan(Context context) {
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context
            .BLUETOOTH_SERVICE);
        stopScanDevice(bluetoothManager.getAdapter(), mCallback);
        mCallback = null;
    }

    private static void scanLeDevice(final BluetoothAdapter adapter, long time, final BluetoothAdapter.LeScanCallback
        callback) {
        if (!mScanning) {
            TimerManage.getInstance().startTimerTask(SCAN_TIMER_TAG, time, new TimerManage.TimerTaskListener() {
                @Override
                public void onTimer(String tag, int count) {
                    stopScanDevice(adapter, callback);
                    mCallback = null;
                }
            });
            mScanning = true;
            if (adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }
            adapter.startDiscovery();
            adapter.startLeScan(callback);
        }
    }

    private static void stopScanDevice(final BluetoothAdapter adapter, BluetoothAdapter.LeScanCallback callback) {
        if (mScanning) {
            TimerManage.getInstance().stopTimerTask("scan_device");
            mScanning = false;
            adapter.cancelDiscovery();
            adapter.stopLeScan(callback);
        }
    }

}
