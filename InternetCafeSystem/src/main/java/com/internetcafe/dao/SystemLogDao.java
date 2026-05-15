package com.internetcafe.dao;

import com.internetcafe.entity.SystemLog;
import com.internetcafe.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统日志数据访问对象
 * 负责 system_log 表的所有数据库操作，包括日志记录、查询及清理功能。
 * 所有SQL操作均使用 PreparedStatement 防止SQL注入，
 * 资源释放统一通过 DBUtil.closeAll() 方法完成。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class SystemLogDao {

    /**
     * 新增一条系统日志
     * 向 system_log 表中插入一条新的操作日志记录。
     *
     * @param log 要插入的系统日志对象，需包含 operatorName、operationType、operationContent、createTime 字段
     * @return 插入成功返回 true，失败返回 false
     */
    public boolean insert(SystemLog log) {
        String sql = "INSERT INTO system_log (operator_name, operation_type, operation_content, create_time) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 插入系统日志时无法获取数据库连接！");
                return false;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, log.getOperatorName());
            pstmt.setString(2, log.getOperationType());
            pstmt.setString(3, log.getOperationContent());
            pstmt.setString(4, log.getCreateTime());

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("错误: 插入系统日志失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 查询所有系统日志
     * 获取系统中全部操作日志记录，按创建时间降序排列，最新的日志在前。
     *
     * @return 包含所有系统日志的 List 集合，查询失败返回空列表
     */
    public List<SystemLog> findAll() {
        String sql = "SELECT id, operator_name, operation_type, operation_content, create_time " +
                     "FROM system_log ORDER BY create_time DESC";

        List<SystemLog> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 查询所有系统日志时无法获取数据库连接！");
                return list;
            }

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                SystemLog log = new SystemLog();
                log.setId(rs.getInt("id"));
                log.setOperatorName(rs.getString("operator_name"));
                log.setOperationType(rs.getString("operation_type"));
                log.setOperationContent(rs.getString("operation_content"));
                log.setCreateTime(rs.getString("create_time"));
                list.add(log);
            }

        } catch (SQLException e) {
            System.err.println("错误: 查询所有系统日志失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return list;
    }

    /**
     * 根据操作人名称查询系统日志
     * 查询指定操作人员产生的所有操作日志记录，按时间降序排列。
     *
     * @param operatorName 操作人名称
     * @return 该操作人的所有日志记录列表，查询失败返回空列表
     */
    public List<SystemLog> findByOperator(String operatorName) {
        String sql = "SELECT id, operator_name, operation_type, operation_content, create_time " +
                     "FROM system_log WHERE operator_name = ? ORDER BY create_time DESC";

        List<SystemLog> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 按操作人查询系统日志时无法获取数据库连接！");
                return list;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, operatorName);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                SystemLog log = new SystemLog();
                log.setId(rs.getInt("id"));
                log.setOperatorName(rs.getString("operator_name"));
                log.setOperationType(rs.getString("operation_type"));
                log.setOperationContent(rs.getString("operation_content"));
                log.setCreateTime(rs.getString("create_time"));
                list.add(log);
            }

        } catch (SQLException e) {
            System.err.println("错误: 按操作人查询系统日志失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return list;
    }

    /**
     * 根据操作类型查询系统日志
     * 查询指定操作类型的所有日志记录，用于分类查看操作历史。
     * 例如查询所有"登录"、"新增用户"、"修改会员"等类型的操作记录。
     *
     * @param operationType 操作类型字符串
     * @return 该操作类型的所有日志记录列表，查询失败返回空列表
     */
    public List<SystemLog> findByType(String operationType) {
        String sql = "SELECT id, operator_name, operation_type, operation_content, create_time " +
                     "FROM system_log WHERE operation_type = ? ORDER BY create_time DESC";

        List<SystemLog> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 按操作类型查询系统日志时无法获取数据库连接！");
                return list;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, operationType);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                SystemLog log = new SystemLog();
                log.setId(rs.getInt("id"));
                log.setOperatorName(rs.getString("operator_name"));
                log.setOperationType(rs.getString("operation_type"));
                log.setOperationContent(rs.getString("operation_content"));
                log.setCreateTime(rs.getString("create_time"));
                list.add(log);
            }

        } catch (SQLException e) {
            System.err.println("错误: 按操作类型查询系统日志失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return list;
    }

    /**
     * 根据日期范围查询系统日志
     * 查询指定开始日期和结束日期之间产生的所有操作日志。
     * 用于按时间段检索操作记录，方便审计和排查问题。
     *
     * @param startDate 开始日期字符串，格式为 yyyy-MM-dd
     * @param endDate   结束日期字符串，格式为 yyyy-MM-dd
     * @return 日期范围内的所有日志记录列表，查询失败返回空列表
     */
    public List<SystemLog> findByDateRange(String startDate, String endDate) {
        String sql = "SELECT id, operator_name, operation_type, operation_content, create_time " +
                     "FROM system_log WHERE create_time >= ? AND create_time <= ? ORDER BY create_time DESC";

        List<SystemLog> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 按日期范围查询系统日志时无法获取数据库连接！");
                return list;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, startDate + " 00:00:00");
            pstmt.setString(2, endDate + " 23:59:59");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                SystemLog log = new SystemLog();
                log.setId(rs.getInt("id"));
                log.setOperatorName(rs.getString("operator_name"));
                log.setOperationType(rs.getString("operation_type"));
                log.setOperationContent(rs.getString("operation_content"));
                log.setCreateTime(rs.getString("create_time"));
                list.add(log);
            }

        } catch (SQLException e) {
            System.err.println("错误: 按日期范围查询系统日志失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return list;
    }

    /**
     * 删除指定日期之前的系统日志（日志清理）
     * 用于定期清理过期日志数据，防止日志表数据量过大影响系统性能。
     * 删除 create_time 早于指定日期的所有日志记录。
     *
     * @param date 截止日期字符串，格式为 yyyy-MM-dd，删除该日期之前（不含当天）的所有日志
     * @return 被删除的记录条数，删除失败返回 0
     */
    public int deleteBefore(String date) {
        String sql = "DELETE FROM system_log WHERE create_time < ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 清理系统日志时无法获取数据库连接！");
                return 0;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, date + " 00:00:00");

            int rows = pstmt.executeUpdate();
            System.out.println("系统日志清理完成，共删除 " + rows + " 条记录。");
            return rows;

        } catch (SQLException e) {
            System.err.println("错误: 清理系统日志失败: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }
}