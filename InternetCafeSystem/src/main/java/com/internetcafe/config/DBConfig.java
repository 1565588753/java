package com.internetcafe.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 数据库配置类
 * 负责从 application.properties 文件中读取数据库相关的配置信息，
 * 并以静态常量的方式提供给整个应用程序使用。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class DBConfig {

    /** 数据库连接URL */
    private static String url;

    /** 数据库用户名 */
    private static String username;

    /** 数据库密码 */
    private static String password;

    /** 数据库驱动类名 */
    private static String driver;

    /** 连接池初始化时创建的连接数 */
    private static int initialSize;

    /** 连接池中最小空闲连接数 */
    private static int minIdle;

    /** 连接池中最大活跃连接数 */
    private static int maxActive;

    /** 获取连接时的最大等待时间（毫秒） */
    private static long maxWait;

    /** 检测空闲连接的间隔时间（毫秒） */
    private static long timeBetweenEvictionRunsMillis;

    /** 连接在池中最小空闲时间（毫秒），达到此值后可能被回收 */
    private static long minEvictableIdleTimeMillis;

    /** 验证连接是否有效的SQL语句 */
    private static String validationQuery;

    /** 是否在连接空闲时检测连接有效性 */
    private static boolean testWhileIdle;

    /** 是否在从连接池获取连接时检测连接有效性 */
    private static boolean testOnBorrow;

    /** 是否在归还连接到连接池时检测连接有效性 */
    private static boolean testOnReturn;

    /** 是否启用预编译语句缓存 */
    private static boolean poolPreparedStatements;

    /** 每个连接上预编译语句缓存的最大数量 */
    private static int maxPoolPreparedStatementPerConnectionSize;

    /** 配置属性文件名 */
    private static final String CONFIG_FILE = "application.properties";

    /** Druid连接池监控过滤器 */
    private static String filters;

    static {
        loadConfig();
    }

    /**
     * 从 classpath 下的 application.properties 文件中加载数据库配置
     * 如果加载失败，将使用默认配置值，确保程序不会因为配置文件缺失而崩溃
     */
    private static void loadConfig() {
        Properties props = new Properties();
        try (InputStream input = DBConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
            } else {
                System.err.println("警告: 未找到配置文件 " + CONFIG_FILE + "，将使用默认配置");
            }
        } catch (IOException e) {
            System.err.println("警告: 加载配置文件 " + CONFIG_FILE + " 时发生异常: " + e.getMessage());
        }

        // 从配置文件中读取各属性值，若未配置则使用默认值
        url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/internet_cafe?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8");
        username = props.getProperty("db.username", "root");
        password = props.getProperty("db.password", "root");
        driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        // 连接池参数配置
        initialSize = Integer.parseInt(props.getProperty("db.initialSize", "5"));
        minIdle = Integer.parseInt(props.getProperty("db.minIdle", "5"));
        maxActive = Integer.parseInt(props.getProperty("db.maxActive", "20"));
        maxWait = Long.parseLong(props.getProperty("db.maxWait", "60000"));
        timeBetweenEvictionRunsMillis = Long.parseLong(props.getProperty("db.timeBetweenEvictionRunsMillis", "60000"));
        minEvictableIdleTimeMillis = Long.parseLong(props.getProperty("db.minEvictableIdleTimeMillis", "300000"));
        validationQuery = props.getProperty("db.validationQuery", "SELECT 1");
        testWhileIdle = Boolean.parseBoolean(props.getProperty("db.testWhileIdle", "true"));
        testOnBorrow = Boolean.parseBoolean(props.getProperty("db.testOnBorrow", "false"));
        testOnReturn = Boolean.parseBoolean(props.getProperty("db.testOnReturn", "false"));
        poolPreparedStatements = Boolean.parseBoolean(props.getProperty("db.poolPreparedStatements", "true"));
        maxPoolPreparedStatementPerConnectionSize = Integer.parseInt(props.getProperty("db.maxPoolPreparedStatementPerConnectionSize", "20"));
        filters = props.getProperty("db.filters", "stat,wall");
    }

    /**
     * 获取数据库连接URL
     *
     * @return 数据库连接URL字符串
     */
    public static String getUrl() {
        return url;
    }

    /**
     * 获取数据库用户名
     *
     * @return 数据库用户名
     */
    public static String getUsername() {
        return username;
    }

    /**
     * 获取数据库密码
     *
     * @return 数据库密码
     */
    public static String getPassword() {
        return password;
    }

    /**
     * 获取数据库驱动类名
     *
     * @return 驱动类的全限定名
     */
    public static String getDriver() {
        return driver;
    }

    /**
     * 获取连接池初始化连接数
     *
     * @return 初始化时创建的连接数量
     */
    public static int getInitialSize() {
        return initialSize;
    }

    /**
     * 获取连接池最小空闲连接数
     *
     * @return 最小空闲连接数
     */
    public static int getMinIdle() {
        return minIdle;
    }

    /**
     * 获取连接池最大活跃连接数
     *
     * @return 最大活跃连接数
     */
    public static int getMaxActive() {
        return maxActive;
    }

    /**
     * 获取获取连接时的最大等待时间
     *
     * @return 最大等待时间（毫秒）
     */
    public static long getMaxWait() {
        return maxWait;
    }

    /**
     * 获取空闲连接检测间隔时间
     *
     * @return 检测间隔（毫秒）
     */
    public static long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    /**
     * 获取连接在池中最小空闲时间
     *
     * @return 最小空闲时间（毫秒）
     */
    public static long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    /**
     * 获取验证连接有效性的SQL语句
     *
     * @return 验证SQL语句
     */
    public static String getValidationQuery() {
        return validationQuery;
    }

    /**
     * 获取是否在空闲时检测连接有效性
     *
     * @return true表示检测，false表示不检测
     */
    public static boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    /**
     * 获取是否在获取连接时检测连接有效性
     *
     * @return true表示检测，false表示不检测
     */
    public static boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    /**
     * 获取是否在归还连接时检测连接有效性
     *
     * @return true表示检测，false表示不检测
     */
    public static boolean isTestOnReturn() {
        return testOnReturn;
    }

    /**
     * 获取是否启用预编译语句缓存
     *
     * @return true表示启用，false表示不启用
     */
    public static boolean isPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    /**
     * 获取每个连接上预编译语句缓存的最大数量
     *
     * @return 预编译语句缓存最大数量
     */
    public static int getMaxPoolPreparedStatementPerConnectionSize() {
        return maxPoolPreparedStatementPerConnectionSize;
    }

    /**
     * 获取Druid监控过滤器配置
     *
     * @return 过滤器配置字符串
     */
    public static String getFilters() {
        return filters;
    }
}