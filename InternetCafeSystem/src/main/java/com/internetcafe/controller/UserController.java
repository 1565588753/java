package com.internetcafe.controller;

import com.internetcafe.entity.Admin;
import com.internetcafe.entity.User;
import com.internetcafe.service.LogService;
import com.internetcafe.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 负责处理网吧会员用户的增删改查、分页搜索、账户充值等Web API请求。
 * 所有涉及数据修改的操作（新增、更新、删除、充值）均需要管理员登录后才能执行，
 * 查询操作无需登录验证。
 *
 * 路由映射：
 *   GET    /api/users            —— 分页查询用户列表
 *   GET    /api/users/{id}       —— 根据ID查询单个用户
 *   POST   /api/users            —— 新增用户（需登录）
 *   PUT    /api/users/{id}       —— 更新用户信息（需登录）
 *   DELETE /api/users/{id}       —— 删除用户（需登录）
 *   POST   /api/users/recharge   —— 用户账户充值（需登录）
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class UserController extends BaseController implements HttpHandler {

    /** 用户服务对象，封装用户相关的所有业务逻辑 */
    private UserService userService;

    /** 日志服务对象，用于记录用户操作日志 */
    private LogService logService;

    /**
     * 构造方法 —— 初始化服务对象
     */
    public UserController() {
        this.userService = new UserService();
        this.logService = new LogService();
    }

    /**
     * HTTP请求处理方法 —— 根据请求方法和路径分发到对应的处理方法
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当处理请求发生I/O错误时抛出
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        /* 设置CORS响应头 */
        setCorsHeaders(exchange);

        /* 处理OPTIONS预检请求 */
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            /* POST /api/users/recharge —— 充值操作（需要与 /api/users 区分开） */
            if ("POST".equals(method) && "/api/users/recharge".equals(path)) {
                handleRecharge(exchange);
                return;
            }

            /* GET /api/users —— 分页查询用户列表 */
            if ("GET".equals(method) && "/api/users".equals(path)) {
                handleList(exchange);
                return;
            }

            /* POST /api/users —— 新增用户 */
            if ("POST".equals(method) && "/api/users".equals(path)) {
                handleAdd(exchange);
                return;
            }

            /* 处理带路径参数的请求：/api/users/{id} */
            if (path.startsWith("/api/users/")) {
                /* 从路径中提取用户ID */
                String idStr = path.substring("/api/users/".length());
                Integer id;
                try {
                    id = Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    sendError(exchange, 400, "无效的用户ID格式: " + idStr);
                    return;
                }

                if ("GET".equals(method)) {
                    /* GET /api/users/{id} —— 查询单个用户 */
                    handleGetById(exchange, id);
                } else if ("PUT".equals(method)) {
                    /* PUT /api/users/{id} —— 更新用户信息 */
                    handleUpdate(exchange, id);
                } else if ("DELETE".equals(method)) {
                    /* DELETE /api/users/{id} —— 删除用户 */
                    handleDelete(exchange, id);
                } else {
                    sendError(exchange, 405, "不支持的请求方法: " + method);
                }
                return;
            }

            sendError(exchange, 404, "接口不存在: " + method + " " + path);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 分页查询用户列表 —— GET /api/users
     * 查询参数：
     *   page     —— 页码，从1开始，默认为1
     *   pageSize —— 每页记录数，默认为10
     *   keyword  —— 搜索关键词（可选），支持按用户名/真实姓名/手机号模糊搜索
     * 响应：包含用户列表和分页信息的JSON对象
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleList(HttpExchange exchange) throws IOException {
        /* 解析查询参数 */
        int page = 1;
        int pageSize = 10;
        String keyword = null;

        String pageStr = getQueryParam(exchange, "page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        String pageSizeStr = getQueryParam(exchange, "pageSize");
        if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
            try {
                pageSize = Integer.parseInt(pageSizeStr);
            } catch (NumberFormatException e) {
                pageSize = 10;
            }
        }

        keyword = getQueryParam(exchange, "keyword");

        /* 调用服务层获取分页数据 */
        List<User> users = userService.getUsersByPage(page, pageSize, keyword);
        int total = userService.getTotalCount(keyword);

        /* 构建响应数据 */
        Map<String, Object> response = new HashMap<>();
        response.put("list", users);
        response.put("total", total);
        response.put("page", page);
        response.put("pageSize", pageSize);

        /* 计算总页数 */
        int totalPages = (int) Math.ceil((double) total / pageSize);
        response.put("totalPages", totalPages);

        sendJson(exchange, response);
    }

    /**
     * 根据ID查询单个用户 —— GET /api/users/{id}
     *
     * @param exchange HTTP交换对象
     * @param id       用户ID
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleGetById(HttpExchange exchange, Integer id) throws IOException {
        /* 调用服务层根据ID查询用户 */
        User user = userService.getUserById(id);
        if (user == null) {
            sendError(exchange, 404, "用户不存在，ID: " + id);
            return;
        }

        sendJson(exchange, user);
    }

    /**
     * 新增用户 —— POST /api/users
     * 需要管理员登录后才能操作
     * 请求体JSON格式：User对象的所有字段
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当读取请求体或发送响应时发生I/O错误
     */
    private void handleAdd(HttpExchange exchange) throws IOException {
        /* 验证管理员是否已登录 */
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        /* 解析请求体中的用户数据 */
        User user = parseBody(exchange, User.class);

        /* 调用服务层新增用户 */
        String result = userService.addUser(user);

        /* 根据服务层返回结果判断操作是否成功 */
        Map<String, Object> response = new HashMap<>();
        if ("新增用户成功".equals(result)) {
            response.put("success", true);
            response.put("message", result);

            /* 记录操作日志 */
            logService.addLog(admin.getUsername(), "新增用户",
                    "管理员 [" + admin.getUsername() + "] 新增了用户 [" + user.getUsername() + "]");

            sendJson(exchange, response);
        } else {
            sendError(exchange, 400, result);
        }
    }

    /**
     * 更新用户信息 —— PUT /api/users/{id}
     * 需要管理员登录后才能操作
     * 请求体JSON格式：User对象中需要更新的字段
     *
     * @param exchange HTTP交换对象
     * @param id       用户ID
     * @throws IOException 当读取请求体或发送响应时发生I/O错误
     */
    private void handleUpdate(HttpExchange exchange, Integer id) throws IOException {
        /* 验证管理员是否已登录 */
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        /* 解析请求体中的用户数据 */
        User user = parseBody(exchange, User.class);
        user.setId(id);

        /* 调用服务层更新用户信息 */
        String result = userService.updateUser(user);

        /* 根据服务层返回结果判断操作是否成功 */
        Map<String, Object> response = new HashMap<>();
        if ("更新用户信息成功".equals(result)) {
            response.put("success", true);
            response.put("message", result);

            /* 记录操作日志 */
            logService.addLog(admin.getUsername(), "修改用户",
                    "管理员 [" + admin.getUsername() + "] 修改了用户ID=" + id + " 的信息");

            sendJson(exchange, response);
        } else {
            sendError(exchange, 400, result);
        }
    }

    /**
     * 删除用户 —— DELETE /api/users/{id}
     * 需要管理员登录后才能操作
     *
     * @param exchange HTTP交换对象
     * @param id       用户ID
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleDelete(HttpExchange exchange, Integer id) throws IOException {
        /* 验证管理员是否已登录 */
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        /* 先查询用户信息，用于日志记录 */
        User user = userService.getUserById(id);
        String username = user != null ? user.getUsername() : "未知";

        /* 调用服务层删除用户 */
        boolean success = userService.deleteUser(id);

        /* 根据服务层返回结果判断操作是否成功 */
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "删除用户成功");

            /* 记录操作日志 */
            logService.addLog(admin.getUsername(), "删除用户",
                    "管理员 [" + admin.getUsername() + "] 删除了用户 [" + username + "]，用户ID=" + id);

            sendJson(exchange, response);
        } else {
            sendError(exchange, 500, "删除用户失败，用户ID: " + id);
        }
    }

    /**
     * 用户账户充值 —— POST /api/users/recharge
     * 需要管理员登录后才能操作
     * 请求体JSON格式：{"userId": 1, "amount": 100.00}
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当读取请求体或发送响应时发生I/O错误
     */
    private void handleRecharge(HttpExchange exchange) throws IOException {
        /* 验证管理员是否已登录 */
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        /* 解析请求体中的充值参数 */
        @SuppressWarnings("unchecked")
        Map<String, Object> params = parseBody(exchange, Map.class);

        /* 提取并校验用户ID */
        Integer userId;
        try {
            Object userIdObj = params.get("userId");
            if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).intValue();
            } else {
                userId = Integer.parseInt(String.valueOf(userIdObj));
            }
        } catch (Exception e) {
            sendError(exchange, 400, "无效的用户ID");
            return;
        }

        /* 提取并校验充值金额 */
        BigDecimal amount;
        try {
            Object amountObj = params.get("amount");
            if (amountObj instanceof Number) {
                amount = new BigDecimal(amountObj.toString());
            } else {
                amount = new BigDecimal(String.valueOf(amountObj));
            }
        } catch (Exception e) {
            sendError(exchange, 400, "无效的充值金额");
            return;
        }

        /* 调用服务层执行充值操作 */
        boolean success = userService.recharge(userId, amount, admin.getUsername());

        /* 根据服务层返回结果判断操作是否成功 */
        Map<String, Object> response = new HashMap<>();
        if (success) {
            /* 查询充值后的用户信息 */
            User user = userService.getUserById(userId);
            response.put("success", true);
            response.put("message", "充值成功");
            if (user != null) {
                response.put("balance", user.getBalance());
            }

            /* 记录操作日志 */
            logService.addLog(admin.getUsername(), "充值",
                    "管理员 [" + admin.getUsername() + "] 为用户ID=" + userId + " 充值 " + amount + " 元");

            sendJson(exchange, response);
        } else {
            sendError(exchange, 500, "充值失败，请检查用户是否存在或充值金额是否有效");
        }
    }
}