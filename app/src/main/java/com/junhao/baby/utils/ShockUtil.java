package com.junhao.baby.utils;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

/**
 * 震动工具类
 *
 * @date 2013-9-5
 * @time 上午9:26:01
 * @where
 */
public class ShockUtil {
    private static boolean isVibrator = false;

    /**
     * 自定义震动模式
     *
     * @param context
     * @param pattern  数组中数字的含义依次是静止的时长，震动时长，静止时长，震动时长。 单位是毫秒
     * @param isRepeat 是否反复震动，如果是true，反复震动，如果是false，只震动一次
     */
    public static void startVibrate(Context context, long[] pattern, boolean isRepeat) {
        if (!isVibrator) {
            Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
            if (vib != null && vib.hasVibrator()) {
                vib.vibrate(pattern, isRepeat ? 2 : -1);
            }
            isVibrator = true;
        }
    }

    public static void stopVibrate(Context context) {
        if (isVibrator) {
            Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
            if (vib != null && vib.hasVibrator()) {
                vib.cancel();
            }
            isVibrator = false;
        }
    }

    public static boolean isVibrator() {
        return isVibrator;
    }
}
