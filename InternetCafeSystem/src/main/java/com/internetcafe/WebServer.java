package com.internetcafe;

import com.internetcafe.controller.ChargeController;
import com.internetcafe.controller.LogController;
import com.internetcafe.controller.LoginController;
import com.internetcafe.controller.ReportController;
import com.internetcafe.controller.UserController;
import com.internetcafe.controller.VipController;
import com.internetcafe.service.ChargeService;
import com.internetcafe.util.DBUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

/**
 * 网吧计费管理系统 —— Web版主启动类
 *
 * 基于JDK内置的 com.sun.net.httpserver.HttpServer 实现的轻量级Web服务器。
 * 启动后监听8080端口，提供RESTful API接口和静态文件服务。
 *
 * 功能特性：
 *   - 使用JDK内置HTTP服务器，无需引入第三方Web框架
 *   - 提供完整的RESTful API接口（登录、用户管理、计费、会员、报表、日志）
 *   - 支持静态文件服务（HTML/CSS/JS前端页面）
 *   - 基于Token的会话管理机制
 *   - CORS跨域支持
 *   - 启动时自动初始化数据库连接池
 *   - 关闭时自动释放数据库资源
 *
 * API接口一览：
 *   认证相关：
 *     POST   /api/login         —— 管理员登录
 *     POST   /api/logout        —— 管理员登出
 *     GET    /api/session       —— 检查会话状态
 *   用户管理：
 *     GET    /api/users         —— 分页查询用户列表
 *     GET    /api/users/{id}    —— 查询单个用户
 *     POST   /api/users         —— 新增用户
 *     PUT    /api/users/{id}    —— 更新用户
 *     DELETE /api/users/{id}    —— 删除用户
 *     POST   /api/users/recharge—— 用户充值
 *   计费管理：
 *     POST   /api/charge/start  —— 开始上机
 *     POST   /api/charge/stop/{id}—— 停止上机（下机结算）
 *     GET    /api/charge/active —— 活跃上机记录
 *     GET    /api/charge/history—— 历史记录
 *     GET    /api/charge/records/{id}—— 用户上机记录
 *   会员管理：
 *     GET    /api/vip/levels    —— 会员等级列表
 *     PUT    /api/vip/levels/{id}—— 修改折扣率
 *     POST   /api/vip/upgrade   —— 升级会员等级
 *     GET    /api/vip/stats     —— 会员分布统计
 *   报表统计：
 *     GET    /api/report/summary—— 概览统计
 *     GET    /api/report/ranking—— 消费排行榜
 *     GET    /api/report/monthly—— 月度营收统计
 *   日志管理：
 *     GET    /api/logs          —— 查询日志
 *     DELETE /api/logs/clean    —— 清理日志
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class WebServer {

    /** Web服务器监听端口 */
    private static final int PORT = 8080;

    /** 静态文件资源根目录（相对于项目根目录） */
    private static final String WEB_ROOT = "src/main/resources/web";

    /**
     * 程序主入口
     * 执行数据库初始化、启动HTTP服务器、注册所有API路由和静态文件处理器，
     * 并注册JVM关闭钩子以确保资源正确释放。
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("  网吧计费管理系统 - Web版 启动中...");
        System.out.println("============================================");

        /* 第一步：初始化数据库连接池 */
        System.out.println("[1/3] 正在初始化数据库连接池...");
        if (!DBUtil.isInitialized()) {
            System.err.println("数据库连接池初始化失败！请检查数据库配置。");
            System.err.println("请确保：");
            System.err.println("  1. MySQL服务已启动");
            System.err.println("  2. application.properties 配置正确");
            System.err.println("  3. 数据库已创建并导入初始化SQL脚本");
            System.exit(1);
        }

        /* 第二步：测试数据库连接是否正常 */
        System.out.println("[2/3] 正在测试数据库连接...");
        if (!DBUtil.testConnection()) {
            System.err.println("数据库连接测试失败！");
            System.exit(1);
        }
        System.out.println("数据库连接测试成功！");

        /* 第三步：启动HTTP服务器并注册路由 */
        try {
            System.out.println("[3/3] 正在启动HTTP服务器（端口：" + PORT + "）...");

            /* 创建HTTP服务器实例，绑定到指定端口 */
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            /* 使用线程池处理并发请求，提高服务器吞吐量 */
            server.setExecutor(Executors.newFixedThreadPool(10));

            /* 注册认证相关路由 */
            LoginController loginController = new LoginController();
            server.createContext("/api/login", loginController);
            server.createContext("/api/logout", loginController);
            server.createContext("/api/session", loginController);
            server.createContext("/api/user", loginController);

            /* 注册用户管理路由 */
            server.createContext("/api/users", new UserController());

            /* 注册计费管理路由 */
            server.createContext("/api/charge", new ChargeController());

            /* 注册会员管理路由 */
            server.createContext("/api/vip", new VipController());

            /* 注册报表统计路由 */
            server.createContext("/api/report", new ReportController());

            /* 注册日志管理路由 */
            server.createContext("/api/logs", new LogController());

            /* 注册静态文件服务 —— 处理所有非API路径的请求，提供前端页面 */
            server.createContext("/", new StaticFileHandler());

            /* 启动HTTP服务器，开始接收请求 */
            server.start();

            System.out.println("============================================");
            System.out.println("  Web服务器启动成功！");
            System.out.println("  访问地址：http://localhost:" + PORT + "/");
            System.out.println("  API接口：http://localhost:" + PORT + "/api/");
            System.out.println("============================================");

            ChargeService chargeService = new ChargeService();
            int recovered = chargeService.recoverInterruptedSessions();
            if (recovered > 0) {
                System.out.println("断点续计：已恢复 " + recovered + " 条上机记录");
            }

            Timer autoStopTimer = new Timer("AutoStopBalanceTimer", true);
            autoStopTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        chargeService.autoStopLowBalance();
                    } catch (Exception e) {
                        System.err.println("自动余额检查异常：" + e.getMessage());
                    }
                }
            }, 5000, ChargeService.TIMER_INTERVAL);
            System.out.println("余额自动监控已启动（扫描间隔：" + ChargeService.TIMER_INTERVAL + "ms）");

            Timer backupTimer = new Timer("DatabaseBackupTimer", true);
            backupTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        String backupFile = DBUtil.backupDatabase();
                        if (backupFile != null) {
                            System.out.println("数据库自动备份完成：" + backupFile);
                        }
                    } catch (Exception e) {
                        System.err.println("数据库自动备份异常：" + e.getMessage());
                    }
                }
            }, 60000, 3600000);
            System.out.println("数据库自动备份已启动（间隔：1小时）");

            /* 注册JVM关闭钩子 —— 确保程序退出时释放数据库连接池资源 */
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("\n系统正在关闭...");
                    /* 停止HTTP服务器，不再接收新请求 */
                    server.stop(2);
                    System.out.println("HTTP服务器已停止。");

                    /* 关闭数据库连接池，释放所有连接 */
                    DBUtil.shutdown();
                    System.out.println("数据库连接池已关闭。");
                    System.out.println("系统已安全退出。");
                }
            }));

        } catch (IOException e) {
            System.err.println("Web服务器启动失败: " + e.getMessage());
            e.printStackTrace();
            /* 服务器启动失败时也需释放数据库资源 */
            DBUtil.shutdown();
            System.exit(1);
        }
    }

    /**
     * 静态文件处理器
     * 负责处理非API路径的HTTP请求，从 src/main/resources/web/ 目录中读取静态文件
     * 并返回给客户端。支持HTML、CSS、JavaScript、图片等常见静态资源类型。
     *
     * 请求处理逻辑：
     *   1. 如果请求路径为 "/" 或 "/index.html"，返回首页 index.html
     *   2. 其他路径直接映射到 web 目录下的对应文件
     *   3. 如果文件不存在，返回404错误
     *
     * MIME类型映射支持：
     *   .html / .htm  → text/html
     *   .css          → text/css
     *   .js           → application/javascript
     *   .json         → application/json
     *   .png          → image/png
     *   .jpg / .jpeg  → image/jpeg
     *   .gif          → image/gif
     *   .svg          → image/svg+xml
     *   .ico          → image/x-icon
     *   .woff         → font/woff
     *   .woff2        → font/woff2
     *   .ttf          → font/ttf
     *
     * @author InternetCafeSystem
     * @version 1.0
     */
    static class StaticFileHandler implements HttpHandler {

        /** MIME类型映射表 —— 根据文件扩展名确定Content-Type响应头 */
        private static final Map<String, String> MIME_TYPES = new HashMap<>();

        static {
            /* 文本类型 */
            MIME_TYPES.put("html", "text/html; charset=UTF-8");
            MIME_TYPES.put("htm", "text/html; charset=UTF-8");
            MIME_TYPES.put("css", "text/css; charset=UTF-8");
            MIME_TYPES.put("js", "application/javascript; charset=UTF-8");
            MIME_TYPES.put("json", "application/json; charset=UTF-8");
            MIME_TYPES.put("xml", "application/xml; charset=UTF-8");

            /* 图片类型 */
            MIME_TYPES.put("png", "image/png");
            MIME_TYPES.put("jpg", "image/jpeg");
            MIME_TYPES.put("jpeg", "image/jpeg");
            MIME_TYPES.put("gif", "image/gif");
            MIME_TYPES.put("svg", "image/svg+xml");
            MIME_TYPES.put("ico", "image/x-icon");
            MIME_TYPES.put("bmp", "image/bmp");
            MIME_TYPES.put("webp", "image/webp");

            /* 字体类型 */
            MIME_TYPES.put("woff", "font/woff");
            MIME_TYPES.put("woff2", "font/woff2");
            MIME_TYPES.put("ttf", "font/ttf");
            MIME_TYPES.put("eot", "application/vnd.ms-fontobject");
            MIME_TYPES.put("otf", "font/otf");

            /* 其他常见类型 */
            MIME_TYPES.put("pdf", "application/pdf");
            MIME_TYPES.put("zip", "application/zip");
            MIME_TYPES.put("mp3", "audio/mpeg");
            MIME_TYPES.put("mp4", "video/mp4");
            MIME_TYPES.put("txt", "text/plain; charset=UTF-8");
        }

        /**
         * 处理静态文件请求
         * 根据请求路径在web目录中查找对应文件，读取文件内容并返回给客户端
         *
         * @param exchange HTTP交换对象
         * @throws IOException 当读取文件或发送响应时发生I/O错误
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            /* 只处理GET请求，其他方法返回405 */
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            /* 获取请求路径，将其映射到web目录下的文件 */
            String requestPath = exchange.getRequestURI().getPath();

            /* 根路径或空路径默认返回index.html */
            if ("/".equals(requestPath)) {
                requestPath = "/index.html";
            }

            /* 安全检查：防止路径遍历攻击（如 ../../etc/passwd） */
            if (requestPath.contains("..")) {
                exchange.sendResponseHeaders(403, -1);
                return;
            }

            /* 构建文件在磁盘上的实际路径 */
            String filePath = WEB_ROOT + requestPath;
            File file = new File(filePath);

            /* 如果请求的是目录，尝试返回目录下的index.html */
            if (file.isDirectory()) {
                file = new File(file, "index.html");
                if (!file.exists()) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
            }

            /* 如果文件不存在，返回404 */
            if (!file.exists() || !file.isFile()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            /* 根据文件扩展名确定MIME类型 */
            String fileName = file.getName();
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                extension = fileName.substring(dotIndex + 1).toLowerCase();
            }
            String mimeType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");

            /* 设置响应头 */
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            /* 设置缓存控制：静态资源缓存1小时 */
            exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600");

            /* 读取文件内容并发送 */
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        }
    }
}