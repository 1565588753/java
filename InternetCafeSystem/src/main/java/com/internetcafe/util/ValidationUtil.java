package com.internetcafe.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * 数据校验工具类
 * 提供身份证、手机号、金额等格式校验方法
 */
public class ValidationUtil {

    /** 手机号正则：1开头的11位数字 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 身份证号正则：18位 */
    private static final Pattern ID_CARD_PATTERN =
            Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");

    /**
     * 校验手机号格式
     *
     * @param phone 手机号
     * @return 格式正确返回true
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 校验身份证号格式（18位）
     *
     * @param idCard 身份证号
     * @return 格式正确返回true
     */
    public static boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return false;
        }
        return ID_CARD_PATTERN.matcher(idCard).matches();
    }

    /**
     * 校验金额是否合法（不能为负数）
     *
     * @param amount 金额
     * @return 金额合法返回true
     */
    public static boolean isValidAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 校验字符串是否为空
     *
     * @param str 待校验字符串
     * @return 为空返回true
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 校验字符串是否非空
     *
     * @param str 待校验字符串
     * @return 非空返回true
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}