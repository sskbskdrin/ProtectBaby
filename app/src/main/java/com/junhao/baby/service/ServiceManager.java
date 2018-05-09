package com.junhao.baby.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Size;
import android.text.TextUtils;

import com.junhao.baby.BabyApp;
import com.junhao.baby.bean.Device;
import com.junhao.baby.bean.DosageBean;
import com.junhao.baby.bean.User;
import com.junhao.baby.db.DosageDao;
import com.junhao.baby.utils.L;
import com.junhao.baby.utils.ShockUtil;
import com.junhao.baby.utils.SpfUtil;
import com.junhao.baby.utils.TimerManage;
import com.junhao.baby.utils.VoiceUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */
public class ServiceManager implements BluetoothScanListener, ServiceListener {
    private static final String TAG = "ServiceManager";

    public static final UUID REVERSION_SERVICE_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID REVERSION_CHAR_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");

    private static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID DES_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final String CMD_REQUIRE_BIND = "# bl $";
    private static final String CMD_REQUIRE_CONNECT = "# cet $";
    private static final String CMD_NORMAL_DATA = "# gd $";
    private static final String CMD_TEMPERATURE_BATTERY = "# ud1 $";
    public static final String CMD_SYNC_STATE = "# ud2 $";
    public static final String CMD_SYNC_FIRMWARE = "# ud3 $";
    public static final String CMD_CLEAR_DATA = "# ct $";
    public static final String CMD_SET_VIBRATOR = "# cv $";
    public static final String CMD_SET_VOICE = "# cs $";

    public static final char UPDATE_MODE = '更';
    public static final char SYNC_MODE = '同';

    private static final int WHAT_STATE_CHANGE = 1001;
    private static final int WHAT_STATE_DISCOVER = 1002;
    private static final int WHAT_DEVICE_DATA = 2001;
    private static final int WHAT_SCAN_START = 3001;
    private static final int WHAT_SCAN_RESULT = 3002;
    private static final int WHAT_RETRY_CONNECT = 4001;
    private static final int WHAT_SYNC_NORMAL_DATA = 8001;
    private static final int WHAT_SEND_COMMAND = 9001;
    private static final int WHAT_SEND_BYTE = 9002;

    private static final int REAL_TIME_INTERVAL = 1500;

    private static final int SCAN_PERIOD = 8 * 1000;

    private final List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private volatile List<String> mCommandList = new ArrayList<>(16);
    private volatile boolean isReply = false;

    private String mDeviceAddress;
    private String mDeviceName;

    private BluetoothService mBluetoothService;

    private BluetoothGattService mRxService;
    private BluetoothGattCharacteristic mRxChar;

    private static ServiceManager mInstance;

    private HashSet<BluetoothScanListener> mScanListeners;
    private HashSet<DeviceStateListener> mDeviceStateListeners;
    private HashMap<Character, List<WeakReference<Callback>>> mCallbacks;

    private boolean dosageAlertEnable = true;
    private boolean dosageTotalAlertEnable = true;

    private String mCurrentCmd;
    private int mRetryCount = 0;

    public void addCallback(String keys, Callback callback) {
        if (!TextUtils.isEmpty(keys)) {
            for (int i = 0; i < keys.length(); i++) {
                addCallback(keys.charAt(i), callback);
            }
        }
    }

