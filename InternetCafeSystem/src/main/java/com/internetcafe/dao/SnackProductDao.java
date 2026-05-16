package com.internetcafe.dao;

import com.internetcafe.entity.SnackProduct;
import com.internetcafe.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SnackProductDao {

    public List<SnackProduct> findAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<SnackProduct> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, name, price, image, stock, status, create_time, update_time FROM snack_product WHERE status = 1 ORDER BY id ASC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractProductFromResultSet(rs));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("查询零食商品列表时发生异常: " + e.getMessage());
            return list;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    public List<SnackProduct> findAllIncludeDisabled() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<SnackProduct> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, name, price, image, stock, status, create_time, update_time FROM snack_product ORDER BY id ASC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractProductFromResultSet(rs));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("查询全部零食商品时发生异常: " + e.getMessage());
            return list;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    public SnackProduct findById(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, name, price, image, stock, status, create_time, update_time FROM snack_product WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractProductFromResultSet(rs);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("查询零食商品（id=" + id + "）时发生异常: " + e.getMessage());
            return null;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    public int insert(SnackProduct product) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO snack_product (name, price, image, stock, status) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, product.getName());
            pstmt.setBigDecimal(2, product.getPrice());
            pstmt.setString(3, product.getImage() != null ? product.getImage() : "");
            pstmt.setInt(4, product.getStock() != null ? product.getStock() : 999);
            pstmt.setInt(5, product.getStatus() != null ? product.getStatus() : 1);
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("新增零食商品时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    public int update(SnackProduct product) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE snack_product SET name = ?, price = ?, image = ?, stock = ?, status = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, product.getName());
            pstmt.setBigDecimal(2, product.getPrice());
            pstmt.setString(3, product.getImage() != null ? product.getImage() : "");
            pstmt.setInt(4, product.getStock() != null ? product.getStock() : 999);
            pstmt.setInt(5, product.getStatus() != null ? product.getStatus() : 1);
            pstmt.setInt(6, product.getId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("更新零食商品（id=" + product.getId() + "）时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    public int delete(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM snack_product WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("删除零食商品（id=" + id + "）时发生异常: " + e.getMessage());
            return -1;
        } finally {
            DBUtil.closeAll(conn, pstmt);
        }
    }

    private SnackProduct extractProductFromResultSet(ResultSet rs) throws SQLException {
        SnackProduct product = new SnackProduct();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setImage(rs.getString("image"));
        product.setStock(rs.getInt("stock"));
        product.setStatus(rs.getInt("status"));
        product.setCreateTime(rs.getString("create_time"));
        product.setUpdateTime(rs.getString("update_time"));
        return product;
    }
}