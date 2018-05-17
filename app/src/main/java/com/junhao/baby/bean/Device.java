package com.junhao.baby.bean;

import android.text.TextUtils;

import com.junhao.baby.utils.CommonUtils;
import com.junhao.baby.utils.SpfUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sskbskdrin on 2018/三月/10.
 */

public class Device {

    public static final String CURRENT_DEVICE = "current_device";

    public static final String LAST_DOSAGE = "last_dosage";
    public static final String LAST_TEMPERATURE = "last_temperature";
    public static final String LAST_BATTERY = "last_battery";
    public static final String LAST_TOTAL_DOSAGE = "last_total_dosage";
    public static final String LAST_ALERT_THRESHOLD = "last_alert_threshold";
    public static final String LAST_ALERT_TOTAL_THRESHOLD = "last_alert_total_threshold";
    public static final String LAST_UPDATE_TIME = "last_update_time";

    public static final String DEVICE_NUMBER = "device_number";
    public static final String ALERT_VIBRATOR = "alert_vibrator";
    public static final String ALERT_VOICE = "alert_voice";
    public static final String ALERT_PHONE_VIBRATOR = "alert_phone_vibrator";
    public static final String ALERT_PHONE_VOICE = "alert_phone_voice";

    private static final String DEVICE_LIST = "device_list";

    //    private static final String STATUS_DEVICE_NOTIFY = "status_device_notify";
    //    private static final String STATUS_PHONE_NOTIFY = "status_phone_notify";

    public static final String BACK_LIGHT_TIME = "back_light_time";

    public static final int TYPE_NONE = 1000;
    public static final int TYPE_VOICE = 1001;
    public static final int TYPE_VIBRATOR = 1002;
    public static final int TYPE_VOICE_VIBRATOR = 1003;

    public static final List<Item> DEVICE_STATE_LIST = new ArrayList<>(3);
    public static final List<Item> DEVICE_BL_TIME_LIST = new ArrayList<>(3);

    static {
        DEVICE_STATE_LIST.add(new Item("声音", TYPE_VOICE));
        DEVICE_STATE_LIST.add(new Item("震动", TYPE_VIBRATOR));
        DEVICE_STATE_LIST.add(new Item("声音和震动", TYPE_VOICE_VIBRATOR));

        DEVICE_BL_TIME_LIST.add(new Item("5秒", 5));
        DEVICE_BL_TIME_LIST.add(new Item("10秒", 10));
        DEVICE_BL_TIME_LIST.add(new Item("15秒", 15));
        DEVICE_BL_TIME_LIST.add(new Item("常亮", 9005));
    }

    private Device() {
    }

    public Device(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public String address;
    public String name;

    public String toSimpleString() {
        return address + "=" + name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Device) {
            Device temp = (Device) obj;
            if (address == null) {
                return TextUtils.isEmpty(temp.address);
            } else {
                return address.equals(temp.address);
            }
        }
        return false;
    }

    private static Device parse(String value) {
        if (!TextUtils.isEmpty(value)) {
            String[] str = value.split("=");
            if (str.length >= 2) {
                return new Device(str[0], str[1]);
            }
        }
        return new Device();
    }

    public static List<Device> getDeviceList() {
        String value = SpfUtil.getString(DEVICE_LIST);
        List<Device> list = new ArrayList<>(5);
        if (!TextUtils.isEmpty(value)) {
            String[] item = value.split("&");
            for (String d : item) {
                list.add(parse(d));
            }
        }
        return list;
    }

    public static Device addDevice(String address, String name) {
        List<Device> list = getDeviceList();
        Device device = new Device(address, name);
        if (list.indexOf(device) < 0) {
            list.add(device);
            saveDevice(list);
        }
        return device;
    }

    private static void saveDevice(List<Device> list) {
        StringBuilder builder = new StringBuilder();
        if (list != null) {
            for (Device d : list) {
                builder.append(d.toSimpleString());
                builder.append("&");
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            }
        }
        SpfUtil.saveString(DEVICE_LIST, builder.toString());
    }

    public static int getNotifyType() {
        boolean voice = isVoice();
        boolean vibrator = isVibrator();
        if (voice && vibrator) {
            return TYPE_VOICE_VIBRATOR;
        } else if (vibrator) {
            return TYPE_VIBRATOR;
        } else if (voice) {
            return TYPE_VOICE;
        } else {
            return TYPE_NONE;
        }
    }

    public static int getPosition() {
        boolean voice = isVoice();
        boolean vibrator = isVibrator();
        if (voice && vibrator) {
            return 2;
        } else if (vibrator) {
            return 1;
        } else if (voice) {
            return 0;
        } else {
            return -1;
        }
    }

    public static int getPhoneNotifyType() {
        boolean voice = SpfUtil.getBoolean(ALERT_PHONE_VOICE);
        boolean vibrator = SpfUtil.getBoolean(ALERT_PHONE_VIBRATOR);
        if (voice && vibrator) {
            return TYPE_VOICE_VIBRATOR;
        } else if (vibrator) {
            return TYPE_VIBRATOR;
        } else if (voice) {
            return TYPE_VOICE;
        } else {
            return TYPE_NONE;
        }
    }

