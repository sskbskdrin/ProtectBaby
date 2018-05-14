package com.junhao.baby.service;

import android.util.Log;

import com.junhao.baby.bean.DosageBean;
import com.junhao.baby.bean.DosageTableBean;
import com.junhao.baby.db.DosageDao;
import com.junhao.baby.db.DosageTableDao;
import com.junhao.baby.db.ThreadPool;
import com.junhao.baby.utils.DateFormatUtil;
import com.junhao.baby.utils.L;
import com.junhao.baby.utils.TimerManage;

import java.util.Date;

/**
 * Created by sskbskdrin on 2018/三月/13.
 */

public class SyncHistoryData {
    private static final String TAG = "SyncHistoryData";

    private static SyncHistoryData mInstance;
    public static boolean isSyncMode;
    public static boolean isSyncComplete = false;

    private DosageTableBean mLastTable;
    private int sendCount = 0;
    private int repeatCount = 0;
    private byte[] tempData;

    private ServiceManager.Callback mCallback = new ServiceManager.Callback() {
        @Override
        public void call(char key, byte[] data, String value) {
            TimerManage.getInstance().stopTimerTask("send_command");
            if ('S' == key) {
                isSyncMode = true;
                requireTimeTable();
            } else if (ServiceManager.SYNC_MODE == key) {
                sendCount = 0;
                if (data[0] == 0x56) {
                    onTimeTable(data);
                    requireHistoryData();
                } else {
                    if (data[1] == 0x14) {
                        if (tempData == null) {
                            tempData = data;
                        }
                        boolean isRepeat = true;
                        for (int i = 0; i < data.length; i++) {
                            if (tempData[i] != data[i]) {
                                isRepeat = false;
                            }
                        }
                        if (isRepeat) {
                            repeatCount++;
                        } else {
                            repeatCount = 0;
                        }
                        tempData = data;
                        if (repeatCount > 6) {
                            tempData = null;
                            repeatCount = 0;
                            requireTimeTable();
                            return;
                        }
                        onHistoryData(data);
                        requireHistoryData();
                    } else if (data[1] == 0x15) {
                        onTableEnd();
                        requireTimeTable();
                    } else if (data[1] == 0x16) {
                        onEnd();
                        requireShutdownTimeTable();
                    } else if (data[1] == 0x18) {
                        onShutdownDosage(data);
                    } else if (data[1] == 0x1a) {
                        onSendShutdownTotalDosageEnd();
                    }
                }
            }
        }
    };

    private SyncHistoryData() {
        ServiceManager.getInstance().addCallback(ServiceManager.SYNC_MODE, mCallback);
        ServiceManager.getInstance().addCallback('S', mCallback);
    }

    public static void comeInMode() {
        if (mInstance == null) {
            mInstance = new SyncHistoryData();
        }
        String time = DateFormatUtil.format(new Date(), "yyMMddHHmmss");
        ServiceManager.getInstance().sendCommand("# sy " + time + " $");
    }

    private void requireTimeTable() {
        sendCount = 0;
        send(new byte[16], (byte) 0x11);
    }

    private void requireHistoryData() {
        sendCount = 0;
        send(new byte[16], (byte) 0x13);
    }

    private void requireShutdownTimeTable() {
        sendCount = 0;
        send(new byte[16], (byte) 0x17);
    }

    private void sendShutdownTotalDosage(int dosage, int unit) {
        byte[] data = new byte[16];
        data[3] = (byte) (dosage >> 24);
        data[2] = (byte) (dosage >> 16);
        data[1] = (byte) (dosage >> 8);
        data[0] = (byte) dosage;
        data[4] = (byte) unit;
        sendCount = 0;
        send(data, (byte) 0x19);
    }

