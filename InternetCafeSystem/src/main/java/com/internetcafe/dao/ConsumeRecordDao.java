package com.internetcafe.dao;

import com.internetcafe.entity.ConsumeRecord;
import com.internetcafe.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费记录数据访问对象
 * 负责 consume_record 表的所有数据库操作，包括增删改查及统计分析功能。
 * 所有SQL操作均使用 PreparedStatement 防止SQL注入，
 * 资源释放统一通过 DBUtil.closeAll() 方法完成。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class ConsumeRecordDao {

    /**
     * 新增一条消费记录
     * 向 consume_record 表中插入一条新的消费记录数据。
     *
     * @param record 要插入的消费记录对象，需包含 userId、amount、consumeType 等必要字段
     * @return 插入成功返回 true，失败返回 false
     */
    public boolean insert(ConsumeRecord record) {
        String sql = "INSERT INTO consume_record (user_id, amount, consume_type, create_time) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 插入消费记录时无法获取数据库连接！");
                return false;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, record.getUserId());
            pstmt.setBigDecimal(2, record.getAmount());
            pstmt.setString(3, record.getConsumeType());
            pstmt.setString(4, record.getCreateTime());

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("错误: 插入消费记录失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 查询所有消费记录（关联用户表获取用户名）
     * 通过 LEFT JOIN user 表获取用户名信息，方便前端展示。
     *
     * @return 包含所有消费记录的 List 集合，查询失败返回空列表
     */
    public List<ConsumeRecord> findAll() {
        String sql = "SELECT cr.id, cr.user_id, u.username, cr.amount, cr.consume_type, cr.create_time " +
                     "FROM consume_record cr " +
                     "LEFT JOIN user u ON cr.user_id = u.id " +
                     "ORDER BY cr.create_time DESC";

        List<ConsumeRecord> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 查询所有消费记录时无法获取数据库连接！");
                return list;
            }

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ConsumeRecord record = new ConsumeRecord();
                record.setId(rs.getInt("id"));
                record.setUserId(rs.getInt("user_id"));
                record.setUsername(rs.getString("username"));
                record.setAmount(rs.getBigDecimal("amount"));
                record.setConsumeType(rs.getString("consume_type"));
                record.setCreateTime(rs.getString("create_time"));
                list.add(record);
            }

        } catch (SQLException e) {
            System.err.println("错误: 查询所有消费记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return list;
    }

    /**
     * 根据用户ID查询该用户的消费记录
     * 用于查看指定用户的所有消费历史。
     *
     * @param userId 要查询的用户ID
     * @return 该用户的所有消费记录列表，查询失败返回空列表
     */
    public List<ConsumeRecord> findByUserId(Integer userId) {
        String sql = "SELECT cr.id, cr.user_id, u.username, cr.amount, cr.consume_type, cr.create_time " +
                     "FROM consume_record cr " +
                     "LEFT JOIN user u ON cr.user_id = u.id " +
                     "WHERE cr.user_id = ? " +
                     "ORDER BY cr.create_time DESC";

        List<ConsumeRecord> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 按用户ID查询消费记录时无法获取数据库连接！");
                return list;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ConsumeRecord record = new ConsumeRecord();
                record.setId(rs.getInt("id"));
                record.setUserId(rs.getInt("user_id"));
                record.setUsername(rs.getString("username"));
                record.setAmount(rs.getBigDecimal("amount"));
                record.setConsumeType(rs.getString("consume_type"));
                record.setCreateTime(rs.getString("create_time"));
                list.add(record);
            }

        } catch (SQLException e) {
            System.err.println("错误: 按用户ID查询消费记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return list;
    }

    public List<ConsumeRecord> findByUserId(Integer userId, int limit) {
        String sql = "SELECT cr.id, cr.user_id, u.username, cr.amount, cr.consume_type, cr.create_time " +
                     "FROM consume_record cr " +
                     "LEFT JOIN user u ON cr.user_id = u.id " +
                     "WHERE cr.user_id = ? " +
                     "ORDER BY cr.create_time DESC LIMIT ?";

        List<ConsumeRecord> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 按用户ID查询消费记录时无法获取数据库连接！");
                return list;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ConsumeRecord record = new ConsumeRecord();
                record.setId(rs.getInt("id"));
                record.setUserId(rs.getInt("user_id"));
                record.setUsername(rs.getString("username"));
                record.setAmount(rs.getBigDecimal("amount"));
                record.setConsumeType(rs.getString("consume_type"));
                record.setCreateTime(rs.getString("create_time"));
                list.add(record);
            }

        } catch (SQLException e) {
            System.err.println("错误: 按用户ID查询消费记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return list;
    }

    /**
     * 获取今日消费总额
     * 统计当天所有消费记录的总金额，用于首页概览展示。
     *
     * @return 今日消费总额（BigDecimal），查询失败或无数据返回 BigDecimal.ZERO
     */
    public BigDecimal getTodayTotal() {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM consume_record " +
                     "WHERE DATE(create_time) = CURDATE()";

        BigDecimal total = BigDecimal.ZERO;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 查询今日消费总额时无法获取数据库连接！");
                return total;
            }

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                total = rs.getBigDecimal("total");
            }

        } catch (SQLException e) {
            System.err.println("错误: 查询今日消费总额失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return total;
    }

    /**
     * 获取本月消费总额
     * 统计当前月份所有消费记录的总金额，用于月度统计报表。
     *
     * @return 本月消费总额（BigDecimal），查询失败或无数据返回 BigDecimal.ZERO
     */
    public BigDecimal getMonthTotal() {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM consume_record " +
                     "WHERE YEAR(create_time) = YEAR(CURDATE()) AND MONTH(create_time) = MONTH(CURDATE())";

        BigDecimal total = BigDecimal.ZERO;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 查询本月消费总额时无法获取数据库连接！");
                return total;
            }

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                total = rs.getBigDecimal("total");
            }

        } catch (SQLException e) {
            System.err.println("错误: 查询本月消费总额失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return total;
    }

    /**
     * 获取指定日期范围内的消费总额
     * 根据传入的开始日期和结束日期，统计该时间区间内的所有消费金额。
     *
     * @param startDate 开始日期字符串，格式为 yyyy-MM-dd
     * @param endDate   结束日期字符串，格式为 yyyy-MM-dd
     * @return 日期范围内的消费总额（BigDecimal），查询失败或无数据返回 BigDecimal.ZERO
     */
    public BigDecimal getTotalByDateRange(String startDate, String endDate) {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM consume_record " +
                     "WHERE create_time >= ? AND create_time <= ?";

        BigDecimal total = BigDecimal.ZERO;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 查询日期范围消费总额时无法获取数据库连接！");
                return total;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, startDate + " 00:00:00");
            pstmt.setString(2, endDate + " 23:59:59");
            rs = pstmt.executeQuery();

            if (rs.next()) {
                total = rs.getBigDecimal("total");
            }

        } catch (SQLException e) {
            System.err.println("错误: 查询日期范围消费总额失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return total;
    }

    /**
     * 获取用户消费排行榜
     * 按消费总额降序排列，统计每个用户的消费总金额和消费次数。
     * 用于展示网吧消费排名，辅助运营决策。
     *
     * @return 包含排行榜数据的 List<Map<String,Object>>，每个 Map 包含以下键：
     *         username     - 用户名
     *         totalAmount  - 消费总金额（BigDecimal）
     *         consumeCount - 消费次数（Long）
     */
    public List<Map<String, Object>> getUserConsumeRanking() {
        String sql = "SELECT u.username, SUM(cr.amount) AS total_amount, COUNT(cr.id) AS consume_count " +
                     "FROM consume_record cr " +
                     "LEFT JOIN user u ON cr.user_id = u.id " +
                     "GROUP BY cr.user_id, u.username " +
                     "ORDER BY total_amount DESC";

        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 查询用户消费排行榜时无法获取数据库连接！");
                return list;
            }

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("username", rs.getString("username"));
                map.put("totalAmount", rs.getBigDecimal("total_amount"));
                map.put("consumeCount", rs.getLong("consume_count"));
                list.add(map);
            }

        } catch (SQLException e) {
            System.err.println("错误: 查询用户消费排行榜失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return list;
    }
}