    public static int getPhonePosition() {
        boolean voice = SpfUtil.getBoolean(ALERT_PHONE_VOICE);
        boolean vibrator = SpfUtil.getBoolean(ALERT_PHONE_VIBRATOR);
        if (voice && vibrator) {
            return 2;
        } else if (vibrator) {
            return 1;
        } else if (voice) {
            return 0;
        } else {
            return -1;
        }
    }

    public static boolean getStatusPhoneNotify() {
        return SpfUtil.getBoolean(ALERT_PHONE_VOICE) || SpfUtil.getBoolean(ALERT_PHONE_VIBRATOR);
    }

    public static Item getItemByType(int type) {
        switch (type) {
            case TYPE_VOICE:
                return DEVICE_STATE_LIST.get(0);
            case TYPE_VIBRATOR:
                return DEVICE_STATE_LIST.get(1);
            case TYPE_VOICE_VIBRATOR:
                return DEVICE_STATE_LIST.get(2);
            default:
                return new Item("无通知", type);
        }
    }

    public static String getBLTime() {
        int time = getBackLightTime();
        return time == 9005 ? "常亮" : time + "秒";
    }

    public static int getBLTimePosition() {
        switch (getBackLightTime()) {
            case 5:
                return 0;
            case 10:
                return 1;
            case 15:
                return 2;
            case 9005:
                return 3;
            default:
                return -1;
        }
    }

    public static int getBackLightTime() {
        return SpfUtil.getInt(BACK_LIGHT_TIME, 9005);
    }

    public static void setBackLightTime(int backLightTime) {
        SpfUtil.saveInt(BACK_LIGHT_TIME, backLightTime);
        updateTime();
    }

    public static boolean isAlertStatus() {
        return SpfUtil.getBoolean(ALERT_VOICE) || SpfUtil.getBoolean(ALERT_VIBRATOR);
    }

    public static boolean isVoice() {
        return SpfUtil.getBoolean(ALERT_VOICE);
    }

    public static void setVoice(boolean voice) {
        SpfUtil.saveBoolean(ALERT_VOICE, voice);
        updateTime();
    }

    public static boolean isVibrator() {
        return SpfUtil.getBoolean(ALERT_VIBRATOR);
    }

    public static void setVibrator(boolean vibrator) {
        SpfUtil.saveBoolean(ALERT_VIBRATOR, vibrator);
        updateTime();
    }

    public static boolean isPhoneVoice() {
        return SpfUtil.getBoolean(ALERT_PHONE_VOICE);
    }

    public static boolean isPhoneVibrator() {
        return SpfUtil.getBoolean(ALERT_PHONE_VIBRATOR);
    }

    public static void setPhoneVoice(boolean voice) {
        SpfUtil.saveBoolean(ALERT_PHONE_VOICE, voice);
    }

    public static void setPhoneVibrator(boolean vibrator) {
        SpfUtil.saveBoolean(ALERT_PHONE_VIBRATOR, vibrator);
    }

    public static float getAlertThreshold() {
        return SpfUtil.getFloat(LAST_ALERT_THRESHOLD, 0.32f);
    }

    public static void setAlertThreshold(String value) {
        if (CommonUtils.isNumber(value)) {
            float threshold = Float.parseFloat(value);
            SpfUtil.saveFloat(LAST_ALERT_THRESHOLD, threshold);
            updateTime();
        }
    }

    public static float getAlertTotalThreshold() {
        return SpfUtil.getFloat(LAST_ALERT_TOTAL_THRESHOLD, 2800f);
    }

    public static void setAlertTotalThreshold(String value) {
        if (CommonUtils.isNumber(value)) {
            float threshold = Float.parseFloat(value);
            SpfUtil.saveFloat(LAST_ALERT_TOTAL_THRESHOLD, threshold);
            updateTime();
        }
    }

    public static String getDeviceNumber() {
        return SpfUtil.getString(DEVICE_NUMBER);
    }

    public static void setDeviceNumber(String deviceNumber) {
        SpfUtil.saveString(DEVICE_NUMBER, deviceNumber);
        updateTime();
    }

    public static float getLastDosage() {
        return SpfUtil.getFloat(LAST_DOSAGE);
    }

    public static void setLastDosage(float lastDosage) {
        SpfUtil.saveFloat(LAST_DOSAGE, lastDosage);
        updateTime();
    }

    public static float getLastTotalDosage() {
        return SpfUtil.getFloat(LAST_TOTAL_DOSAGE, -1);
    }

    public static void setLastTotalDosage(float lastTotalDosage) {
        SpfUtil.saveFloat(LAST_TOTAL_DOSAGE, lastTotalDosage);
        updateTime();
    }

    public static int getLastTemperature() {
        return SpfUtil.getInt(LAST_TEMPERATURE);
    }

    public static void setLastTemperature(int lastTemperature) {
        SpfUtil.saveInt(LAST_TEMPERATURE, lastTemperature);
        updateTime();
    }

    public static int getLastBattery() {
        return SpfUtil.getInt(LAST_BATTERY);
    }

    public static void setLastBattery(int lastBattery) {
        SpfUtil.saveInt(LAST_BATTERY, lastBattery);
        updateTime();
    }

    public static long getLastUpdateTime() {
        return SpfUtil.getInt(LAST_UPDATE_TIME);
    }

    private static void updateTime() {
        SpfUtil.saveLong(LAST_UPDATE_TIME, System.currentTimeMillis());
    }
}
