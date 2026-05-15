package com.internetcafe.dao;

import com.internetcafe.entity.OnlineRecord;
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
 * 上机记录数据访问对象
 * 负责 online_record 表的所有数据库操作，
 * 提供上机记录的增删改查、按条件查询以及统计分析功能。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class OnlineRecordDao {

    /**
     * 新增上机记录
     * 向数据库插入一条新的上机记录，并返回数据库自动生成的主键ID。
     *
     * @param record 要插入的上机记录对象（id字段会被自动填充）
     * @return 数据库自动生成的主键ID，插入失败返回-1
     */
    public Integer insert(OnlineRecord record) {
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
            String sql = "INSERT INTO online_record (user_id, login_time, logout_time, duration, cost, machine_no, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // 设置预编译参数
            pstmt.setInt(1, record.getUserId());
            pstmt.setString(2, record.getLoginTime());
            pstmt.setString(3, record.getLogoutTime());
            if (record.getDuration() != null) {
                pstmt.setLong(4, record.getDuration());
            } else {
                pstmt.setNull(4, java.sql.Types.BIGINT);
            }
            if (record.getCost() != null) {
                pstmt.setBigDecimal(5, record.getCost());
            } else {
                pstmt.setNull(5, java.sql.Types.DECIMAL);
            }
            pstmt.setString(6, record.getMachineNo());
            pstmt.setInt(7, record.getStatus());

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
            System.err.println("错误：新增上机记录失败: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
    }

    /**
     * 更新上机记录
     * 根据记录中的ID更新对应的上机记录所有字段。
     *
     * @param record 包含更新数据的上机记录对象（id字段用于定位记录）
     * @return true表示更新成功，false表示更新失败
     */
    public boolean update(OnlineRecord record) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return false;
            }

            // 构建更新SQL语句，使用预编译防止SQL注入
            String sql = "UPDATE online_record SET user_id = ?, login_time = ?, logout_time = ?, " +
                         "duration = ?, cost = ?, machine_no = ?, status = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);

            // 设置预编译参数
            pstmt.setInt(1, record.getUserId());
            pstmt.setString(2, record.getLoginTime());
            pstmt.setString(3, record.getLogoutTime());
            if (record.getDuration() != null) {
                pstmt.setLong(4, record.getDuration());
            } else {
                pstmt.setNull(4, java.sql.Types.BIGINT);
            }
            if (record.getCost() != null) {
                pstmt.setBigDecimal(5, record.getCost());
            } else {
                pstmt.setNull(5, java.sql.Types.DECIMAL);
            }
            pstmt.setString(6, record.getMachineNo());
            pstmt.setInt(7, record.getStatus());
            pstmt.setInt(8, record.getId());

            // 执行更新操作
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("错误：更新上机记录失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 根据ID删除上机记录
     *
     * @param id 要删除的上机记录ID
     * @return true表示删除成功，false表示删除失败
     */
    public boolean delete(Integer id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return false;
            }

            // 构建删除SQL语句，使用预编译防止SQL注入
            String sql = "DELETE FROM online_record WHERE id = ?";
            pstmt = conn.prepareStatement(sql);

            // 设置预编译参数
            pstmt.setInt(1, id);

            // 执行删除操作
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("错误：删除上机记录失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 查询所有上机记录
     * 通过与用户表（user）进行左连接，获取用户名信息用于前端展示。
     * 结果按上机时间降序排列，最新的记录在前。
     *
     * @return 包含所有上机记录的列表，查询失败返回空列表
     */
    public List<OnlineRecord> findAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<OnlineRecord> list = new ArrayList<>();
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return list;
            }

            // 构建查询SQL，左连接用户表获取用户名
            String sql = "SELECT o.id, o.user_id, u.username, o.login_time, o.logout_time, " +
                         "o.duration, o.cost, o.machine_no, o.status " +
                         "FROM online_record o " +
                         "LEFT JOIN user u ON o.user_id = u.id " +
                         "ORDER BY o.login_time DESC";
            pstmt = conn.prepareStatement(sql);

            // 执行查询
            rs = pstmt.executeQuery();

            // 遍历结果集，将每条记录封装为OnlineRecord对象
            while (rs.next()) {
                OnlineRecord record = new OnlineRecord();
                record.setId(rs.getInt("id"));
                record.setUserId(rs.getInt("user_id"));
                record.setUsername(rs.getString("username"));
                record.setLoginTime(rs.getString("login_time"));
                record.setLogoutTime(rs.getString("logout_time"));
                long duration = rs.getLong("duration");
                if (!rs.wasNull()) {
                    record.setDuration(duration);
                }
                BigDecimal cost = rs.getBigDecimal("cost");
                if (cost != null) {
                    record.setCost(cost);
                }
                record.setMachineNo(rs.getString("machine_no"));
                record.setStatus(rs.getInt("status"));
                list.add(record);
            }
        } catch (SQLException e) {
            System.err.println("错误：查询所有上机记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据ID查询上机记录
     * 通过与用户表（user）进行左连接，获取用户名信息。
     *
     * @param id 上机记录ID
     * @return 查询到的上机记录对象，未找到返回null
     */
    public OnlineRecord findById(Integer id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return null;
            }

            // 构建查询SQL，左连接用户表获取用户名
            String sql = "SELECT o.id, o.user_id, u.username, o.login_time, o.logout_time, " +
                         "o.duration, o.cost, o.machine_no, o.status " +
                         "FROM online_record o " +
                         "LEFT JOIN user u ON o.user_id = u.id " +
                         "WHERE o.id = ?";
            pstmt = conn.prepareStatement(sql);

            // 设置预编译参数
            pstmt.setInt(1, id);

            // 执行查询
            rs = pstmt.executeQuery();

            // 将查询结果封装为OnlineRecord对象
            if (rs.next()) {
                OnlineRecord record = new OnlineRecord();
                record.setId(rs.getInt("id"));
                record.setUserId(rs.getInt("user_id"));
                record.setUsername(rs.getString("username"));
                record.setLoginTime(rs.getString("login_time"));
                record.setLogoutTime(rs.getString("logout_time"));
                long duration = rs.getLong("duration");
                if (!rs.wasNull()) {
                    record.setDuration(duration);
                }
                BigDecimal cost = rs.getBigDecimal("cost");
                if (cost != null) {
                    record.setCost(cost);
                }
                record.setMachineNo(rs.getString("machine_no"));
                record.setStatus(rs.getInt("status"));
                return record;
            }
        } catch (SQLException e) {
            System.err.println("错误：根据ID查询上机记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 根据用户ID查询该用户的所有上机记录
     * 通过与用户表（user）进行左连接，获取用户名信息。
     * 结果按上机时间降序排列。
     *
     * @param userId 用户ID
     * @return 该用户的所有上机记录列表，查询失败返回空列表
     */
    public List<OnlineRecord> findByUserId(Integer userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<OnlineRecord> list = new ArrayList<>();
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return list;
            }

            // 构建查询SQL，左连接用户表获取用户名
            String sql = "SELECT o.id, o.user_id, u.username, o.login_time, o.logout_time, " +
                         "o.duration, o.cost, o.machine_no, o.status " +
                         "FROM online_record o " +
                         "LEFT JOIN user u ON o.user_id = u.id " +
                         "WHERE o.user_id = ? " +
                         "ORDER BY o.login_time DESC";
            pstmt = conn.prepareStatement(sql);

            // 设置预编译参数
            pstmt.setInt(1, userId);

            // 执行查询
            rs = pstmt.executeQuery();

            // 遍历结果集，将每条记录封装为OnlineRecord对象
            while (rs.next()) {
                OnlineRecord record = new OnlineRecord();
                record.setId(rs.getInt("id"));
                record.setUserId(rs.getInt("user_id"));
                record.setUsername(rs.getString("username"));
                record.setLoginTime(rs.getString("login_time"));
                record.setLogoutTime(rs.getString("logout_time"));
                long duration = rs.getLong("duration");
                if (!rs.wasNull()) {
                    record.setDuration(duration);
                }
                BigDecimal cost = rs.getBigDecimal("cost");
                if (cost != null) {
                    record.setCost(cost);
                }
                record.setMachineNo(rs.getString("machine_no"));
                record.setStatus(rs.getInt("status"));
                list.add(record);
            }
        } catch (SQLException e) {
            System.err.println("错误：根据用户ID查询上机记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 根据状态查询上机记录
     * 状态值：1=上机中，2=已下机，3=异常下机。
     * 通过与用户表（user）进行左连接，获取用户名信息。
     * 结果按上机时间降序排列。
     *
     * @param status 上机状态（1=上机中, 2=已下机, 3=异常下机）
     * @return 符合状态条件的所有上机记录列表，查询失败返回空列表
     */
    public List<OnlineRecord> findByStatus(Integer status) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<OnlineRecord> list = new ArrayList<>();
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return list;
            }

            // 构建查询SQL，左连接用户表获取用户名
            String sql = "SELECT o.id, o.user_id, u.username, o.login_time, o.logout_time, " +
                         "o.duration, o.cost, o.machine_no, o.status " +
                         "FROM online_record o " +
                         "LEFT JOIN user u ON o.user_id = u.id " +
                         "WHERE o.status = ? " +
                         "ORDER BY o.login_time DESC";
            pstmt = conn.prepareStatement(sql);

            // 设置预编译参数
            pstmt.setInt(1, status);

            // 执行查询
            rs = pstmt.executeQuery();

            // 遍历结果集，将每条记录封装为OnlineRecord对象
            while (rs.next()) {
                OnlineRecord record = new OnlineRecord();
                record.setId(rs.getInt("id"));
                record.setUserId(rs.getInt("user_id"));
                record.setUsername(rs.getString("username"));
                record.setLoginTime(rs.getString("login_time"));
                record.setLogoutTime(rs.getString("logout_time"));
                long duration = rs.getLong("duration");
                if (!rs.wasNull()) {
                    record.setDuration(duration);
                }
                BigDecimal cost = rs.getBigDecimal("cost");
                if (cost != null) {
                    record.setCost(cost);
                }
                record.setMachineNo(rs.getString("machine_no"));
                record.setStatus(rs.getInt("status"));
                list.add(record);
            }
        } catch (SQLException e) {
            System.err.println("错误：根据状态查询上机记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 查询指定用户的活跃（正在上机）记录
     * 一个用户同时只能有一条活跃的上机记录（status=1）。
     * 通过与用户表（user）进行左连接，获取用户名信息。
     *
     * @param userId 用户ID
     * @return 该用户的活跃上机记录对象，如果没有活跃记录则返回null
     */
    public OnlineRecord findActiveByUserId(Integer userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return null;
            }

            // 构建查询SQL，查询状态为1（上机中）且属于指定用户的记录
            String sql = "SELECT o.id, o.user_id, u.username, o.login_time, o.logout_time, " +
                         "o.duration, o.cost, o.machine_no, o.status " +
                         "FROM online_record o " +
                         "LEFT JOIN user u ON o.user_id = u.id " +
                         "WHERE o.user_id = ? AND o.status = 1 " +
                         "LIMIT 1";
            pstmt = conn.prepareStatement(sql);

            // 设置预编译参数
            pstmt.setInt(1, userId);

            // 执行查询
            rs = pstmt.executeQuery();

            // 将查询结果封装为OnlineRecord对象
            if (rs.next()) {
                OnlineRecord record = new OnlineRecord();
                record.setId(rs.getInt("id"));
                record.setUserId(rs.getInt("user_id"));
                record.setUsername(rs.getString("username"));
                record.setLoginTime(rs.getString("login_time"));
                record.setLogoutTime(rs.getString("logout_time"));
                long duration = rs.getLong("duration");
                if (!rs.wasNull()) {
                    record.setDuration(duration);
                }
                BigDecimal cost = rs.getBigDecimal("cost");
                if (cost != null) {
                    record.setCost(cost);
                }
                record.setMachineNo(rs.getString("machine_no"));
                record.setStatus(rs.getInt("status"));
                return record;
            }
        } catch (SQLException e) {
            System.err.println("错误：查询用户活跃上机记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 根据机器编号查询该机器上的活跃（正在上机）记录
     * 一台机器同时只能有一条活跃的上机记录（status=1）。
     * 通过与用户表（user）进行左连接，获取用户名信息。
     *
     * @param machineNo 机器编号
     * @return 该机器的活跃上机记录对象，如果没有活跃记录则返回null
     */
    public OnlineRecord findActiveByMachineNo(String machineNo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return null;
            }

            // 构建查询SQL，查询状态为1（上机中）且属于指定机器的记录
            String sql = "SELECT o.id, o.user_id, u.username, o.login_time, o.logout_time, " +
                         "o.duration, o.cost, o.machine_no, o.status " +
                         "FROM online_record o " +
                         "LEFT JOIN user u ON o.user_id = u.id " +
                         "WHERE o.machine_no = ? AND o.status = 1 " +
                         "LIMIT 1";
            pstmt = conn.prepareStatement(sql);

            // 设置预编译参数
            pstmt.setString(1, machineNo);

            // 执行查询
            rs = pstmt.executeQuery();

            // 将查询结果封装为OnlineRecord对象
            if (rs.next()) {
                OnlineRecord record = new OnlineRecord();
                record.setId(rs.getInt("id"));
                record.setUserId(rs.getInt("user_id"));
                record.setUsername(rs.getString("username"));
                record.setLoginTime(rs.getString("login_time"));
                record.setLogoutTime(rs.getString("logout_time"));
                long duration = rs.getLong("duration");
                if (!rs.wasNull()) {
                    record.setDuration(duration);
                }
                BigDecimal cost = rs.getBigDecimal("cost");
                if (cost != null) {
                    record.setCost(cost);
                }
                record.setMachineNo(rs.getString("machine_no"));
                record.setStatus(rs.getInt("status"));
                return record;
            }
        } catch (SQLException e) {
            System.err.println("错误：根据机器编号查询活跃上机记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 用户下机时更新上机记录
     * 将下机时间、上机时长、消费金额和状态（改为已下机）更新到指定记录中。
     * 此操作用于用户正常下机结算场景。
     *
     * @param id         上机记录ID
     * @param logoutTime 下机时间（格式：yyyy-MM-dd HH:mm:ss）
     * @param duration   上机时长（单位：分钟）
     * @param cost       消费金额
     * @return true表示更新成功，false表示更新失败
     */
    public boolean stopOnline(Integer id, String logoutTime, Long duration, BigDecimal cost) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return false;
            }

            // 构建更新SQL，将记录状态改为已下机（status=2），并记录下机信息
            String sql = "UPDATE online_record SET logout_time = ?, duration = ?, cost = ?, status = 2 WHERE id = ?";
            pstmt = conn.prepareStatement(sql);

            // 设置预编译参数
            pstmt.setString(1, logoutTime);
            pstmt.setLong(2, duration);
            pstmt.setBigDecimal(3, cost);
            pstmt.setInt(4, id);

            // 执行更新操作
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("错误：更新下机记录失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt);
        }
    }

    /**
     * 统计今日上机次数
     * 查询今天（以服务器当前日期为准）所有上机记录的总条数。
     * 用于统计每日网吧上机人次。
     *
     * @return 今日上机总次数，查询失败返回0
     */
    public int getTodayOnlineCount() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            conn = DBUtil.getConnection();
            if (conn == null) {
                System.err.println("错误：获取数据库连接失败！");
                return 0;
            }

            // 构建统计SQL，使用DATE函数比较日期部分，统计今日上机记录数
            String sql = "SELECT COUNT(*) AS count FROM online_record WHERE DATE(login_time) = CURDATE()";
            pstmt = conn.prepareStatement(sql);

            // 执行查询
            rs = pstmt.executeQuery();

            // 获取统计结果
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("错误：统计今日上机次数失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 安全关闭所有数据库资源，归还连接到连接池
            DBUtil.closeAll(conn, pstmt, rs);
        }
        return 0;
    }
}