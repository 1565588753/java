package com.internetcafe.dao;

import com.internetcafe.entity.User;
import com.internetcafe.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象
 * 负责 user 表的增删改查操作，所有SQL操作均使用 PreparedStatement 防止SQL注入
 * 查询操作会关联 vip_level 表以获取会员等级名称
 */
public class UserDao {

    /**
     * 新增用户
     * @param user 待添加的用户对象（id和createTime会被忽略，由数据库自动生成）
     * @return 受影响的行数，大于0表示成功，-1表示失败
     */
    public int insert(User user) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO user (username, password, real_name, id_card, phone, balance, vip_level, points, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRealName());
            pstmt.setString(4, user.getIdCard());
            pstmt.setString(5, user.getPhone());
            pstmt.setBigDecimal(6, user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO);
            pstmt.setInt(7, user.getVipLevel() != null ? user.getVipLevel() : 1);
            pstmt.setInt(8, user.getPoints() != null ? user.getPoints() : 0);
            pstmt.setInt(9, user.getStatus() != null ? user.getStatus() : 1);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("新增用户（用户名=" + user.getUsername() + "）时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 更新用户信息
     * 根据用户对象的id更新对应记录的所有可修改字段
     * @param user 待更新的用户对象
     * @return 受影响的行数，大于0表示成功，-1表示失败
     */
    public int update(User user) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE user SET username = ?, password = ?, real_name = ?, id_card = ?, phone = ?, " +
                         "balance = ?, vip_level = ?, points = ?, status = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRealName());
            pstmt.setString(4, user.getIdCard());
            pstmt.setString(5, user.getPhone());
            pstmt.setBigDecimal(6, user.getBalance());
            pstmt.setInt(7, user.getVipLevel() != null ? user.getVipLevel() : 1);
            pstmt.setInt(8, user.getPoints() != null ? user.getPoints() : 0);
            pstmt.setInt(9, user.getStatus() != null ? user.getStatus() : 1);
            pstmt.setInt(10, user.getId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("更新用户（id=" + user.getId() + "）时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 根据主键ID删除用户
     * @param id 用户ID
     * @return 受影响的行数，大于0表示成功，-1表示失败
     */
    public int delete(Integer id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM user WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("删除用户（id=" + id + "）时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 查询所有用户
     * 关联 vip_level 表查询，同时获取会员等级名称
     * @return 包含所有用户的列表，如果没有数据则返回空列表
     */
    public List<User> findAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> userList = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT u.id, u.username, u.password, u.real_name, u.id_card, u.phone, " +
                         "u.balance, u.vip_level, vl.level_name AS vip_level_name, u.points, u.status, u.create_time " +
                         "FROM user u LEFT JOIN vip_level vl ON u.vip_level = vl.id " +
                         "ORDER BY u.id ASC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                userList.add(extractUserFromResultSet(rs));
            }
            return userList;
        } catch (SQLException e) {
            System.err.println("查询所有用户时发生异常: " + e.getMessage());
            return userList;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    /**
     * 根据主键ID查找用户
     * 关联 vip_level 表查询，同时获取会员等级名称
     * @param id 用户ID
     * @return 匹配的User对象，如果未找到则返回null
     */
    public User findById(Integer id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT u.id, u.username, u.password, u.real_name, u.id_card, u.phone, " +
                         "u.balance, u.vip_level, vl.level_name AS vip_level_name, u.points, u.status, u.create_time " +
                         "FROM user u LEFT JOIN vip_level vl ON u.vip_level = vl.id " +
                         "WHERE u.id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("查询用户（id=" + id + "）时发生异常: " + e.getMessage());
            return null;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    /**
     * 根据用户名查找用户
     * 关联 vip_level 表查询，同时获取会员等级名称
     * @param username 用户名
     * @return 匹配的User对象，如果未找到则返回null
     */
    public User findByUsername(String username) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT u.id, u.username, u.password, u.real_name, u.id_card, u.phone, " +
                         "u.balance, u.vip_level, vl.level_name AS vip_level_name, u.points, u.status, u.create_time " +
                         "FROM user u LEFT JOIN vip_level vl ON u.vip_level = vl.id " +
                         "WHERE u.username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("查询用户（用户名=" + username + "）时发生异常: " + e.getMessage());
            return null;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    /**
     * 根据关键词模糊搜索用户
     * 在 username（用户名）、real_name（真实姓名）、phone（手机号）三个字段中进行模糊匹配
     * 关联 vip_level 表查询，同时获取会员等级名称
     * @param keyword 搜索关键词
     * @return 匹配的用户列表，如果没有匹配结果则返回空列表
     */
    public List<User> searchByKeyword(String keyword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> userList = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT u.id, u.username, u.password, u.real_name, u.id_card, u.phone, " +
                         "u.balance, u.vip_level, vl.level_name AS vip_level_name, u.points, u.status, u.create_time " +
                         "FROM user u LEFT JOIN vip_level vl ON u.vip_level = vl.id " +
                         "WHERE u.username LIKE ? OR u.real_name LIKE ? OR u.phone LIKE ? " +
                         "ORDER BY u.id ASC";
            pstmt = conn.prepareStatement(sql);
            String likeKeyword = "%" + keyword + "%";
            pstmt.setString(1, likeKeyword);
            pstmt.setString(2, likeKeyword);
            pstmt.setString(3, likeKeyword);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                userList.add(extractUserFromResultSet(rs));
            }
            return userList;
        } catch (SQLException e) {
            System.err.println("模糊搜索用户（关键词=" + keyword + "）时发生异常: " + e.getMessage());
            return userList;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    /**
     * 分页查询用户（支持关键词搜索）
     * 根据页码和每页大小返回对应页的用户数据，支持按关键词模糊过滤
     * 关联 vip_level 表查询，同时获取会员等级名称
     * @param pageNum  页码，从1开始
     * @param pageSize 每页记录数
     * @param keyword  搜索关键词，如果为null或空字符串则不进行过滤
     * @return 当前页的用户列表，如果没有数据则返回空列表
     */
    public List<User> findByPage(int pageNum, int pageSize, String keyword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> userList = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT u.id, u.username, u.password, u.real_name, u.id_card, u.phone, ");
            sqlBuilder.append("u.balance, u.vip_level, vl.level_name AS vip_level_name, u.points, u.status, u.create_time ");
            sqlBuilder.append("FROM user u LEFT JOIN vip_level vl ON u.vip_level = vl.id ");

            boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
            if (hasKeyword) {
                sqlBuilder.append("WHERE u.username LIKE ? OR u.real_name LIKE ? OR u.phone LIKE ? ");
            }
            sqlBuilder.append("ORDER BY u.id ASC LIMIT ? OFFSET ?");

            pstmt = conn.prepareStatement(sqlBuilder.toString());
            int paramIndex = 1;
            if (hasKeyword) {
                String likeKeyword = "%" + keyword.trim() + "%";
                pstmt.setString(paramIndex++, likeKeyword);
                pstmt.setString(paramIndex++, likeKeyword);
                pstmt.setString(paramIndex++, likeKeyword);
            }
            pstmt.setInt(paramIndex++, pageSize);
            pstmt.setInt(paramIndex, (pageNum - 1) * pageSize);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                userList.add(extractUserFromResultSet(rs));
            }
            return userList;
        } catch (SQLException e) {
            System.err.println("分页查询用户（pageNum=" + pageNum + ", pageSize=" + pageSize + "）时发生异常: " + e.getMessage());
            return userList;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    /**
     * 获取匹配关键词的用户总数
     * 用于分页查询时计算总页数
     * @param keyword 搜索关键词，如果为null或空字符串则统计所有用户
     * @return 符合条件的用户总数，查询失败时返回0
     */
    public int getTotalCount(String keyword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT COUNT(*) FROM user ");

            boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
            if (hasKeyword) {
                sqlBuilder.append("WHERE username LIKE ? OR real_name LIKE ? OR phone LIKE ?");
            }

            pstmt = conn.prepareStatement(sqlBuilder.toString());
            if (hasKeyword) {
                String likeKeyword = "%" + keyword.trim() + "%";
                pstmt.setString(1, likeKeyword);
                pstmt.setString(2, likeKeyword);
                pstmt.setString(3, likeKeyword);
            }
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("查询用户总数（关键词=" + keyword + "）时发生异常: " + e.getMessage());
            return 0;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    /**
     * 更新用户账户余额
     * @param userId 用户ID
     * @param amount 新的余额金额
     * @return 受影响的行数，大于0表示成功，-1表示失败
     */
    public int updateBalance(Integer userId, BigDecimal amount) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE user SET balance = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setBigDecimal(1, amount);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("更新用户余额（userId=" + userId + ", amount=" + amount + "）时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 更新用户会员等级
     * @param userId     用户ID
     * @param vipLevelId 新的会员等级ID
     * @return 受影响的行数，大于0表示成功，-1表示失败
     */
    public int updateVipLevel(Integer userId, Integer vipLevelId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE user SET vip_level = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, vipLevelId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("更新用户会员等级（userId=" + userId + ", vipLevelId=" + vipLevelId + "）时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 更新用户积分
     * @param userId 用户ID
     * @param points 新的积分值
     * @return 受影响的行数，大于0表示成功，-1表示失败
     */
    public int updatePoints(Integer userId, Integer points) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE user SET points = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, points);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("更新用户积分（userId=" + userId + ", points=" + points + "）时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 根据会员等级查询用户列表
     * 关联 vip_level 表查询，同时获取会员等级名称
     * @param vipLevelId 会员等级ID
     * @return 该等级下的用户列表，如果没有数据则返回空列表
     */
    public List<User> getUsersByVipLevel(Integer vipLevelId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> userList = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT u.id, u.username, u.password, u.real_name, u.id_card, u.phone, " +
                         "u.balance, u.vip_level, vl.level_name AS vip_level_name, u.points, u.status, u.create_time " +
                         "FROM user u LEFT JOIN vip_level vl ON u.vip_level = vl.id " +
                         "WHERE u.vip_level = ? " +
                         "ORDER BY u.id ASC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, vipLevelId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                userList.add(extractUserFromResultSet(rs));
            }
            return userList;
        } catch (SQLException e) {
            System.err.println("查询会员等级（vipLevelId=" + vipLevelId + "）下的用户时发生异常: " + e.getMessage());
            return userList;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    /**
     * 从 ResultSet 中提取 User 对象
     * 将数据库查询结果集中的字段值映射到User实体类的属性上
     * @param rs 结果集对象，游标已指向当前行
     * @return 封装好的User对象
     * @throws SQLException 如果从结果集中读取数据时发生数据库访问错误
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRealName(rs.getString("real_name"));
        user.setIdCard(rs.getString("id_card"));
        user.setPhone(rs.getString("phone"));
        user.setBalance(rs.getBigDecimal("balance"));
        user.setVipLevel(rs.getInt("vip_level"));
        user.setVipLevelName(rs.getString("vip_level_name"));
        user.setPoints(rs.getInt("points"));
        user.setStatus(rs.getInt("status"));
        user.setCreateTime(rs.getString("create_time"));
        return user;
    }
}