/*
 * 文件名: DateFormatUtil
 * 版    权：  Copyright  LiJinHua  All Rights Reserved.
 * 描    述: [常量类]
 * 创建人: LiJinHua
 * 创建时间:  2014-3-21
 * 
 * 修改人：
 * 修改时间:
 * 修改内容：[修改内容]
 */
package com.junhao.baby.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * DateFormatUtil时间日期格式
 *
 * @author EX-KEAYUAN001
 * @date 2017-10-12
 */
public class DateFormatUtil {

    private static final int WEE_HOURS = 6;
    private static final int FORENOON = 12;
    private static final int AFTERNOON = 18;

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYMD = "yyyy.MM.dd";
    public static final String YYYYMM = "yyyyMM";
    public static final String YYYY_MM = "yyyy-MM";
    public static final String YYMDHMS = "yyyyMMddHHmmss";
    public static final String YY_M_D_H_M_S = "yyyy-MM-dd HH:mm:ss";
    public static final String YY_M_D_H_M = "yyyy-MM-dd HH:mm";
    public static final String TZ = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String MM_DD = "MM-dd";

    public static final String MM_DD2 = "MM月dd日";
    public static final String MM_DD2_HH_MM = "MM月dd日HH:mm";
    public static final String YYYY_MM_DD2 = "yyyy年MM月dd日";
    public static final String YY_M_D_H_M2 = "yyyy年MM月dd日HH:mm";
    public static final String YY_M_D_H_M_S2 = "yyyy年MM月dd日 HH:mm:ss";

    public static final String HH_MM_12 = "KK:mm";
    public static final String HH_MM_24 = "HH:mm";
    public static final String HH_MM_SS = "HH:mm:ss";

    private static String currentFormat = YYYY_MM_DD;
    private static SimpleDateFormat FORMAT = (SimpleDateFormat) DateFormat.getDateInstance();
    private static Date DATE = new Date();

    /**
     * 格式化日期时间
     *
     * @param milliseconds
     * @param format
     * @return
     */
    public static String format(long milliseconds, String format) {
        DATE.setTime(milliseconds);
        return format(DATE, format);
    }

    /**
     * 格式化日期时间
     *
     * @param date
     * @param format
     * @return
     */
    public static String format(Date date, String format) {
        if (!currentFormat.equals(format)) {
            currentFormat = format;
            FORMAT.applyPattern(format);
        }
        return FORMAT.format(date);
    }

    /**
     * 解析日期格式
     *
     * @param date
     * @param format
     * @return
     */
    public static Date parseDate(String date, String format) {
        Date tempDate = null;
        try {
            if (!currentFormat.equals(format)) {
                currentFormat = format;
                FORMAT.applyPattern(format);
            }
            tempDate = FORMAT.parse(date);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (tempDate == null) {
            tempDate = new Date();
            tempDate.setTime(0);
        }
        return tempDate;
    }

    public static long getTime(int year, int month, int day, int hour, int min) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month % 12, day, hour, min, 0);
        return calendar.getTimeInMillis();
    }
}
