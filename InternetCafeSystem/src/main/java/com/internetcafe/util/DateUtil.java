package com.internetcafe.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期时间工具类
 * 提供日期格式化、解析等常用方法
 */
public class DateUtil {

    /** 标准日期时间格式 */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** 标准日期格式 */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 获取当前日期时间字符串
     *
     * @return 格式为 yyyy-MM-dd HH:mm:ss 的当前时间字符串
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
        return sdf.format(new Date());
    }

    /**
     * 获取当前日期字符串
     *
     * @return 格式为 yyyy-MM-dd 的当前日期字符串
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(new Date());
    }

    /**
     * 将字符串解析为Date对象
     *
     * @param dateStr 日期字符串，格式为 yyyy-MM-dd HH:mm:ss
     * @return Date对象，解析失败返回null
     */
    public static Date parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 格式化Date对象为字符串
     *
     * @param date Date对象
     * @return 格式为 yyyy-MM-dd HH:mm:ss 的字符串
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
        return sdf.format(date);
    }

    /**
     * 计算两个日期时间字符串之间的分钟差
     *
     * @param startTime 开始时间字符串
     * @param endTime 结束时间字符串
     * @return 分钟差，计算失败返回0
     */
    public static long getMinutesBetween(String startTime, String endTime) {
        if (startTime == null || endTime == null || startTime.isEmpty() || endTime.isEmpty()) {
            return 0;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            long diff = end.getTime() - start.getTime();
            return diff / (1000 * 60);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}