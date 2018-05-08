package com.junhao.baby.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * SharedPreference 工具类
 *
 * @author keayuan001
 * @date 2013-9-9
 * @time 上午11:42:54
 */
public class SpfUtil {
    private static final String TAG = "SpfUtil";
    private static final String NAME = "BaBy";

    private static SharedPreferences mSharedPreferences;

    /**
     * 移除某个key值已经对应的值
     *
     * @param key
     */
    public static void remove(String key) {
        if (isExists(key)) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.remove(key);
            editor.apply();
        }
    }

    public static void init(Context context) {
        init(context, NAME);
    }

    public static void init(Context context, String name) {
        init(context, name, Context.MODE_PRIVATE);
    }

    public static void init(Context context, String name, int mode) {
        if (context != null) {
            mSharedPreferences = context.getSharedPreferences(name, mode);
        }
    }

    public static boolean isInit() {
        if (mSharedPreferences == null) {
            Log.e(TAG, "sharePreferences is null");
        }
        return mSharedPreferences != null;
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param key
     * @return
     */
    public static boolean isExists(String key) {
        return isInit() && mSharedPreferences.contains(key);
    }

    /**
     * @param key   关键字
     * @param value 存储值
     */
    public static void saveInt(String key, int value) {
        if (isInit()) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt(key, value);
            editor.apply();
        }
    }

    /**
     * @param key          关键字
     * @param defaultValue 默认值
     */
    public static int getInt(String key, int defaultValue) {
        if (isInit()) {
            return mSharedPreferences.getInt(key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * @param key 关键字
     * @return 默认0
     */
    public static int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * @param key   关键字
     * @param value 存储值
     */
    public static void saveBoolean(String key, boolean value) {
        if (isInit()) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(key, value);
            editor.apply();
        }
    }

    /**
     * @param key 关键字
     * @return 默认false
     */
    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        if (isInit()) {
            return mSharedPreferences.getBoolean(key, false);
        }
        return defaultValue;
    }

    /**
     * @param key   关键字
     * @param value 存储值
     */
    public static void saveString(String key, String value) {
        if (isInit()) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    /**
     * @param key 关键字
     * @return 默认""
     */
    public static String getString(String key) {
        return getString(key, "");
    }

    /**
     * @param key          关键字
     * @param defaultValue 默认值
     */
    public static String getString(String key, String defaultValue) {
        if (isInit()) {
            return mSharedPreferences.getString(key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * @param key   关键字
     * @param value 存储值
     */
    public static void saveFloat(String key, float value) {
        if (isInit()) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putFloat(key, value);
            editor.apply();
        }
    }

    /**
     * @param key 关键字
     * @return 默认值0
     */
    public static float getFloat(String key) {
        return getFloat(key, 0);
    }

    /**
     * @param key 关键字
     * @return 默认值0
     */
    public static float getFloat(String key, float defaultValue) {
        if (isInit()) {
            return mSharedPreferences.getFloat(key, defaultValue);
        }
        return 0;
    }

    /**
     * @param key   关键字
     * @param value 存储值
     */
    public static void saveLong(String key, long value) {
        if (isInit()) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putLong(key, value);
            editor.apply();
        }
    }

    /**
     * @param key 关键字
     * @return 默认0
     */
    public static long getLong(String key) {
        if (isInit()) {
            return mSharedPreferences.getLong(key, 0);
        }
        return 0;
    }

    /**
     * 将所有SharedPreferences文件删除
     */
    public static void clear() {
        if (isInit()) {
            mSharedPreferences.edit().clear().apply();
        }
    }
}
