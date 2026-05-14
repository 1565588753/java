package com.internetcafe.controller;

import com.internetcafe.dao.AdminDao;
import com.internetcafe.dao.SystemLogDao;
import com.internetcafe.entity.Admin;
import com.internetcafe.entity.SystemLog;
import com.internetcafe.util.DateUtil;
import com.internetcafe.util.PasswordUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录控制器
 * 负责处理管理员的登录、登出以及会话状态查询等认证相关请求。
 * 基于Token的会话管理机制，使用UUID生成唯一令牌，
 * 会话数据存储在BaseController的sessionMap中，实现无状态服务的会话保持。
 *
 * 路由映射：
 *   POST   /api/login   —— 管理员登录
 *   POST   /api/logout  —— 管理员登出
 *   GET    /api/session —— 检查当前会话状态
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class LoginController extends BaseController implements HttpHandler {

    /** 管理员数据访问对象，用于查询管理员账号信息 */
    private AdminDao adminDao;

    /** 系统日志数据访问对象，用于记录登录/登出操作日志 */
    private SystemLogDao systemLogDao;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000;

    private ConcurrentHashMap<String, Integer> loginFailCount = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Long> accountLockTime = new ConcurrentHashMap<>();

    /**
     * 构造方法 —— 初始化DAO对象
     */
    public LoginController() {
        this.adminDao = new AdminDao();
        this.systemLogDao = new SystemLogDao();
    }

    /**
     * HTTP请求处理方法 —— 根据请求方法和路径分发到对应的处理方法
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当处理请求发生I/O错误时抛出
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        /* 设置CORS响应头，允许跨域访问 */
        setCorsHeaders(exchange);

        /* 处理OPTIONS预检请求 */
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        /* 获取请求方法和路径 */
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("POST".equals(method) && "/api/login".equals(path)) {
                handleLogin(exchange);
            } else if ("POST".equals(method) && "/api/logout".equals(path)) {
                handleLogout(exchange);
            } else if ("GET".equals(method) && "/api/session".equals(path)) {
                handleSession(exchange);
            } else {
                sendError(exchange, 404, "接口不存在: " + method + " " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 处理管理员登录请求 —— POST /api/login
     * 请求体JSON格式：{"username": "admin", "password": "123456"}
     * 成功响应：{"token": "uuid字符串", "admin": {...}}
     * 失败响应：{"code": 401, "message": "用户名或密码错误"}
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当读取请求体或发送响应时发生I/O错误
     */
    private void handleLogin(HttpExchange exchange) throws IOException {
        /* 解析请求体中的JSON数据，提取用户名和密码 */
        @SuppressWarnings("unchecked")
        Map<String, String> params = parseBody(exchange, Map.class);

        String username = params.get("username");
        String password = params.get("password");

        /* 校验用户名和密码是否为空 */
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            sendError(exchange, 400, "用户名和密码不能为空");
            return;
        }

        username = username.trim();

        Long lockTime = accountLockTime.get(username);
        if (lockTime != null) {
            long remainingLock = lockTime - System.currentTimeMillis();
            if (remainingLock > 0) {
                long remainingMinutes = remainingLock / 60000 + 1;
                sendError(exchange, 429, "账户已被锁定，请" + remainingMinutes + "分钟后再试");
                return;
            } else {
                accountLockTime.remove(username);
                loginFailCount.remove(username);
            }
        }

        Admin admin = adminDao.findByUsername(username);
        if (admin == null) {
            recordLoginFailure(username);
            sendError(exchange, 401, "用户名或密码错误");
            return;
        }

        boolean passwordValid = PasswordUtil.verify(password, admin.getPassword());
        if (!passwordValid) {
            recordLoginFailure(username);
            sendError(exchange, 401, "用户名或密码错误");
            return;
        }

        loginFailCount.remove(username);
        accountLockTime.remove(username);

        /* 生成唯一的会话令牌 */
        String token = UUID.randomUUID().toString().replace("-", "");

        /* 将令牌和管理员对象存入会话Map */
        sessionMap.put(token, admin);

        /* 记录登录日志 */
        SystemLog loginLog = new SystemLog();
        loginLog.setOperatorName(admin.getUsername());
        loginLog.setOperationType("登录");
        loginLog.setOperationContent("管理员 [" + admin.getUsername() + "] 通过Web端登录系统，角色：" + admin.getRole());
        loginLog.setCreateTime(DateUtil.getCurrentDateTime());
        systemLogDao.insert(loginLog);

        /* 构建响应数据：令牌 + 管理员信息（不包含密码） */
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);

        /* 构建不包含密码的管理员信息 */
        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("id", admin.getId());
        adminInfo.put("username", admin.getUsername());
        adminInfo.put("role", admin.getRole());
        adminInfo.put("createTime", admin.getCreateTime());
        response.put("admin", adminInfo);

        /* 设置Cookie响应头，让浏览器自动保存token */
        exchange.getResponseHeaders().set("Set-Cookie",
                "token=" + token + "; Path=/; HttpOnly; Max-Age=86400");

        sendJson(exchange, response);

        System.out.println("管理员 [" + admin.getUsername() + "] 通过Web端登录成功，token=" + token.substring(0, 8) + "...");
    }

    /**
     * 处理管理员登出请求 —— POST /api/logout
     * 从会话Map中移除当前token对应的会话记录
     * 请求头需携带 Authorization: Bearer <token> 或 Cookie: token=<token>
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleLogout(HttpExchange exchange) throws IOException {
        /* 获取当前会话的管理员信息 */
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "未登录或会话已过期");
            return;
        }

        /* 获取当前请求的token */
        String token = getTokenFromRequest(exchange);
        if (token != null) {
            /* 从会话Map中移除该token */
            sessionMap.remove(token);
        }

        /* 记录登出日志 */
        SystemLog logoutLog = new SystemLog();
        logoutLog.setOperatorName(admin.getUsername());
        logoutLog.setOperationType("登出");
        logoutLog.setOperationContent("管理员 [" + admin.getUsername() + "] 通过Web端退出系统");
        logoutLog.setCreateTime(DateUtil.getCurrentDateTime());
        systemLogDao.insert(logoutLog);

        /* 清除浏览器Cookie中的token */
        exchange.getResponseHeaders().set("Set-Cookie",
                "token=; Path=/; HttpOnly; Max-Age=0");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");

        sendJson(exchange, response);

        System.out.println("管理员 [" + admin.getUsername() + "] 通过Web端登出成功");
    }

    /**
     * 检查当前会话状态 —— GET /api/session
     * 用于前端页面加载时验证用户是否已登录
     * 如果已登录则返回管理员信息，否则返回未登录状态
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleSession(HttpExchange exchange) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            /* 未登录状态 */
            Map<String, Object> response = new HashMap<>();
            response.put("loggedIn", false);
            sendJson(exchange, response);
            return;
        }

        /* 已登录状态 —— 返回管理员信息（不包含密码） */
        Map<String, Object> response = new HashMap<>();
        response.put("loggedIn", true);

        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("id", admin.getId());
        adminInfo.put("username", admin.getUsername());
        adminInfo.put("role", admin.getRole());
        adminInfo.put("createTime", admin.getCreateTime());
        response.put("admin", adminInfo);

        sendJson(exchange, response);
    }

    /**
     * 从HTTP请求中提取token字符串（辅助方法）
     * 与BaseController.getSession()逻辑相同，但直接返回token字符串
     *
     * @param exchange HTTP交换对象
     * @return token字符串，如果未找到则返回null
     */
    private String getTokenFromRequest(HttpExchange exchange) {
        /* 尝试从Cookie中获取 */
        java.util.List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");
        if (cookieHeaders != null) {
            for (String cookieHeader : cookieHeaders) {
                for (String cookieStr : cookieHeader.split(";")) {
                    cookieStr = cookieStr.trim();
                    if (cookieStr.startsWith("token=")) {
                        return cookieStr.substring("token=".length());
                    }
                }
            }
        }

        /* 尝试从Authorization头中获取 */
        java.util.List<String> authHeaders = exchange.getRequestHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring("Bearer ".length()).trim();
            }
        }

        return null;
    }

    private void recordLoginFailure(String username) {
        int count = loginFailCount.getOrDefault(username, 0) + 1;
        loginFailCount.put(username, count);

        if (count >= MAX_LOGIN_ATTEMPTS) {
            accountLockTime.put(username, System.currentTimeMillis() + LOCK_DURATION_MS);
            SystemLog errorLog = new SystemLog();
            errorLog.setOperatorName(username);
            errorLog.setOperationType("错误");
            errorLog.setOperationContent("账户 [" + username + "] 因连续" + MAX_LOGIN_ATTEMPTS + "次登录失败已被锁定15分钟");
            errorLog.setCreateTime(DateUtil.getCurrentDateTime());
            systemLogDao.insert(errorLog);
            System.err.println("安全警告：账户 [" + username + "] 已被锁定15分钟（连续" + MAX_LOGIN_ATTEMPTS + "次登录失败）");
        }
    }
}