package com.internetcafe.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码加密工具类
 * 使用SHA-256算法对密码进行加密存储
 */
public class PasswordUtil {

    /**
     * 使用SHA-256算法对密码进行加密
     *
     * @param password 明文密码
     * @return 加密后的密文（十六进制字符串）
     */
    public static String encrypt(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 校验密码是否匹配
     *
     * @param inputPassword 输入的明文密码
     * @param storedPassword 数据库中存储的密文
     * @return 密码匹配返回true，否则返回false
     */
    public static boolean verify(String inputPassword, String storedPassword) {
        if (inputPassword == null || storedPassword == null) {
            return false;
        }
        String encrypted = encrypt(inputPassword);
        return encrypted.equals(storedPassword);
    }
}