    private void onTimeTable(byte[] data) {
        DosageTableBean bean = new DosageTableBean();
        bean.index = getInt(data[1]);
        bean.second = convert(data[2], data[3], data[4], data[5]);
        bean.date = DateFormatUtil.parseDate("20" + new String(data, 6, 12), DateFormatUtil.YYMDHMS).getTime() / 1000;
        bean.name = ServiceManager.getInstance().getDeviceName();
        bean.address = ServiceManager.getInstance().getDeviceAddress();
        if (bean.date > 0) {
            mLastTable = bean;
            DosageTableDao.getInstance().addOrUpdate(bean);
        } else {
            mLastTable = null;
        }
        if (L.IS_DEBUG) {
            L.d(TAG, "onTimeTable:time=" + DateFormatUtil.format(bean.date * 1000, DateFormatUtil.YY_M_D_H_M_S) + " "
                + "second=" + bean.second);
        }
    }

    private void onHistoryData(byte[] data) {
        int count = 0;
        int sum = 0;
        for (int i = 2; i <= 12; i++) {
            if (data[i] == (byte) 0xff) {
                sum++;
            } else {
                sum = 0;
            }
            if (sum > count) {
                count = sum;
            }
        }
        if (count >= 3 || data[10] == (byte) 0xff || mLastTable == null) {
            return;
        }
        int second = convert(data[2], data[3], data[4], data[5]);
        DosageBean bean = DosageBean.obtain();
        double dosagePreS = convert(data[6], data[7], data[8], data[9]) / 1000f / 3600f;
        int unit = getInt(data[10]);
        if (unit > 0) {
            dosagePreS *= 1000;
        }
        if (unit > 1) {
            dosagePreS *= 1000;
        }
        bean.name = ServiceManager.getInstance().getDeviceName();
        bean.address = ServiceManager.getInstance().getDeviceAddress();
        bean.time = mLastTable.date - mLastTable.second + second;
        bean.dosageEachH = (float) (dosagePreS * 3600);
        bean.unit = 0;

        DosageBean temp = DosageDao.getInstance().queryLastForTime(bean.time, bean.address);
        if (temp != null) {
            bean.dosage = temp.dosage + dosagePreS * (bean.time - temp.time);
        } else {
            bean.dosage = 0;
        }
        if (bean.dosage > 0.00001f) {
            DosageDao.getInstance().addOrUpdate(bean);
        }
        if (L.IS_DEBUG) {
            L.d(TAG, "onHistoryData:data=" + DateFormatUtil.format(bean.time * 1000, DateFormatUtil.YY_M_D_H_M_S) +
                "" + " dosage=" + bean.dosage + " unit=" + bean.unit);
        }
    }

    private void onTableEnd() {
        L.d(TAG, "当前表同步完成");
    }

    private void onEnd() {
        L.d(TAG, "历史数据同步完成");
    }

