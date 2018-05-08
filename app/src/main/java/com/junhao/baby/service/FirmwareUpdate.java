package com.junhao.baby.service;

import com.junhao.baby.utils.TimerManage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by ex-keayuan001 on 2018/3/14.
 *
 * @author ex-keayuan001
 */
public class FirmwareUpdate {

    private static FirmwareUpdate mInstance;
    public static boolean isUpdateMode;

    private byte[] mData;
    private int mLength = 0;
    private int mOffset = 0;
    private boolean isAgain = false;
    private int maxVersion;
    private File mFile;

    private ServiceManager.Callback mCallback = new ServiceManager.Callback() {
        @Override
        public void call(char key, byte[] data, String value) {
            if (mInstance == null) {
                return;
            }
            if ('Q' == key) {
                isUpdateMode = true;
                if (isAgain) {
                    send();
                } else {
                    check();
                }
            } else if (key == ServiceManager.UPDATE_MODE) {
                TimerManage.getInstance().stopTimerTask("update_mode");
                if (data[1] == 0x00) {
                    int version = 0xff & data[3];
                    version = version << 8 | (0xff & data[4]);
                    version = version << 8 | (0xff & data[5]);
                    version = version << 8 | (0xff & data[6]);
                    if (version < maxVersion && data[2] > 30) {
                        startUpdate();
                    }
                } else if (data[1] == 0x02) {
                    send();
                } else if (data[1] == 0x04) {
                    int temp = 0xff & data[2];
                    temp = temp << 8 | (0xff & data[3]);
                    temp = temp << 8 | (0xff & data[4]);
                    temp = temp << 8 | (0xff & data[5]);
                    if (temp != 0) {
                        mOffset = mLength - temp;
                        send();
                    } else {
                        sendEnd();
                    }
                } else if (data[1] == 0x06) {
                    if (data[2] != (byte) 0x01) {
                        stopUpdate();
                    }
                } else if (data[1] == 0x09) {
                    stopUpdate();
                }
            }
        }
    };

    private FirmwareUpdate() {
        ServiceManager.getInstance().addCallback(ServiceManager.UPDATE_MODE, mCallback);
        ServiceManager.getInstance().addCallback('Q', mCallback);
    }

    public static FirmwareUpdate getInstance() {
        if (mInstance == null) {
            synchronized (FirmwareUpdate.class) {
                if (mInstance == null) {
                    mInstance = new FirmwareUpdate();
                }
            }
        }
        return mInstance;
    }

    private void comeInUpdateMode() {
        ServiceManager.getInstance().sendCommand("# dn $");
    }

    //1118535
    private void check() {
        byte[] data = new byte[16];
        byte[] temp = convertBytes(maxVersion);
        data[0] = temp[0];
        data[1] = temp[1];
        data[2] = temp[2];
        data[3] = temp[3];
        ServiceManager.getInstance().sendCommand(data, (byte) 0x01);
    }

    public void startUpdate(File file, int version) {
        maxVersion = version;
        isAgain = false;
        mFile = file;
        ServiceManager.getInstance().sendCommand("# dn $");
    }

    public void stopUpdate() {
        TimerManage.getInstance().stopTimerTask("update_mode");
        isUpdateMode = false;
        isAgain = false;
        mInstance = null;
    }

    private void startUpdate() {
        byte[] data = new byte[16];
        try {
            FileInputStream inputStream = new FileInputStream(mFile);
            mLength = inputStream.available();
            mData = new byte[mLength + 16];
            for (int i = 0; i < 16; i++) {
                mData[mLength + i] = 0;
            }
            inputStream.read(mData);
            byte[] length = convertBytes(mLength);
            data[0] = length[0];
            data[1] = length[1];
            data[2] = length[2];
            data[3] = length[3];
        } catch (IOException e) {
            e.printStackTrace();
        }
        ServiceManager.getInstance().sendCommand(data, (byte) 0x03);
    }

    public static void main(String[] args) {
        byte[] mData = new byte[]{(byte) 0xa0, (byte) 0x4c, (byte) 0x01, (byte) 0x20, (byte) 0xf5,
                (byte) 0xa1, (byte) 0x00, (byte) 0x0, (byte) 0xfd, (byte) 0xa1, (byte) 0x00,
                (byte) 0x00, (byte) 0xff, (byte) 0xa1, (byte) 0x00, (byte) 0x00};
        byte[] data = new byte[16];
        for (int i = 0, j = 0; i < mData.length && j < 16; i++, j++) {
            byte i2 = (byte) ~mData[i];
            byte j1 = (byte) ((i2 >> 1) & 0x7f);
            int o = i2 & 0x01;
            int p = o << 7;
            int q = p | j1;
            data[j] = (byte) q;
        }
        final StringBuilder stringBuilder = new StringBuilder(data.length);
        for (byte byteChar : data)
            stringBuilder.append(String.format("%02X ", byteChar));
        System.out.println("onAvailableData:" + stringBuilder.toString());
    }

    private void send() {
        byte[] data = new byte[16];
        if (mData == null) {
            stopUpdate();
        }
        for (int j = 0; mOffset < mData.length && j < 16; mOffset++, j++) {
            byte i2 = (byte) ~mData[mOffset];
            byte j1 = (byte) ((i2 >> 1) & 0x7f);
            int o = i2 & 0x01;
            int p = o << 7;
            int q = p | j1;
            data[j] = (byte) q;
        }
        ServiceManager.getInstance().sendCommandNoDelay(data, (byte) 0x05);
        TimerManage.getInstance().stopTimerTask("update_mode");
        TimerManage.getInstance().startTimerTask("update_mode", 3000, new TimerManage
                .TimerTaskListener() {
            @Override
            public void onTimer(String tag, int count) {
                send();
            }
        });
    }

    private void sendEnd() {
        byte[] data = new byte[16];
        ServiceManager.getInstance().sendCommand(data, (byte) 0x07);
    }

    /**
     * @return data[0]最高8位
     */
    private byte[] convertBytes(int value) {
        byte[] data = new byte[4];
        data[0] = (byte) (value >> 24);
        data[1] = (byte) (value >> 16);
        data[2] = (byte) (value >> 8);
        data[3] = (byte) value;
        return data;
    }
}
