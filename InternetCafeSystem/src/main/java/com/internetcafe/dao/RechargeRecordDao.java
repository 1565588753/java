package com.internetcafe.dao;

import com.internetcafe.entity.RechargeRecord;
import com.internetcafe.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 充值记录数据访问对象
 * 负责 recharge_record 表的所有数据库操作，
 * 提供充值记录的增删改查以及按时间段统计分析功能。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class RechargeRecordDao {

    /**
     * 新增充值记录
     * 向数据库插入一条新的充值记录，并返回数据库自动生成的主键ID。
     *
     * @param record 要插入的充值记录对象（id字段会被自动填充）
     * @return 数据库自动生成的主键ID，插入失败返回-1
     */
    public Integer insert(RechargeRecord record) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return -1;
            }

            // 构建插入SQL语句，使用预编译防止SQL注入
            String sql = "INSERT INTO recharge_record (user_id, amount, recharge_time, operator_name) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // 设置预编译参数
            pstmt.setInt(1, record.getUserId());
            pstmt.setBigDecimal(2, record.getAmount());
            pstmt.setString(3, record.getRechargeTime());
            pstmt.setString(4, record.getOperatorName());

            // 执行插入操作
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // 获取数据库自动生成的主键ID
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    record.setId(generatedId);
                    return generatedId;
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("错误：新增充值记录失败: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    /**
     * 查询所有充值记录
     * 通过与用户表（user）进行左连接，获取用户名信息用于前端展示。
     * 结果按充值时间降序排列，最新的记录在前。
     *
     * @return 包含所有充值记录的列表，查询失败返回空列表
     */
    public List<RechargeRecord> findAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<RechargeRecord> list = new ArrayList<>();
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return list;
            }

            // 构建查询SQL，左连接用户表获取用户名
            String sql = "SELECT r.id, r.user_id, u.username, r.amount, r.recharge_time, r.operator_name " +
                         "FROM recharge_record r " +
                         "LEFT JOIN user u ON r.user_id = u.id " +
                         "ORDER BY r.recharge_time DESC";
            pstmt = conn.prepareStatement(sql);

            // 执行查询
            rs = pstmt.executeQuery();

            // 遍历结果集，将每条记录封装为RechargeRecord对象
            while (rs.next()) {
                RechargeRecord record = new RechargeRecord();
                record.setId(rs.getInt("id"));
                record.setUserId(rs.getInt("user_id"));
                record.setUsername(rs.getString("username"));
                record.setAmount(rs.getBigDecimal("amount"));
                record.setRechargeTime(rs.getString("recharge_time"));
                record.setOperatorName(rs.getString("operator_name"));
                list.add(record);
            }
        } catch (SQLException e) {
            System.err.println("错误：查询所有充值记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据用户ID查询该用户的所有充值记录
     * 通过与用户表（user）进行左连接，获取用户名信息。
     * 结果按充值时间降序排列。
     *
     * @param userId 用户ID
     * @return 该用户的所有充值记录列表，查询失败返回空列表
     */
    public List<RechargeRecord> findByUserId(Integer userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<RechargeRecord> list = new ArrayList<>();
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return list;
            }

            // 构建查询SQL，左连接用户表获取用户名
            String sql = "SELECT r.id, r.user_id, u.username, r.amount, r.recharge_time, r.operator_name " +
                         "FROM recharge_record r " +
                         "LEFT JOIN user u ON r.user_id = u.id " +
                         "WHERE r.user_id = ? " +
                         "ORDER BY r.recharge_time DESC";
            pstmt = conn.prepareStatement(sql);

            // 设置预编译参数
            pstmt.setInt(1, userId);

            // 执行查询
            rs = pstmt.executeQuery();

            // 遍历结果集，将每条记录封装为RechargeRecord对象
            while (rs.next()) {
                RechargeRecord record = new RechargeRecord();
                record.setId(rs.getInt("id"));
                record.setUserId(rs.getInt("user_id"));
                record.setUsername(rs.getString("username"));
                record.setAmount(rs.getBigDecimal("amount"));
                record.setRechargeTime(rs.getString("recharge_time"));
                record.setOperatorName(rs.getString("operator_name"));
                list.add(record);
            }
        } catch (SQLException e) {
            System.err.println("错误：根据用户ID查询充值记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return list;
    }

    public List<RechargeRecord> findByUserId(Integer userId, int limit) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<RechargeRecord> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return list;
            }
            String sql = "SELECT r.id, r.user_id, u.username, r.amount, r.recharge_time, r.operator_name " +
                         "FROM recharge_record r " +
                         "LEFT JOIN user u ON r.user_id = u.id " +
                         "WHERE r.user_id = ? " +
                         "ORDER BY r.recharge_time DESC LIMIT ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                RechargeRecord record = new RechargeRecord();
                record.setId(rs.getInt("id"));
                record.setUserId(rs.getInt("user_id"));
                record.setUsername(rs.getString("username"));
                record.setAmount(rs.getBigDecimal("amount"));
                record.setRechargeTime(rs.getString("recharge_time"));
                record.setOperatorName(rs.getString("operator_name"));
                list.add(record);
            }
        } catch (SQLException e) {
            System.err.println("错误：根据用户ID查询充值记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 统计今日充值总金额
     * 查询今天（以服务器当前日期为准）所有充值记录的金额总和。
     * 用于每日营业数据统计。
     *
     * @return 今日充值总金额，查询失败或今日无充值记录返回 BigDecimal.ZERO
     */
    public BigDecimal getTodayTotal() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return BigDecimal.ZERO;
            }

            // 构建统计SQL，使用DATE函数比较日期部分，对今日充值金额求和
            String sql = "SELECT SUM(amount) AS total FROM recharge_record WHERE DATE(recharge_time) = CURDATE()";
            pstmt = conn.prepareStatement(sql);

            // 执行查询
            rs = pstmt.executeQuery();

            // 获取统计结果，如果SUM结果为NULL（今日无充值记录），返回BigDecimal.ZERO
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("错误：统计今日充值总金额失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 统计本月充值总金额
     * 查询当前月份（以服务器当前日期为准）所有充值记录的金额总和。
     * 用于月度营业数据汇总统计。
     *
     * @return 本月充值总金额，查询失败或本月无充值记录返回 BigDecimal.ZERO
     */
    public BigDecimal getMonthTotal() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return BigDecimal.ZERO;
            }

            // 构建统计SQL，使用DATE_FORMAT函数匹配当前月份
            String sql = "SELECT SUM(amount) AS total FROM recharge_record " +
                         "WHERE DATE_FORMAT(recharge_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')";
            pstmt = conn.prepareStatement(sql);

            // 执行查询
            rs = pstmt.executeQuery();

            // 获取统计结果
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("错误：统计本月充值总金额失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 统计指定日期范围内的充值总金额
     * 查询从开始日期到结束日期之间（含起止日期）所有充值记录的金额总和。
     * 用于自定义时间段报表统计。
     *
     * @param startDate 开始日期（格式：yyyy-MM-dd），包含该日期
     * @param endDate   结束日期（格式：yyyy-MM-dd），包含该日期
     * @return 指定日期范围内的充值总金额，查询失败或无记录返回 BigDecimal.ZERO
     */
    public BigDecimal getTotalByDateRange(String startDate, String endDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return BigDecimal.ZERO;
            }

            // 构建统计SQL，使用BETWEEN进行日期范围筛选，使用预编译防止SQL注入
            String sql = "SELECT SUM(amount) AS total FROM recharge_record " +
                         "WHERE DATE(recharge_time) BETWEEN ? AND ?";
            pstmt = conn.prepareStatement(sql);

            // 设置预编译参数
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);

            // 执行查询
            rs = pstmt.executeQuery();

            // 获取统计结果
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            System.err.println("错误：统计日期范围内充值总金额失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return BigDecimal.ZERO;
    }
}