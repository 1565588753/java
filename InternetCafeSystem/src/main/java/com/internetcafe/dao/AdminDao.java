package com.internetcafe.dao;

import com.internetcafe.entity.Admin;
import com.internetcafe.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员数据访问对象
 * 负责 admin 表的增删改查操作，所有SQL操作均使用 PreparedStatement 防止SQL注入
 */
public class AdminDao {

    /**
     * 根据用户名查找管理员
     * @param username 管理员用户名
     * @return 匹配的Admin对象，如果未找到则返回null
     */
    public Admin findByUsername(String username) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, username, password, role, create_time FROM admin WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractAdminFromResultSet(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("查询管理员（用户名=" + username + "）时发生异常: " + e.getMessage());
            return null;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    /**
     * 新增管理员
     * @param admin 待添加的管理员对象（id会被忽略，由数据库自增生成）
     * @return 受影响的行数，大于0表示成功，-1表示失败
     */
    public int insert(Admin admin) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO admin (username, password, role) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, admin.getUsername());
            pstmt.setString(2, admin.getPassword());
            pstmt.setString(3, admin.getRole());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("新增管理员（用户名=" + admin.getUsername() + "）时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 更新管理员信息
     * 根据管理员对象的id更新对应记录的 username、password、role
     * @param admin 待更新的管理员对象
     * @return 受影响的行数，大于0表示成功，-1表示失败
     */
    public int update(Admin admin) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE admin SET username = ?, password = ?, role = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, admin.getUsername());
            pstmt.setString(2, admin.getPassword());
            pstmt.setString(3, admin.getRole());
            pstmt.setInt(4, admin.getId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("更新管理员（id=" + admin.getId() + "）时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 根据主键ID删除管理员
     * @param id 管理员ID
     * @return 受影响的行数，大于0表示成功，-1表示失败
     */
    public int delete(Integer id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM admin WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("删除管理员（id=" + id + "）时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 查询所有管理员
     * @return 包含所有管理员的列表，如果没有数据则返回空列表
     */
    public List<Admin> findAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Admin> adminList = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, username, password, role, create_time FROM admin ORDER BY id ASC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                adminList.add(extractAdminFromResultSet(rs));
            }
            return adminList;
        } catch (SQLException e) {
            System.err.println("查询所有管理员时发生异常: " + e.getMessage());
            return adminList;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    /**
     * 根据主键ID查找管理员
     * @param id 管理员ID
     * @return 匹配的Admin对象，如果未找到则返回null
     */
    public Admin findById(Integer id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, username, password, role, create_time FROM admin WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractAdminFromResultSet(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("查询管理员（id=" + id + "）时发生异常: " + e.getMessage());
            return null;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    /**
     * 从 ResultSet 中提取 Admin 对象
     * 将数据库查询结果集中的字段值映射到Admin实体类的属性上
     * @param rs 结果集对象，游标已指向当前行
     * @return 封装好的Admin对象
     * @throws SQLException 如果从结果集中读取数据时发生数据库访问错误
     */
    private Admin extractAdminFromResultSet(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setId(rs.getInt("id"));
        admin.setUsername(rs.getString("username"));
        admin.setPassword(rs.getString("password"));
        admin.setRole(rs.getString("role"));
        admin.setCreateTime(rs.getString("create_time"));
        return admin;
    }
}