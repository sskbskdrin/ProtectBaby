package com.junhao.baby.bean;

import android.content.Context;

import com.junhao.baby.utils.SpfUtil;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */

public class User {
    private static final String NAME = "name";
    private static final String BABY_NAME = "baby_name";
    private static final String CONCEIVE_DATE = "conceive_date";
    private static final String IS_LOGIN = "is_login";
    private static final String BIND_ADDRESS = "bind_address";
    private static final String BIND_NAME = "bind_name";
    private static final String THEME_NAME = "theme_name";
    private static final String HISTORY_TYPE = "history_type";

    private static final String HEAD_FILE_NAME = "head_file";


    private String name;
    private String babyName;

    /**
     * 受孕日期
     */
    private long conceiveDate;

    private String themeName;

    public static boolean isLogin() {
        return SpfUtil.getBoolean(IS_LOGIN, false);
    }

    public static void login() {
        SpfUtil.saveBoolean(IS_LOGIN, true);
    }

    private static User mInstance;

    private User() {
        load();
    }

    public static User getInstance() {
        if (mInstance == null) {
            synchronized (User.class) {
                if (mInstance == null) {
                    mInstance = new User();
                }
            }
        }
        return mInstance;
    }

    public String getHeadPath(Context context) {
        return context.getCacheDir().getAbsolutePath() + HEAD_FILE_NAME;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        SpfUtil.saveString(NAME, name);
    }

    public String getBabyName() {
        return babyName;
    }

    public void setBabyName(String babyName) {
        this.babyName = babyName;
        SpfUtil.saveString(BABY_NAME, babyName);
    }

    public long getConceiveDate() {
        return conceiveDate;
    }

    public void setConceiveDate(long conceiveDate) {
        this.conceiveDate = conceiveDate;
        SpfUtil.saveLong(CONCEIVE_DATE, conceiveDate);
    }

    private void load() {
        name = SpfUtil.getString(NAME);
        babyName = SpfUtil.getString(BABY_NAME);
        conceiveDate = SpfUtil.getLong(CONCEIVE_DATE);
    }

    public void save() {
        SpfUtil.saveString(NAME, name);
        SpfUtil.saveString(BABY_NAME, babyName);
        SpfUtil.saveLong(CONCEIVE_DATE, conceiveDate);
    }

    public void bindAddress(String name, String address) {
        SpfUtil.saveString(BIND_NAME, name);
        SpfUtil.saveString(BIND_ADDRESS, address);
    }

    public String getDeviceAddress() {
        return SpfUtil.getString(BIND_ADDRESS, "");
    }

    public String getDeviceName() {
        return SpfUtil.getString(BIND_NAME);
    }

    public String getHistoryType() {
        return SpfUtil.getString(HISTORY_TYPE, "月");
    }

    public void setHistoryType(String type) {
        SpfUtil.saveString(HISTORY_TYPE, type);
    }

    public String getThemeName() {
        return SpfUtil.getString(THEME_NAME, "Red");
    }

    public void setThemeName(String themeName) {
        SpfUtil.saveString(THEME_NAME, themeName);
    }
}
