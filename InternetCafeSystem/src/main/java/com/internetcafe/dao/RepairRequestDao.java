package com.internetcafe.dao;

import com.internetcafe.entity.RepairRequest;
import com.internetcafe.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepairRequestDao {

    public Integer insert(RepairRequest request) {
        String sql = "INSERT INTO repair_request (user_id, username, machine_no, description, status, create_time) VALUES (?, ?, ?, ?, 0, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return -1;
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, request.getUserId());
            pstmt.setString(2, request.getUsername());
            pstmt.setString(3, request.getMachineNo());
            pstmt.setString(4, request.getDescription());
            pstmt.setString(5, request.getCreateTime());
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    public List<RepairRequest> findAll() {
        List<RepairRequest> list = new ArrayList<>();
        String sql = "SELECT r.id, r.user_id, r.username, r.machine_no, r.description, r.status, r.create_time, r.handle_time " +
                "FROM repair_request r ORDER BY r.id DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return list;
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return list;
    }

    public List<RepairRequest> findByStatus(Integer status) {
        List<RepairRequest> list = new ArrayList<>();
        String sql = "SELECT r.id, r.user_id, r.username, r.machine_no, r.description, r.status, r.create_time, r.handle_time " +
                "FROM repair_request r WHERE r.status = ? ORDER BY r.id DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return list;
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return list;
    }

    public List<RepairRequest> findByUserId(Integer userId) {
        List<RepairRequest> list = new ArrayList<>();
        String sql = "SELECT r.id, r.user_id, r.username, r.machine_no, r.description, r.status, r.create_time, r.handle_time " +
                "FROM repair_request r WHERE r.user_id = ? ORDER BY r.id DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return list;
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return list;
    }

    public RepairRequest findById(Integer id) {
        String sql = "SELECT r.id, r.user_id, r.username, r.machine_no, r.description, r.status, r.create_time, r.handle_time " +
                "FROM repair_request r WHERE r.id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return null;
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    public boolean updateStatus(Integer id, Integer status, String handleTime) {
        String sql = "UPDATE repair_request SET status = ?, handle_time = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return false;
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            pstmt.setString(2, handleTime);
            pstmt.setInt(3, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeAll(conn, pstmt, null);
        }
    }

    public int countPending() {
        String sql = "SELECT COUNT(*) FROM repair_request WHERE status = 0";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) return 0;
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    private RepairRequest mapRow(ResultSet rs) throws SQLException {
        RepairRequest r = new RepairRequest();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setUsername(rs.getString("username"));
        r.setMachineNo(rs.getString("machine_no"));
        r.setDescription(rs.getString("description"));
        r.setStatus(rs.getInt("status"));
        r.setCreateTime(rs.getString("create_time"));
        r.setHandleTime(rs.getString("handle_time"));
        return r;
    }
}