    public void addCallback(Character key, Callback callback) {
        WeakReference<Callback> weakReference = new WeakReference<>(callback);
        if (!mCallbacks.containsKey(key)) {
            mCallbacks.put(key, new ArrayList<WeakReference<Callback>>(2));
        }
        for (WeakReference<Callback> call : mCallbacks.get(key)) {
            if (callback == call.get()) {
                return;
            }
        }
        mCallbacks.get(key).add(weakReference);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_RETRY_CONNECT:
                    if (mBluetoothService != null) {
                        mBluetoothService.close();
                    }
                    connect(mDeviceName, mDeviceAddress);
                    break;
                case WHAT_SEND_COMMAND:
                    if (mRxChar != null && mBluetoothService != null) {
                        if (isReply) {
                            mCurrentCmd = null;
                        } else {
                            if (mCurrentCmd != null) {
                                mRetryCount++;
                            }
                        }
                        if (mCurrentCmd == null && mCommandList.size() > 0) {
                            mCurrentCmd = mCommandList.remove(0);
                            mRetryCount = 0;
                        }
                        if (mRetryCount >= 10) {
                            return true;
                        }
                        if (!TextUtils.isEmpty(mCurrentCmd)) {
                            if (!SyncHistoryData.isSyncMode && !FirmwareUpdate.isUpdateMode) {
                                mRxChar.setValue(mCurrentCmd.getBytes());
                                mBluetoothService.writeCharacteristic(mRxChar);
                                isReply = CMD_CLEAR_DATA.equals(mCurrentCmd);
                            }
                        }
                    }
                    mHandler.sendEmptyMessageDelayed(WHAT_SEND_COMMAND, REAL_TIME_INTERVAL);
                    break;
                case WHAT_SEND_BYTE:
                    if (mRxChar != null && mBluetoothService != null) {
                        mRxChar.setValue((byte[]) msg.obj);
                        mBluetoothService.writeCharacteristic(mRxChar);
                    }
                    break;
                case WHAT_STATE_CHANGE:
                    for (DeviceStateListener listener : mDeviceStateListeners) {
                        listener.onConnectStatusChange(msg.arg1);
                    }
                    break;
                case WHAT_STATE_DISCOVER:
                    for (DeviceStateListener listener : mDeviceStateListeners) {
                        listener.onDiscovered();
                    }
                    break;
                case WHAT_SCAN_START:
                    DeviceScanManage.stopScan(BabyApp.getContext());
                    DeviceScanManage.startScan(BabyApp.getContext(), SCAN_PERIOD, ServiceManager.getInstance());
                    mHandler.sendEmptyMessageDelayed(WHAT_SCAN_START, SCAN_PERIOD);
                    break;
                case WHAT_SCAN_RESULT:
                    for (BluetoothScanListener listener : mScanListeners) {
                        listener.onScanResult((BluetoothDevice) msg.obj);
                    }
                    break;
                case WHAT_DEVICE_DATA:
                    if (msg.obj instanceof String) {
                        dispatchData((String) msg.obj);
                    } else {
                        dispatchData((byte[]) msg.obj);
                    }
                    break;
                case WHAT_SYNC_NORMAL_DATA:
                    isNormalData = ++isNormalData % 5;
                    sendCommand(isNormalData == 0 ? CMD_TEMPERATURE_BATTERY : CMD_NORMAL_DATA);
                    mHandler.sendEmptyMessageDelayed(WHAT_SYNC_NORMAL_DATA, REAL_TIME_INTERVAL);
                    break;
                default:
            }
            return true;
        }
    });

    private int isNormalData;

    private ServiceManager() {
        bindService();
        mCallbacks = new HashMap<>(26);
        mScanListeners = new HashSet<>(3);
        mDeviceStateListeners = new HashSet<>(4);
    }

    public static ServiceManager getInstance() {
        if (mInstance == null) {
            synchronized (ServiceManager.class) {
                if (mInstance == null) {
                    mInstance = new ServiceManager();
                }
            }
        }
        return mInstance;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothService = ((BluetoothService.LocalBinder) service).getService();
            mBluetoothService.setServiceListener(getInstance());
            if (!mBluetoothService.initialize()) {
                L.e(TAG, "Unable to initialize Bluetooth");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothService = null;
            mRxService = null;
            mRxChar = null;
        }
    };

    @Override
    public void onConnectStatusChange(int state) {
        if (state == BluetoothService.STATE_CONNECTED) {
            mHandler.removeMessages(WHAT_RETRY_CONNECT);
        } else if (state == BluetoothService.STATE_DISCONNECTED) {
            SyncHistoryData.isSyncMode = false;
            FirmwareUpdate.isUpdateMode = false;
            mHandler.removeMessages(WHAT_SYNC_NORMAL_DATA);
            TimerManage.getInstance().stopTimerAll();
            mRxService = null;
            mRxChar = null;
            ShockUtil.stopVibrate(BabyApp.getContext());
            VoiceUtil.stop();
        }
        Message.obtain(mHandler, WHAT_STATE_CHANGE, state, 0).sendToTarget();
    }

    @Override
    public void onDiscovered() {
        mHandler.removeMessages(WHAT_RETRY_CONNECT);
        mRxService = mBluetoothService.getSupportedGattServices(RX_SERVICE_UUID);
        mRxChar = mRxService.getCharacteristic(RX_CHAR_UUID);
        enableTXNotification();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pauseSyncRealTimeData();
                isReply = true;
                mCommandList.clear();
                if (TextUtils.isEmpty(User.getInstance().getDeviceAddress())) {
                    sendCommand(CMD_REQUIRE_BIND);
                } else {
                    sendCommand(CMD_REQUIRE_CONNECT);
                }
                mHandler.sendEmptyMessage(WHAT_SEND_COMMAND);
            }
        }, 1000);
        Message.obtain(mHandler, WHAT_STATE_DISCOVER).sendToTarget();
    }

    @Override
    public void onAvailableData(byte[] data) {
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            L.i(TAG, "onAvailableData:" + stringBuilder.toString() + "[" + new String(data) + "]");
            Message.obtain(mHandler, WHAT_DEVICE_DATA, data).sendToTarget();
        }
    }

    @Override
    public void onAvailableData(String data) {
        L.i(TAG, "onAvailableData:" + "[" + data + "]");
        if (!TextUtils.isEmpty(data)) {
            isReply = true;
            Message.obtain(mHandler, WHAT_DEVICE_DATA, data).sendToTarget();
        }
    }

    @Override
    public void onScanResult(BluetoothDevice device) {
        L.d(TAG, "device=" + device.getName() + "[" + device.toString() + "]");
        String name = device.getName();
        if (!mDeviceList.contains(device) && name != null && name.length() == 8 && name.charAt(0) == 'M') {
            mDeviceList.add(device);
            Message.obtain(mHandler, WHAT_SCAN_RESULT, device).sendToTarget();
        }
    }

    public boolean isConnected() {
        return mRxService != null && mRxChar != null;
    }

    public void addScanListener(BluetoothScanListener listener) {
        if (listener != null && !mScanListeners.contains(listener)) {
            mScanListeners.add(listener);
        }
    }

    public void removeScanListener(BluetoothScanListener listener) {
        if (listener != null && mScanListeners.contains(listener)) {
            mScanListeners.remove(listener);
        }
    }

    public void addDeviceStateListener(DeviceStateListener listener) {
        if (listener != null && !mDeviceStateListeners.contains(listener)) {
            mDeviceStateListeners.add(listener);
        }
    }

    public void removeDeviceStateListener(DeviceStateListener listener) {
        if (listener != null && mDeviceStateListeners.contains(listener)) {
            mDeviceStateListeners.remove(listener);
        }
    }

    public void startScan() {
        Message.obtain(mHandler, WHAT_SCAN_START).sendToTarget();
    }

    public void stopScan() {
        mHandler.removeMessages(WHAT_SCAN_START);
        DeviceScanManage.stopScan(BabyApp.getContext());
    }

    private void bindService() {
        DeviceScanManage.stopScan(BabyApp.getContext());
        Intent gattServiceIntent = new Intent(BabyApp.getContext(), BluetoothService.class);
        BabyApp.getContext().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void connect(String name, String address) {
        mDeviceName = name;
        mDeviceAddress = address;
        stopScan();
        if (mBluetoothService != null) {
            mHandler.sendEmptyMessageDelayed(WHAT_RETRY_CONNECT, SCAN_PERIOD);
            mBluetoothService.connect(mDeviceAddress, true);
        } else {
            bindService();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    connect(mDeviceName, mDeviceAddress);
                }
            }, 500);
        }
    }

    public void disconnect() {
        if (mBluetoothService != null) {
            mBluetoothService.disconnect();
        }
    }

    private void dispatchData(String data) {
        if (TextUtils.isEmpty(data) || data.length() < 4) {
            return;
        }
        char key = data.charAt(2);
        String value = data.substring(3, data.length() - 2).trim();
        float threshold = 0;
        switch (key) {
            case 'A':
                threshold = Float.parseFloat(value.substring(0, value.length() - 2));
                if (value.charAt(value.length() - 1) - 0x30 > 0) {
                    threshold *= 1000;
                }
                if (value.charAt(value.length() - 1) - 0x30 > 1) {
                    threshold *= 1000;
                }
                if (threshold >= Device.getAlertThreshold()) {
                    if (Device.isPhoneVibrator() && dosageAlertEnable) {
                        ShockUtil.startVibrate(BabyApp.getContext(), new long[]{800, 800, 800, 800}, true);
                    } else {
                        ShockUtil.stopVibrate(BabyApp.getContext());
                    }
                    if (Device.isPhoneVoice() && dosageAlertEnable) {
                        VoiceUtil.play(BabyApp.getContext());
                    } else {
                        VoiceUtil.stop();
                    }
                } else {
                    if (!dosageAlertEnable && Device.isVibrator()) {
                        sendCommand(CMD_SYNC_STATE);
                    }
                    dosageAlertEnable = true;
                    if (Device.getLastTotalDosage() < Device.getAlertTotalThreshold()) {
                        VoiceUtil.stop();
                        ShockUtil.stopVibrate(BabyApp.getContext());
                    }
                }
                Device.setLastDosage(threshold);
                L.d(TAG, "实时剂量率==>" + threshold);
                break;
            case 'B':
                float dosage = Float.parseFloat(value.substring(0, value.length() - 2).trim());
                int unit = value.charAt(value.length() - 1) - 0x30;
                if (unit > 0) {
                    dosage *= 1000;
                }
                if (unit > 1) {
                    dosage *= 1000;
                }
                long current = System.currentTimeMillis();
                if (SyncHistoryData.isSyncComplete && current - SpfUtil.getLong("dosage_time") > 60 * 1000) {
                    DosageBean bean = DosageBean.obtain();
                    bean.time = current / 1000;
                    bean.address = mDeviceAddress;
                    bean.name = mDeviceName;
                    bean.unit = 0;
                    bean.dosage = dosage;
                    bean.dosageEachH = Device.getLastDosage();
                    DosageDao.getInstance().addOrUpdate(bean);
                    bean.recycle();
                    SpfUtil.saveLong("dosage_time", current);
                }
                if (dosage >= Device.getAlertTotalThreshold()) {
                    if (Device.isPhoneVibrator() && dosageTotalAlertEnable) {
                        ShockUtil.startVibrate(BabyApp.getContext(), new long[]{800, 800, 800, 800}, true);
                    } else {
                        ShockUtil.stopVibrate(BabyApp.getContext());
                    }
                    if (Device.isPhoneVoice() && dosageTotalAlertEnable) {
                        VoiceUtil.play(BabyApp.getContext());
                    } else {
                        VoiceUtil.stop();
                    }
                } else {
                    if (!dosageTotalAlertEnable && Device.isVibrator()) {
                        sendCommand(CMD_SYNC_STATE);
                    }
                    dosageTotalAlertEnable = true;
                    if (Device.getLastDosage() < Device.getAlertThreshold()) {
                        VoiceUtil.stop();
                        ShockUtil.stopVibrate(BabyApp.getContext());
                    }
                }
//                Device.setLastTotalDosage(dosage);
                L.d(TAG, "实时总剂量==>" + value);
                break;
            case 'C':
                L.d(TAG, "实时CPS==>" + value);
                break;
            case 'D':
                L.d(TAG, "实时温度==>" + value);
                Device.setLastTemperature(Integer.parseInt(value));
                break;
            case 'E':
                L.d(TAG, "实时电量==>" + value);
                Device.setLastBattery(Integer.parseInt(value));
                break;
            case 'F':
                L.d(TAG, "报警阈值==>" + value);
                if (value.length() >= 2) {
                    if (value.contains(" ")) {
                        threshold = Float.parseFloat(value.substring(0, value.length() - 2));
                        if (value.charAt(value.length() - 1) - 0x30 > 0) {
                            threshold *= 1000;
                        }
                        if (value.charAt(value.length() - 1) - 0x30 > 1) {
                            threshold *= 1000;
                        }
                    } else {
                        threshold = Float.parseFloat(value);
                    }
                }
                if (Device.getAlertThreshold() != threshold) {
                    sendCommand("# sa " + Device.getAlertThreshold() + " 0 $");
                }
                break;
            case 'G':
                L.d(TAG, "声音报警开关状态==>" + value);
                boolean isOn = "on".equalsIgnoreCase(value);
                if (Device.isVoice() != isOn) {
                    sendCommand(CMD_SET_VOICE);
                }
                break;
            case 'H':
                L.d(TAG, "关机屏幕显示状态==>" + value);
                break;
            case 'I':
                L.d(TAG, "屏幕背光状态==>" + value);
                break;
            case 'J':
                L.d(TAG, "显示模式==>" + value);
                break;
            case 'L':
                L.d(TAG, "设备编号==>" + value);
                Device.setDeviceNumber(value);
                break;
            case 'M':
                L.d(TAG, "标定值==>" + value);
                break;
            case 'O':
                L.d(TAG, "背光时间==>" + value);
                int time = Integer.parseInt(value);
                if (time != Device.getBackLightTime()) {
                    sendCommand("# sl " + Device.getBackLightTime() + " $");
                }
                break;
            case 'P':
                L.d(TAG, "镇子报警开关状态==>" + value);
                boolean isVibrator = "on".equalsIgnoreCase(value);
                if (Device.getLastTotalDosage() >= Device.getAlertTotalThreshold() || Device.getLastDosage() >=
                    Device.getAlertThreshold()) {
                    if (!isVibrator) {
                        if (Device.getLastTotalDosage() >= Device.getAlertTotalThreshold()) {
                            dosageTotalAlertEnable = false;
                        }
                        if (Device.getLastDosage() >= Device.getAlertThreshold()) {
                            dosageAlertEnable = false;
                        }
                        VoiceUtil.stop();
                        ShockUtil.stopVibrate(BabyApp.getContext());
                        break;
                    }
                }
                if (Device.isVibrator() != isVibrator) {
                    sendCommand(CMD_SET_VIBRATOR);
                }
                break;
            case 'Q':
                break;
            case 'R':
                L.d(TAG, "设备应答==>" + value);
                break;
            case 'S':
                L.d(TAG, "时间同步==>" + value);
                break;
            case 'T':
                L.d(TAG, "总剂量报警阈值==>" + value);
                if (!"RE".equalsIgnoreCase(value)) {
                    if (value.length() >= 2) {
                        if (value.contains(" ")) {
                            threshold = Float.parseFloat(value.substring(0, value.length() - 2));
                            if (value.charAt(value.length() - 1) - 0x30 > 0) {
                                threshold *= 1000;
                            }
                            if (value.charAt(value.length() - 1) - 0x30 > 1) {
                                threshold *= 1000;
                            }
                        } else {
                            threshold = Float.parseFloat(value);
                        }
                    }
                    if (Device.getAlertTotalThreshold() != threshold) {
                        sendCommand("# st " + Device.getAlertTotalThreshold() + " 0 $");
                    }
                }
                break;
            case 'U':
                if ("RE".equals(value)) {
                    SpfUtil.saveString(Device.CURRENT_DEVICE, mDeviceAddress);
                    startSync();
                }
                break;
            case 'V':
                L.d(TAG, "设备连接请求应答==>" + value);
                if ("OK".equals(value)) {
                    SpfUtil.saveString(Device.CURRENT_DEVICE, mDeviceAddress);
                    User.getInstance().bindAddress(mDeviceName, mDeviceAddress);
                    startSync();
                }
                break;
            default:
                L.d(TAG, "设备返回其它值==>" + data);
                break;
        }
        notifyCallback(key, data.getBytes(), value);
    }

    private void startSync() {
        isReply = true;
        mCommandList.clear();
        SyncHistoryData.comeInMode();
    }

    private void dispatchData(byte[] data) {
        if (data == null || data.length < 4) {
            L.w(TAG, "dispatchData: data is " + (data == null ? "null" : new String(data)));
            return;
        }
        char key = SYNC_MODE;
        if (data[0] == 0x55 && data[1] < 0x10) {
            if (data[1] == 0x09 || (data[1] == 0x06 && data[2] != 0x01)) {
                isReply = true;
                mCommandList.clear();
                startSyncRealTimeData();
            }
            key = UPDATE_MODE;
        }
        notifyCallback(key, data, new String(data));
    }

    private void notifyCallback(char key, byte[] data, String value) {
        if (mCallbacks.containsKey(key)) {
            List<WeakReference<Callback>> list = mCallbacks.get(key);
            if (list != null) {
                for (int i = 0; i < list.size(); ) {
                    Callback callback = list.get(i).get();
                    if (callback != null) {
                        callback.call(key, data, value);
                        i++;
                    } else {
                        list.remove(i);
                    }
                }
            } else {
                mCallbacks.remove(key);
            }
        }
    }

    public void startSyncRealTimeData() {
        mHandler.removeMessages(WHAT_SYNC_NORMAL_DATA);
        mHandler.sendEmptyMessage(WHAT_SYNC_NORMAL_DATA);
    }

    public void pauseSyncRealTimeData() {
        mHandler.removeMessages(WHAT_SYNC_NORMAL_DATA);
        if (mCommandList.contains(CMD_NORMAL_DATA) || mCommandList.contains(CMD_TEMPERATURE_BATTERY)) {
            mCommandList.remove(CMD_NORMAL_DATA);
            mCommandList.remove(CMD_TEMPERATURE_BATTERY);
        }
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    private void enableTXNotification() {
        if (mRxService == null) {
            L.d(TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic TxChar = mRxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            L.d(TAG, "Tx characteristic not found!");
            return;
        }
        mBluetoothService.setCharacteristicNotification(TxChar, true);
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(DES_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothService.writeDescriptor(descriptor);
    }

    public void sendCommand(String cmd) {
        if (!TextUtils.isEmpty(cmd)) {
            if (!mCommandList.contains(cmd)) {
                mCommandList.add(cmd);
            }
        }
    }

    public void sendCommand(@Size(16) byte[] data, byte type) {
        Message message = Message.obtain();
        message.what = WHAT_SEND_BYTE;
        message.obj = generateMessageByte(data, type);
        mHandler.sendMessageDelayed(message, 100);
    }

    public void sendCommandNoDelay(@Size(16) byte[] data, byte type) {
        Message message = Message.obtain();
        message.what = WHAT_SEND_BYTE;
        message.obj = generateMessageByte(data, type);
        mHandler.sendMessage(message);
    }

    /**
     * 拼接报文
     *
     * @param data 传入的版本号或者文件大小
     * @param type 报文的类型
     */
    private byte[] generateMessageByte(byte[] data, byte type) {
        byte[] tempByte = new byte[20];
        tempByte[0] = 0x55;
        tempByte[1] = type;

        byte temp = (byte) (tempByte[0] ^ tempByte[1]);
        for (int i = 2; i < 18; i++) {
            tempByte[i] = data[i - 2];
            temp = (byte) (temp ^ tempByte[i]);
        }
        tempByte[18] = temp;
        tempByte[19] = (byte) 0xaa;
        return tempByte;
    }

    public interface Callback {
        void call(char key, byte[] data, String value);
    }
}
