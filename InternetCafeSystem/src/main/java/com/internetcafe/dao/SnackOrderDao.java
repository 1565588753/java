package com.internetcafe.dao;

import com.internetcafe.entity.SnackOrder;
import com.internetcafe.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SnackOrderDao {

    public List<SnackOrder> findAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<SnackOrder> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT so.id, so.user_id, u.username, so.product_id, so.product_name, " +
                         "so.quantity, so.unit_price, so.total_price, so.status, so.order_time, so.confirm_time " +
                         "FROM snack_order so " +
                         "LEFT JOIN user u ON so.user_id = u.id " +
                         "LEFT JOIN snack_product sp ON so.product_id = sp.id " +
                         "ORDER BY so.order_time DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractOrderFromResultSet(rs));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("查询所有零食订单时发生异常: " + e.getMessage());
            return list;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    public List<SnackOrder> findByUserId(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<SnackOrder> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT so.id, so.user_id, u.username, so.product_id, so.product_name, " +
                         "so.quantity, so.unit_price, so.total_price, so.status, so.order_time, so.confirm_time " +
                         "FROM snack_order so " +
                         "LEFT JOIN user u ON so.user_id = u.id " +
                         "LEFT JOIN snack_product sp ON so.product_id = sp.id " +
                         "WHERE so.user_id = ? " +
                         "ORDER BY so.order_time DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractOrderFromResultSet(rs));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("查询用户零食订单（userId=" + userId + "）时发生异常: " + e.getMessage());
            return list;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    public List<SnackOrder> findPending() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<SnackOrder> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT so.id, so.user_id, u.username, so.product_id, so.product_name, " +
                         "so.quantity, so.unit_price, so.total_price, so.status, so.order_time, so.confirm_time " +
                         "FROM snack_order so " +
                         "LEFT JOIN user u ON so.user_id = u.id " +
                         "LEFT JOIN snack_product sp ON so.product_id = sp.id " +
                         "WHERE so.status = 0 " +
                         "ORDER BY so.order_time ASC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractOrderFromResultSet(rs));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("查询待确认零食订单时发生异常: " + e.getMessage());
            return list;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    public int insert(SnackOrder order) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO snack_order (user_id, product_id, product_name, quantity, unit_price, total_price, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, order.getUserId());
            pstmt.setInt(2, order.getProductId());
            pstmt.setString(3, order.getProductName());
            pstmt.setInt(4, order.getQuantity());
            pstmt.setBigDecimal(5, order.getUnitPrice());
            pstmt.setBigDecimal(6, order.getTotalPrice());
            pstmt.setInt(7, order.getStatus() != null ? order.getStatus() : 0);
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("新增零食订单时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    public int updateStatus(int id, int status, String confirmTime) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            if (confirmTime != null) {
                String sql = "UPDATE snack_order SET status = ?, confirm_time = ? WHERE id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, status);
                pstmt.setString(2, confirmTime);
                pstmt.setInt(3, id);
            } else {
                String sql = "UPDATE snack_order SET status = ? WHERE id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, status);
                pstmt.setInt(2, id);
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("更新零食订单状态（id=" + id + "）时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    public BigDecimal getTodaySnackRevenue() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT COALESCE(SUM(total_price), 0) AS total FROM snack_order " +
                         "WHERE status = 1 AND DATE(confirm_time) = CURDATE()";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            System.err.println("查询今日零食营收时发生异常: " + e.getMessage());
            return BigDecimal.ZERO;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    public BigDecimal getMonthSnackRevenue() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT COALESCE(SUM(total_price), 0) AS total FROM snack_order " +
                         "WHERE status = 1 AND YEAR(confirm_time) = YEAR(CURDATE()) AND MONTH(confirm_time) = MONTH(CURDATE())";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            System.err.println("查询本月零食营收时发生异常: " + e.getMessage());
            return BigDecimal.ZERO;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    private SnackOrder extractOrderFromResultSet(ResultSet rs) throws SQLException {
        SnackOrder order = new SnackOrder();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setUsername(rs.getString("username"));
        order.setProductId(rs.getInt("product_id"));
        order.setProductName(rs.getString("product_name"));
        order.setQuantity(rs.getInt("quantity"));
        order.setUnitPrice(rs.getBigDecimal("unit_price"));
        order.setTotalPrice(rs.getBigDecimal("total_price"));
        order.setStatus(rs.getInt("status"));
        order.setOrderTime(rs.getString("order_time"));
        order.setConfirmTime(rs.getString("confirm_time"));
        return order;
    }
}