    private void onShutdownDosage(byte[] data) {
        int index = getInt(data[2]);
        long second = convert(data[3], data[4], data[5], data[6]);
        final double dosagePreS = convert(data[7], data[8], data[9], data[10]) / 1000f / 3600.0f;
        final int unit = getInt(data[11]);
        int bootSecond = convert(data[12], data[13], data[14], data[15]);
        int flag = getInt(data[16]);
        if (L.IS_DEBUG) {
            L.d(TAG, "onShutdownDosage:flag=" + flag + " pre=" + dosagePreS + " second=" + second + " " +
                "bootSecond=" + bootSecond);
        }
        if (flag == 0) {
            onSendShutdownTotalDosageEnd();
        } else {
            int count = 0;
            int sum = 0;
            for (byte d : data) {
                if (d == (byte) 0xff) {
                    sum++;
                } else {
                    sum = 0;
                }
                if (sum > count) {
                    count = sum;
                }
            }
            if (count >= 4 || mLastTable == null) {
                onSendShutdownTotalDosageEnd();
                return;
            }
            DosageTableBean tableBean = DosageTableDao.getInstance().queryForIdAndAddress(index, ServiceManager
                .getInstance().getDeviceAddress());
            if (tableBean == null) {
                tableBean = new DosageTableBean();
                tableBean.date = mLastTable.date - bootSecond;
            }
            if (L.IS_DEBUG) {
                L.d(TAG, "onShutdownDosage:date=" + DateFormatUtil.format(tableBean.date * 1000, DateFormatUtil
                    .YY_M_D_H_M_S) + " syncDate=" + DateFormatUtil.format(mLastTable.date * 1000, DateFormatUtil
                    .YY_M_D_H_M_S));
            }
            final long shutdownTime = tableBean.date;
            final long bootTime = mLastTable.date - bootSecond;
            if (bootTime < shutdownTime) {
                onSendShutdownTotalDosageEnd();
                return;
            }
            final int newDosage = (int) ((bootTime - shutdownTime) * dosagePreS * 1000);
            if (L.IS_DEBUG) {
                L.d(TAG, "onShutdownDosage:shutdown time=" + DateFormatUtil.format(shutdownTime * 1000,
                    DateFormatUtil.YY_M_D_H_M_S) + " boot time=" + DateFormatUtil.format(bootTime * 1000,
                    DateFormatUtil.YY_M_D_H_M_S));
            }

            ThreadPool.execute(new ThreadPool.Callback<Object>() {
                @Override
                public Object background() {
                    long itemTime = shutdownTime;
                    DosageBean last = DosageDao.getInstance().queryLastForTime(itemTime, ServiceManager.getInstance()
                        .getDeviceAddress());
                    if (last == null) {
                        last = DosageBean.obtain();
                    }
                    while (itemTime < bootTime) {
                        itemTime += 600;
                        DosageBean bean = DosageBean.obtain();
                        bean.time = itemTime;
                        bean.unit = 0;
                        bean.address = last.address;
                        bean.name = last.name;
                        bean.dosage = last.dosage + dosagePreS * 600;
                        bean.dosageEachH = (float) (dosagePreS * 3600);
                        bean.name = ServiceManager.getInstance().getDeviceName();
                        bean.address = ServiceManager.getInstance().getDeviceAddress();
                        DosageDao.getInstance().addOrUpdate(bean);
                        last.recycle();
                        last = bean;
                        if (L.IS_DEBUG) {
                            L.d(TAG, "onShutdownDosage:time=" + DateFormatUtil.format(bean.time * 1000,
                                DateFormatUtil.YY_M_D_H_M_S) + " dosage=" + bean.dosage);
                        }
                    }
                    return null;
                }

                @Override
                public void callback(Object o) {
                    sendShutdownTotalDosage(newDosage, unit);
                }
            });
        }
    }

    private void onSendShutdownTotalDosageEnd() {
        L.d(TAG, "同步结束");
        isSyncMode = false;
        isSyncComplete = true;
        ServiceManager.getInstance().sendCommand(ServiceManager.CMD_SYNC_STATE);
        ServiceManager.getInstance().startSyncRealTimeData();
        mInstance = null;
    }

    private void send(final byte[] data, final byte type) {
        ServiceManager.getInstance().sendCommand(data, type);
        TimerManage.getInstance().stopTimerTask("send_command");
        TimerManage.getInstance().startTimerTask("send_command", 2000, new TimerManage.TimerTaskListener() {
            @Override
            public void onTimer(String tag, int count) {
                Log.d(TAG, "onTimer: " + tag);
                sendCount++;
                if (sendCount >= 10) {
                    onSendShutdownTotalDosageEnd();
                    return;
                }
                send(data, type);
            }
        });
    }

    /**
     * @param b1 最低8位
     * @param b4 最高8位
     */
    private int convert(byte b1, byte b2, byte b3, byte b4) {
        int temp = 0xff & b4;
        temp = temp << 8 | (0xff & b3);
        temp = temp << 8 | (0xff & b2);
        temp = temp << 8 | (0xff & b1);
        return temp;
    }

    private int getInt(byte value) {
        return 0xff & value;
    }
}
