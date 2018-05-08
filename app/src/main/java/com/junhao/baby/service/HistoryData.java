package com.junhao.baby.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.PopupWindow;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

public class HistoryData {
    private static final String TAG = "HistoryData";

    private List<String> months;
    private HashMap<String, List<String>> group;
    private boolean isWeek = false;
    private boolean isDay = false;
    private boolean isWeekendChart = false;
    private Bundle bundle;
    private boolean isDayChart = false;
    private Bundle arguments;
    private boolean isYearChart = true;
    private boolean isYear = false;
    private Bundle monthBundle;
    private ExpandableListView historyDataListView;
    private PopupWindow mPop;
    private String language;
    private String[] monthStr = {"", "January", "February", "March", "April", "May", "June",
            "July", "August",
            "September", "October", "November", "December"};

    private int mTimeTableIndex;

    public BroadcastReceiver getReceiver() {
        return mReceiver;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private int second;
        private int minute;
        private int hour;
        private int day;
        private int montn;
        private int year;
        private int deviceTimeSeconde;
        private int tempBytesLength = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            processData(intent.getByteArrayExtra("data"));
        }

        public void processData(byte[] bytes) {
            if (bytes[0] == 0x55 || bytes[0] == 0x56) {
                if (bytes.length == 20) {
                    if (bytes[19] == (byte) 0xaa) {
                        if (bytes[0] == 0x55) {
                            if (checkPackage(bytes)) {
                                saveHistoryData(bytes, deviceTimeSeconde, year, montn, day, hour,
                                        minute, second);
                            }
                        }
                        if (bytes[0] == 0x56) {
                            Log.e("historySend", "接收56");
                            if (checkPackage(bytes)) {
                                mTimeTableIndex = bytes[1];
                                deviceTimeSeconde = byteConvert(bytes[5]) << 24 |
                                        byteConvert(bytes[4]) << 16 |
                                        byteConvert(bytes[3]) << 8 | byteConvert(bytes[2]);
                                byte[] temp = {bytes[6], bytes[7]};
                                try {
                                    String s = new String(temp, "GBK");
                                    temp = new byte[]{bytes[8], bytes[9]};
                                    String s1 = new String(temp, "GBK");
                                    temp = new byte[]{bytes[10], bytes[11]};
                                    String s2 = new String(temp, "GBK");
                                    temp = new byte[]{bytes[12], bytes[13]};
                                    String s3 = new String(temp, "GBK");
                                    temp = new byte[]{bytes[14], bytes[15]};
                                    String s4 = new String(temp, "GBK");
                                    temp = new byte[]{bytes[16], bytes[17]};
                                    String s5 = new String(temp, "GBK");

                                    Log.d("history", "deviceTimeSeconde: " + deviceTimeSeconde +
                                            "year: " + s
                                            + "month: " + s1 + "day: " + s2 + "hour: " + s3 +
                                            "minute: " + s4 + "second: " + s5);
                                    Log.e("sssssssss", s + ">>>" + s1 + ">>>" + s2 + ">>>" + s3 +
                                            ">>>" + s4 + ">>>" + s5);
                                    year = Integer.parseInt(s);
                                    montn = Integer.parseInt(s1);
                                    day = Integer.parseInt(s2);
                                    hour = Integer.parseInt(s3);
                                    minute = Integer.parseInt(s4);
                                    second = Integer.parseInt(s5);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //同步错误
                                }
                            }
                        } else {
                            //数据传输错误
                        }
                    }
                } else if (bytes.length < 20) {
                    if (tempBytesLength == -1) {
                        if (bytes[0] == 0x55 || bytes[0] == 0x56) {
                            for (int i = 0; i < bytes.length; i++) {
                                tempBytes[i] = bytes[i];
                            }
                            tempBytesLength = bytes.length;
                        }
                    }
//                    else {
//
//                        int length = tempBytesLength + bytes.length;
//                        if (length == 20) {
//
//                            for (int i = 0; i < bytes.length; i++) {
//                                tempBytes[tempBytesLength + i] = bytes[i];
//                            }
//                            processData(tempBytes);
//                            tempBytesLength = -1;
//                            tempBytes = new byte[20];
//                        } else if (length < 20) {
//
//                            for (int i = 0; i < bytes.length; i++) {
//                                tempBytes[tempBytesLength + i] = bytes[i];
//                            }
//                            tempBytesLength = length;
//                        } else {
//
//                            mSyncHistoryDataalertDialog.dismiss();
//                            ToastUtils.showToast(HistoryDataChartActivity.this, getString(R
// .string.sync_history_abort));
//                            tempBytesLength = -1;
//                            tempBytes = new byte[20];
//                        }
//                    }

                }
            } else if ((bytes[bytes.length - 1] & 0xff) == 0xaa) {
                if (tempBytesLength != -1) {
                    int length = tempBytesLength + bytes.length;
                    if (length == 20) {
                        for (int i = 0; i < bytes.length; i++) {
                            int i1 = tempBytesLength + i;
                            tempBytes[tempBytesLength + i] = bytes[i];
                        }
                        save(tempBytes);

                        tempBytesLength = -1;
                        tempBytes = new byte[20];
                    }//这里要处理一下
                }
            }//这里也要处理一下
        }

