package com.internetcafe.dao;

import com.internetcafe.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SystemConfigDao {

    public Map<String, String> getAllConfigs() {
        Map<String, String> configs = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return configs;
            }
            String sql = "SELECT config_key, config_value FROM system_config";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                configs.put(rs.getString("config_key"), rs.getString("config_value"));
            }
        } catch (SQLException e) {
            System.err.println("查询系统配置失败: " + e.getMessage());
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return configs;
    }

    public String getConfig(String key) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return null;
            }
            String sql = "SELECT config_value FROM system_config WHERE config_key = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, key);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("config_value");
            }
        } catch (SQLException e) {
            System.err.println("查询系统配置(" + key + ")失败: " + e.getMessage());
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }

    public boolean updateConfig(String key, String value) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return false;
            }
            String sql = "INSERT INTO system_config (config_key, config_value) VALUES (?, ?) "
                    + "ON DUPLICATE KEY UPDATE config_value = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.setString(3, value);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新系统配置(" + key + ")失败: " + e.getMessage());
            return false;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }
}