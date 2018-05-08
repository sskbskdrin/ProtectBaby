/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.junhao.baby.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.junhao.baby.utils.L;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class BluetoothService extends Service {
    private final static String TAG = BluetoothService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;

    private BluetoothConnectThread mConnectThread;

    private BluetoothGatt mBluetoothGatt;

    private boolean isSupportLe;

    public final static int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;
    public final static int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
    public final static int STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;

    public final static String ACTION_GATT_CONNECTED = "com.junhao.baby.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING = "com.junhao.baby.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED = "com.junhao.baby" + "" +
            ".ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.junhao.baby" + "" +
            ".ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.junhao.baby.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.junhao.baby.EXTRA_DATA";

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private BluetoothGattCallback mGattCallback;

    private ServiceListener mServiceListener;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                L.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            L.e(TAG, "Unable to obtain a BluetoothAdapter.ayke");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int,
     * int)}
     * callback.
     */
    public boolean connect(final String address, boolean supportLe) {
        if (mBluetoothAdapter == null || address == null) {
            L.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            L.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        if (mBluetoothAdapter.isDiscovering()) mBluetoothAdapter.cancelDiscovery();
        isSupportLe = supportLe;
        if (isSupportLe) {
            // Previously connected device. Try to reconnect.
            if (address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
                L.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
                return mBluetoothGatt.connect();
            }
            if (mGattCallback == null) {
                mGattCallback = new GattCallback();
            }
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            L.d(TAG, "Trying to create a new connection.");
        } else {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
            mConnectThread = new BluetoothConnectThread(device);
            mConnectThread.start();
        }
        mBluetoothDeviceAddress = address;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int,
     * int)}
     * callback.
     */
    public void disconnect() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt = null;
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     */
    public void write(byte[] out) {
        if (isSupportLe) {
            L.e(TAG, "not support write");
            return;
        }
        // Create temporary object
        BluetoothConnectThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            r = mConnectThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android
     * .bluetooth
     * .BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    // 读取数据的函数
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            L.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        L.d(TAG, "readCharacteristic properties=" + Utils.getCharPropertie(characteristic
                .getProperties()));
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    // 写入指定的characteristic
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            L.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        L.d(TAG, "writeCharacteristic properties=" + Utils.getCharPropertie(characteristic
                .getProperties()));
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void writeDescriptor(BluetoothGattDescriptor descriptor) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            L.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean
            enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            L.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        L.d(TAG, "setCharacteristicNotification properties=" + Utils.getCharPropertie
                (characteristic.getProperties())
                + " " + enabled);
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        // This is specific to Heart Rate Measurement.
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully. 获取已连接设备支持的所有GATT服务集合
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }

    public BluetoothGattService getSupportedGattServices(UUID uuid) {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getService(uuid);
    }

    public boolean discoverServices() {
        return mBluetoothGatt.discoverServices();
    }

    public void setServiceListener(ServiceListener listener) {
        mServiceListener = listener;
    }

    private class GattCallback extends BluetoothGattCallback {
        private StringBuffer mBuffer = new StringBuffer(20);
        private byte[] mByteBuffer = new byte[20];
        private int mByteLength = 0;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            L.d(TAG, "onConnectionStateChange newState=" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                L.i(TAG, "Connected to GATT server.");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                L.d(TAG, "connecting to GATT server.");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                L.e(TAG, "Disconnected from GATT server.");
            }
            if (mServiceListener != null) {
                mServiceListener.onConnectStatusChange(newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            L.d(TAG, "onServicesDiscovered status=" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Utils.print(gatt);
                if (mServiceListener != null) {
                    mServiceListener.onDiscovered();
                }
            } else {
                L.d(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            L.d(TAG, "onCharacteristicRead status=" + status);
        }

        @Override
        public synchronized void onCharacteristicChanged(BluetoothGatt gatt,
                                                         BluetoothGattCharacteristic
                                                                 characteristic) {
            byte[] data = characteristic.getValue();
            if (L.IS_DEBUG) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                L.i(TAG, "onCharacteristicChanged:" + stringBuilder.toString() + "[" + new String
                        (data) + "]");
            }
            if (data.length <= 0) {
                return;
            }
            if (data[0] != (byte) 0x24) {
                if (data.length == 20) {
                    System.arraycopy(data, 0, mByteBuffer, 0, data.length);
                    mByteLength = 20;
                } else {
                    if (mByteLength + data.length <= 20) {
                        System.arraycopy(data, 0, mByteBuffer, mByteLength, data.length);
                        mByteLength += data.length;
                    }
                }
                if (mByteBuffer[0] == 0x55 || mByteBuffer[0] == 0x56) {
                    if (verify(mByteBuffer)) {
                        if (mServiceListener != null) {
                            mServiceListener.onAvailableData(mByteBuffer);
                        }
                        mByteBuffer = new byte[20];
                        mByteLength = 0;
                    }
                    return;
                }
            }
            mBuffer.append(new String(data));
            for (int i = 0; i < mBuffer.length(); ) {
                if (i == 0) {
                    if (mBuffer.charAt(0) != '$') {
                        mBuffer.deleteCharAt(0);
                        continue;
                    }
                } else {
                    if (mBuffer.charAt(i) == '$') {
                        mBuffer.delete(0, i);
                        i = 0;
                        continue;
                    }
                }
                if (mBuffer.charAt(i) == '#') {
                    if (mServiceListener != null) {
                        mServiceListener.onAvailableData(mBuffer.substring(0, i + 1));
                    }
                    mBuffer.delete(0, i + 1);
                    i = 0;
                } else {
                    i++;
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            byte[] value = characteristic.getValue();
            StringBuilder builder = new StringBuilder(value.length);
            List<Byte> list = new ArrayList<>();
            for (byte byteChar : value) {
                builder.append(String.format("%02X ", byteChar));
                list.add(byteChar);
            }
            L.d(TAG, "onCharacteristicWrite:" + builder.toString() + "[" + new String(value) + "]");
        }

        private boolean verify(byte[] data) {
            if (data.length < 20) {
                return false;
            }

//            int count = 0;
//            int sum = 0;
//            for (byte d : data) {
//                if (d == (byte) 0xff) {
//                    sum++;
//                } else {
//                    sum = 0;
//                }
//                if (sum > count) {
//                    count = sum;
//                }
//            }
//            if (count >= 4) {
//                return false;
//            }

            byte temp = (byte) (data[0] ^ data[1]);
            for (int i = 2; i < 18; i++) {
                temp = (byte) (temp ^ data[i]);
            }
            return data[18] == temp && data[19] == (byte) 0xaa;
        }
    }
}