        private void save(byte[] bytes) {

            if (bytes[19] == (byte) 0xaa) {
                if (bytes[0] == 0x55) {

                    if (checkPackage(bytes)) {
                        saveHistoryData(bytes, deviceTimeSeconde, year, montn, day, hour, minute,
                                second);
                    }
                }

                if (bytes[0] == 0x56) {
                    Log.e("historySend", "接收56");

                    if (checkPackage(bytes)) {
                        mTimeTableIndex = bytes[1];
                        deviceTimeSeconde = byteConvert(bytes[5]) << 24 |
                                byteConvert(bytes[4]) << 16 |
                                byteConvert(bytes[3]) << 8 | byteConvert(bytes[2]);


                        byte[] temp = {bytes[6], bytes[7]};
                        try {

                            String s = new String(temp, "GBK");
                            temp = new byte[]{bytes[8], bytes[9]};
                            String s1 = new String(temp, "GBK");
                            temp = new byte[]{bytes[10], bytes[11]};
                            String s2 = new String(temp, "GBK");
                            temp = new byte[]{bytes[12], bytes[13]};
                            String s3 = new String(temp, "GBK");
                            temp = new byte[]{bytes[14], bytes[15]};
                            String s4 = new String(temp, "GBK");
                            temp = new byte[]{bytes[16], bytes[17]};
                            String s5 = new String(temp, "GBK");

                            Log.d("history", "deviceTimeSeconde: " + deviceTimeSeconde + "year: "
                                    + s
                                    + "month: " + s1 + "day: " + s2 + "hour: " + s3 + "minute: "
                                    + s4 + "second: " + s5);
                            Log.e("sssssssss", s + ">>>" + s1 + ">>>" + s2 + ">>>" + s3 + ">>>" +
                                    s4 + ">>>" + s5);
                            year = Integer.parseInt(s);
                            montn = Integer.parseInt(s1);
                            day = Integer.parseInt(s2);
                            hour = Integer.parseInt(s3);
                            minute = Integer.parseInt(s4);
                            second = Integer.parseInt(s5);

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            //同步错误
                        }
                    }
                } else {
                    //数据传输错误
                }
            }
        }
    };
    private int mLastDeviceTime = -1;
    private double mLastDose;
    private int mTableIndex;
    private byte[] tempBytes = new byte[20];

    private int byteConvert(byte b) {

        return (b & 0xff);
    }

    private int byteC(byte b) {
        return b;
    }

    /**
     * 存储从设备同步的历史数据
     *
     * @param bytes
     */
    private void saveHistoryData(byte[] bytes, int deviceTime, int year, int month, int day, int
            hour, int minute, int second) {
        switch ((int) bytes[1]) {
            case 0x14:
                Log.e("historySend", "接收14");
                int currentDeviceTime = byteConvert(bytes[5]) << 24 | byteConvert(bytes[4]) << 16 |
                        byteConvert(bytes[3]) << 8 | byteConvert(bytes[2]);

                long mata = byteConvert(bytes[9]) << 24 | byteConvert(bytes[8]) << 16 |
                        byteConvert(bytes[7]) << 8 | byteConvert(bytes[6]);
                double dose = mata / 1000.0;

                Log.e("history", "currentDeviceTime: " + currentDeviceTime + "dose: " + dose);
                int doseUnit = bytes[10];
                int tableIndex = bytes[11];
                if (mTableIndex != tableIndex) {
                    mLastDeviceTime = -1;
                    mTableIndex = tableIndex;
                }
                if (mLastDeviceTime > 0) {

                    int i = currentDeviceTime - mLastDeviceTime;
                    if (i > 10 && i < 60) {
                        mLastDeviceTime += 10;
                        while (mLastDeviceTime < currentDeviceTime) {

                            String[] strings = convertCalendar(mLastDeviceTime, deviceTime,
                                    second, minute, hour, day, month, year);
                            saveDataToDB(strings, mLastDose, doseUnit, 10);
                            mLastDeviceTime += 10;

                        }

                        int dur = currentDeviceTime - (mLastDeviceTime - 10);
                        mLastDose = dose;
                        mLastDeviceTime = currentDeviceTime;
                        String[] strings = convertCalendar(currentDeviceTime, deviceTime, second,
                                minute, hour, day, month, year);
                        saveDataToDB(strings, dose, doseUnit, dur);


                    } else if (i >= 60 && i < 120) {
                        mLastDeviceTime += 20;
                        while (mLastDeviceTime < currentDeviceTime) {

                            String[] strings = convertCalendar(mLastDeviceTime, deviceTime,
                                    second, minute, hour, day, month, year);
                            saveDataToDB(strings, mLastDose, doseUnit, 20);
                            mLastDeviceTime += 20;
                        }

                        int dur = currentDeviceTime - (mLastDeviceTime - 20);
                        mLastDose = dose;
                        mLastDeviceTime = currentDeviceTime;
                        String[] strings = convertCalendar(currentDeviceTime, deviceTime, second,
                                minute, hour, day, month, year);
                        saveDataToDB(strings, dose, doseUnit, dur);
                    } else if (i >= 120) {
                        mLastDeviceTime += 30;
                        while (mLastDeviceTime < currentDeviceTime) {

                            String[] strings = convertCalendar(mLastDeviceTime, deviceTime,
                                    second, minute, hour, day, month, year);
                            saveDataToDB(strings, mLastDose, doseUnit, 30);
                            mLastDeviceTime += 30;
                        }

                        int dur = currentDeviceTime - (mLastDeviceTime - 30);
                        mLastDose = dose;
                        mLastDeviceTime = currentDeviceTime;
                        String[] strings = convertCalendar(currentDeviceTime, deviceTime, second,
                                minute, hour, day, month, year);
                        saveDataToDB(strings, dose, doseUnit, dur);
                    } else {
                        int dur = currentDeviceTime - mLastDeviceTime;
                        mLastDose = dose;
                        mLastDeviceTime = currentDeviceTime;
                        String[] strings = convertCalendar(currentDeviceTime, deviceTime, second,
                                minute, hour, day, month, year);
                        saveDataToDB(strings, dose, doseUnit, dur);
                    }

                } else {

                    mLastDeviceTime = currentDeviceTime;
                    mLastDose = dose;
                    String[] strings = convertCalendar(currentDeviceTime, deviceTime, second,
                            minute, hour, day, month, year);
                    saveDataToDB(strings, dose, doseUnit, 1);
                }
                break;
        }
    }

    private void saveDataToDB(String[] s, double dose, int doseUnit, int dur) {
        Log.d(TAG, "saveDataToDB: s=" + " do" + dose + " unit" + doseUnit + " dur" + dur);
    }

    private String[] convertCalendar(int currentDeviceTime, int deviceTime, int second, int minute,
                                     int hour, int day, int month, int year) {
        int DValue = (currentDeviceTime - deviceTime);
        if (DValue >= 0) {
            second = second + DValue % 60;

            if (second > 60) {
                second = second - 60;
                minute = minute + 1;
            }
            minute = minute + DValue / 60;
            while (minute >= 60) {
                minute = minute - 60;
                hour++;
                if (hour >= 24) {
                    day++;
                    hour = hour - 24;
                    if (day > 28) {
                        if (month == 2) {

                            if (year % 400 == 0) {
                                if (day > 29) {
                                    day = day - 29;
                                    month++;
                                }
                            } else if (year % 4 == 0) {
                                if (day > 29) {
                                    day = day - 29;
                                    month++;
                                }
                            } else {
                                day = day - 28;
                                month++;
                            }


                        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
                            if (day > 30) {
                                day = day - 30;
                                month++;
                            }
                        } else {
                            if (day > 31) {
                                day = day - 31;
                                month++;
                            }
                        }

                        if (month > 12) {
                            month = month - 12;
                            year++;
                        }

                    }

                }
            }
        } else {
            second = second + DValue % 60;
            if (second < 0) {
                second = 60 + second;
                minute--;
            }

            minute = minute + DValue / 60;
            while (minute < 0) {
                minute = 60 + minute;
                hour--;
                if (hour < 0) {
                    hour = 24 + hour;
                    day--;

                    if (day <= 0) {
                        month--;
                        if (month == 2) {

                            if (year % 400 == 0 || year % 4 == 0) {
                                day = day + 29;
                            } else {
                                day = day + 28;
                            }
                        } else if (month == 4 || month == 6 || month == 9 || month == 11) {

                            day = day + 30;
                        } else {

                            day = day + 31;

                        }


                    }
                }

                if (month <= 0) {
                    month = month + 12;
                    year--;
                }

            }

        }
        String date = "20" + year + ":" + month + ":" + day;
        String time = hour + ":" + minute + ":" + second;
        String[] s = {date, time, "20" + year, month + "", day + ""};
        return s;
    }

    private boolean checkPackage(byte[] bytes) {
        byte temp = bytes[0];
        for (int i = 1; i < bytes.length - 2; i++) {
            temp = (byte) (temp ^ bytes[i]);
        }
        return temp == bytes[bytes.length - 2];
    }
}

