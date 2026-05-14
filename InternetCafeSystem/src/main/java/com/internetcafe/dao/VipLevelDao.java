package com.internetcafe.dao;

import com.internetcafe.entity.VipLevel;
import com.internetcafe.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 会员等级数据访问对象
 * 负责 vip_level 表的所有数据库操作，包括增删改查功能。
 * 所有SQL操作均使用 PreparedStatement 防止SQL注入，
 * 资源释放统一通过 DBUtil.closeAll() 方法完成。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class VipLevelDao {

    /**
     * 查询所有会员等级
     * 获取系统中定义的全部会员等级信息，按ID升序排列。
     *
     * @return 包含所有会员等级的 List 集合，查询失败返回空列表
     */
    public List<VipLevel> findAll() {
        String sql = "SELECT id, level_name, discount_rate, description FROM vip_level ORDER BY id ASC";

        List<VipLevel> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 查询所有会员等级时无法获取数据库连接！");
                return list;
            }

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                VipLevel vipLevel = new VipLevel();
                vipLevel.setId(rs.getInt("id"));
                vipLevel.setLevelName(rs.getString("level_name"));
                vipLevel.setDiscountRate(rs.getBigDecimal("discount_rate"));
                vipLevel.setDescription(rs.getString("description"));
                list.add(vipLevel);
            }

        } catch (SQLException e) {
            System.err.println("错误: 查询所有会员等级失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return list;
    }

    /**
     * 根据ID查询单个会员等级
     * 通过主键ID精确查找对应的会员等级信息。
     *
     * @param id 会员等级ID
     * @return 匹配的 VipLevel 对象，未找到或查询失败返回 null
     */
    public VipLevel findById(Integer id) {
        String sql = "SELECT id, level_name, discount_rate, description FROM vip_level WHERE id = ?";

        VipLevel vipLevel = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 按ID查询会员等级时无法获取数据库连接！");
                return null;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                vipLevel = new VipLevel();
                vipLevel.setId(rs.getInt("id"));
                vipLevel.setLevelName(rs.getString("level_name"));
                vipLevel.setDiscountRate(rs.getBigDecimal("discount_rate"));
                vipLevel.setDescription(rs.getString("description"));
            }

        } catch (SQLException e) {
            System.err.println("错误: 按ID查询会员等级失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return vipLevel;
    }

    /**
     * 新增会员等级
     * 向 vip_level 表中插入一条新的会员等级记录。
     *
     * @param vipLevel 要插入的会员等级对象，需包含 levelName、discountRate、description 字段
     * @return 插入成功返回 true，失败返回 false
     */
    public boolean insert(VipLevel vipLevel) {
        String sql = "INSERT INTO vip_level (level_name, discount_rate, description) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 插入会员等级时无法获取数据库连接！");
                return false;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, vipLevel.getLevelName());
            pstmt.setBigDecimal(2, vipLevel.getDiscountRate());
            pstmt.setString(3, vipLevel.getDescription());

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("错误: 插入会员等级失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 更新会员等级信息
     * 根据会员等级对象中的ID，更新对应记录的所有字段。
     *
     * @param vipLevel 要更新的会员等级对象，必须包含有效的 id 字段
     * @return 更新成功返回 true，失败返回 false
     */
    public boolean update(VipLevel vipLevel) {
        String sql = "UPDATE vip_level SET level_name = ?, discount_rate = ?, description = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 更新会员等级时无法获取数据库连接！");
                return false;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, vipLevel.getLevelName());
            pstmt.setBigDecimal(2, vipLevel.getDiscountRate());
            pstmt.setString(3, vipLevel.getDescription());
            pstmt.setInt(4, vipLevel.getId());

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("错误: 更新会员等级失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 根据ID删除会员等级
     * 从 vip_level 表中删除指定ID对应的会员等级记录。
     * 注意：删除前应确保没有用户关联到此等级，否则可能因外键约束导致删除失败。
     *
     * @param id 要删除的会员等级ID
     * @return 删除成功返回 true，失败返回 false
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM vip_level WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 删除会员等级时无法获取数据库连接！");
                return false;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("错误: 删除会员等级失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 根据等级名称查询会员等级
     * 通过等级名称模糊匹配，查找对应的会员等级信息。
     * 用于判断等级名称是否重复，以及根据名称获取等级详情。
     *
     * @param levelName 会员等级名称
     * @return 匹配的 VipLevel 对象，未找到或查询失败返回 null
     */
    public VipLevel findByLevelName(String levelName) {
        String sql = "SELECT id, level_name, discount_rate, description FROM vip_level WHERE level_name = ?";

        VipLevel vipLevel = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误: 按等级名称查询会员等级时无法获取数据库连接！");
                return null;
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, levelName);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                vipLevel = new VipLevel();
                vipLevel.setId(rs.getInt("id"));
                vipLevel.setLevelName(rs.getString("level_name"));
                vipLevel.setDiscountRate(rs.getBigDecimal("discount_rate"));
                vipLevel.setDescription(rs.getString("description"));
            }

        } catch (SQLException e) {
            System.err.println("错误: 按等级名称查询会员等级失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }

        return vipLevel;
    }
}