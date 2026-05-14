package com.internetcafe.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.internetcafe.config.DBConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库工具类
 * 基于阿里巴巴 Druid 连接池实现的数据库操作工具类。
 * 提供获取数据库连接、安全关闭资源、测试数据库连通性等核心功能。
 * 通过静态代码块在类加载时初始化 Druid 连接池，确保全局只有一个连接池实例。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class DBUtil {

    /** Druid数据库连接池实例，整个应用程序共享同一个连接池 */
    private static DruidDataSource dataSource;

    /** 标记连接池是否已成功初始化 */
    private static boolean initialized = false;

    /**
     * 静态代码块：在类加载时初始化Druid连接池
     * 从DBConfig中读取配置参数，配置连接池的各项属性
     */
    static {
        try {
            // 创建Druid连接池实例
            dataSource = new DruidDataSource();

            // 配置基本连接信息
            dataSource.setUrl(DBConfig.getUrl());
            dataSource.setUsername(DBConfig.getUsername());
            dataSource.setPassword(DBConfig.getPassword());
            dataSource.setDriverClassName(DBConfig.getDriver());

            // 配置连接池大小参数
            dataSource.setInitialSize(DBConfig.getInitialSize());
            dataSource.setMinIdle(DBConfig.getMinIdle());
            dataSource.setMaxActive(DBConfig.getMaxActive());
            dataSource.setMaxWait(DBConfig.getMaxWait());

            // 配置连接有效性检测参数，防止连接因长时间空闲而被数据库服务端断开
            dataSource.setTimeBetweenEvictionRunsMillis(DBConfig.getTimeBetweenEvictionRunsMillis());
            dataSource.setMinEvictableIdleTimeMillis(DBConfig.getMinEvictableIdleTimeMillis());
            dataSource.setValidationQuery(DBConfig.getValidationQuery());
            dataSource.setTestWhileIdle(DBConfig.isTestWhileIdle());
            dataSource.setTestOnBorrow(DBConfig.isTestOnBorrow());
            dataSource.setTestOnReturn(DBConfig.isTestOnReturn());

            // 配置预编译语句缓存，提升SQL执行性能
            dataSource.setPoolPreparedStatements(DBConfig.isPoolPreparedStatements());
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(DBConfig.getMaxPoolPreparedStatementPerConnectionSize());

            // 配置Druid监控过滤器，stat用于SQL监控统计，wall用于SQL防火墙防御SQL注入
            try {
                dataSource.setFilters(DBConfig.getFilters());
            } catch (SQLException e) {
                System.err.println("警告: 设置Druid过滤器失败: " + e.getMessage());
            }

            initialized = true;
            System.out.println("Druid连接池初始化成功！");
        } catch (Exception e) {
            initialized = false;
            System.err.println("严重错误: Druid连接池初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从连接池中获取一个数据库连接
     * 每次调用都会从 Druid 连接池中借出一个可用连接，
     * 使用完毕后务必调用 closeAll 方法归还连接。
     *
     * @return 数据库连接对象，如果连接池未初始化或获取失败则返回null
     */
    public static Connection getConnection() {
        if (!initialized || dataSource == null) {
            System.err.println("错误: 连接池尚未初始化，无法获取数据库连接！");
            return null;
        }
        try {
            Connection conn = dataSource.getConnection();
            return conn;
        } catch (SQLException e) {
            System.err.println("错误: 从连接池获取数据库连接失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 安全关闭数据库相关资源
     * 按照 ResultSet -> Statement -> Connection 的顺序依次关闭，
     * 每个资源的关闭操作独立进行，互不影响。
     * 注意：通过Druid连接池获取的Connection在调用close()时，
     * 实际上是将连接归还到连接池中，并非真正关闭物理连接。
     *
     * @param connection 数据库连接对象，可为null
     * @param statement  数据库语句对象，可为null
     * @param resultSet  数据库结果集对象，可为null
     */
    public static void closeAll(Connection connection, Statement statement, ResultSet resultSet) {
        // 关闭ResultSet
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.err.println("错误: 关闭ResultSet时发生异常: " + e.getMessage());
            }
        }

        // 关闭Statement
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println("错误: 关闭Statement时发生异常: " + e.getMessage());
            }
        }

        // 关闭Connection（归还到Druid连接池）
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("错误: 关闭Connection时发生异常: " + e.getMessage());
            }
        }
    }

    /**
     * 安全关闭数据库相关资源（重载方法，适用于无ResultSet的场景）
     *
     * @param connection 数据库连接对象，可为null
     * @param statement  数据库语句对象，可为null
     */
    public static void closeAll(Connection connection, Statement statement) {
        closeAll(connection, statement, null);
    }

    /**
     * 测试数据库连接是否正常
     * 尝试从连接池获取一个连接并执行验证查询，
     * 用于检测数据库服务是否可用。
     *
     * @return true表示数据库连接正常，false表示连接失败
     */
    public static boolean testConnection() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            if (conn == null) {
                System.err.println("测试失败: 无法获取数据库连接");
                return false;
            }
            // 执行验证查询，确认连接有效
            stmt = conn.createStatement();
            rs = stmt.executeQuery(DBConfig.getValidationQuery());
            if (rs.next()) {
                System.out.println("数据库连接测试成功！");
                return true;
            }
            System.err.println("测试失败: 验证查询未返回预期结果");
            return false;
        } catch (SQLException e) {
            System.err.println("测试失败: 数据库连接异常: " + e.getMessage());
            return false;
        } finally {
            // 无论测试成功与否，都要确保资源被正确关闭
            closeAll(conn, stmt, rs);
        }
    }

    /**
     * 获取Druid连接池实例
     * 可用于获取连接池的监控数据，如活跃连接数、池大小等
     *
     * @return DruidDataSource实例，如果未初始化则返回null
     */
    public static DruidDataSource getDataSource() {
        return dataSource;
    }

    /**
     * 检查连接池是否已成功初始化
     *
     * @return true表示已初始化，false表示未初始化或初始化失败
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * 关闭Druid连接池，释放所有数据库连接资源
     * 通常在应用程序关闭时调用，确保所有资源被正确释放
     */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            initialized = false;
            System.out.println("Druid连接池已关闭，所有资源已释放。");
        }
    }
}