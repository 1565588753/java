package com.internetcafe.controller;

import com.internetcafe.entity.Admin;
import com.internetcafe.entity.OnlineRecord;
import com.internetcafe.entity.User;
import com.internetcafe.service.ChargeService;
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
 * 计费控制器
 * 负责处理网吧上机计费相关的Web API请求，包括开始上机、结束上机（下机结算）、
 * 查询活跃上机记录、查询历史记录以及按用户查询上机记录等功能。
 * 所有操作均需要管理员登录后才能执行。
 *
 * 路由映射：
 *   POST   /api/charge/start          —— 开始上机
 *   POST   /api/charge/stop/{id}      —— 停止上机（下机结算）
 *   GET    /api/charge/active         —— 获取所有活跃上机记录
 *   GET    /api/charge/history        —— 获取所有历史记录
 *   GET    /api/charge/records/{id}   —— 根据用户ID查询上机记录
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class ChargeController extends BaseController implements HttpHandler {

    /** 计费服务对象，封装上机/下机/费用计算等核心业务逻辑 */
    private ChargeService chargeService;

    /** 用户服务对象，用于查询用户信息 */
    private UserService userService;

    /** 日志服务对象，用于记录操作日志 */
    private LogService logService;

    /**
     * 构造方法 —— 初始化服务对象
     */
    public ChargeController() {
        this.chargeService = new ChargeService();
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
            /* POST /api/charge/start —— 开始上机 */
            if ("POST".equals(method) && "/api/charge/start".equals(path)) {
                handleStartOnline(exchange);
                return;
            }

            /* GET /api/charge/active —— 获取活跃上机记录 */
            if ("GET".equals(method) && "/api/charge/active".equals(path)) {
                handleActiveRecords(exchange);
                return;
            }

            /* GET /api/charge/history —— 获取所有历史记录 */
            if ("GET".equals(method) && "/api/charge/history".equals(path)) {
                handleHistoryRecords(exchange);
                return;
            }

            /* POST /api/charge/stop/{recordId} —— 停止上机 */
            if (path.startsWith("/api/charge/stop/")) {
                if ("POST".equals(method)) {
                    String idStr = path.substring("/api/charge/stop/".length());
                    Integer recordId;
                    try {
                        recordId = Integer.parseInt(idStr);
                    } catch (NumberFormatException e) {
                        sendError(exchange, 400, "无效的上机记录ID: " + idStr);
                        return;
                    }
                    handleStopOnline(exchange, recordId);
                } else {
                    sendError(exchange, 405, "不支持的请求方法: " + method);
                }
                return;
            }

            /* GET /api/charge/records/{userId} —— 根据用户ID查询记录 */
            if (path.startsWith("/api/charge/records/")) {
                if ("GET".equals(method)) {
                    String idStr = path.substring("/api/charge/records/".length());
                    Integer userId;
                    try {
                        userId = Integer.parseInt(idStr);
                    } catch (NumberFormatException e) {
                        sendError(exchange, 400, "无效的用户ID: " + idStr);
                        return;
                    }
                    handleRecordsByUser(exchange, userId);
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
     * 开始上机 —— POST /api/charge/start
     * 需要管理员登录后才能操作
     * 请求体JSON格式：{"userId": 1, "machineNo": "A01"}
     * 执行流程：校验用户状态 → 检查是否已上机 → 检查机器是否被占用 → 检查余额 → 创建上机记录
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当读取请求体或发送响应时发生I/O错误
     */
    private void handleStartOnline(HttpExchange exchange) throws IOException {
        /* 验证管理员是否已登录 */
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        /* 解析请求体中的上机参数 */
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

        /* 提取机器编号 */
        String machineNo = params.get("machineNo") != null ? params.get("machineNo").toString() : "";
        if (machineNo.trim().isEmpty()) {
            sendError(exchange, 400, "机器编号不能为空");
            return;
        }

        /* 调用服务层开始上机 */
        try {
            Integer recordId = chargeService.startOnline(userId, machineNo.trim());

            /* 查询用户信息，用于响应和日志 */
            User user = userService.getUserById(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "上机成功");
            response.put("recordId", recordId);
            if (user != null) {
                response.put("username", user.getUsername());
            }

            /* 记录操作日志 */
            String userName = user != null ? user.getUsername() : "用户ID=" + userId;
            logService.addLog(admin.getUsername(), "上机",
                    "管理员 [" + admin.getUsername() + "] 为用户 [" + userName + "] 开启上机，机器号：" + machineNo);

            sendJson(exchange, response);
        } catch (RuntimeException e) {
            sendError(exchange, 400, e.getMessage());
        }
    }

    /**
     * 停止上机（下机结算） —— POST /api/charge/stop/{recordId}
     * 需要管理员登录后才能操作
     * 执行流程：获取上机记录 → 计算时长 → 计算费用 → 更新记录 → 扣费 → 创建消费记录 → 增加积分
     *
     * @param exchange HTTP交换对象
     * @param recordId 上机记录ID
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleStopOnline(HttpExchange exchange, Integer recordId) throws IOException {
        /* 验证管理员是否已登录 */
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        /* 调用服务层停止上机并结算 */
        try {
            BigDecimal cost = chargeService.stopOnline(recordId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "下机成功");
            response.put("cost", cost);

            /* 记录操作日志 */
            logService.addLog(admin.getUsername(), "下机",
                    "管理员 [" + admin.getUsername() + "] 执行下机结算，记录ID=" + recordId + "，消费金额=" + cost + "元");

            sendJson(exchange, response);
        } catch (RuntimeException e) {
            sendError(exchange, 400, e.getMessage());
        }
    }

    /**
     * 获取所有活跃上机记录 —— GET /api/charge/active
     * 查询所有状态为"上机中"（status=1）的记录
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleActiveRecords(HttpExchange exchange) throws IOException {
        /* 调用服务层获取所有活跃上机记录 */
        List<OnlineRecord> records = chargeService.getActiveRecords();

        Map<String, Object> response = new HashMap<>();
        response.put("list", records);
        response.put("total", records.size());

        sendJson(exchange, response);
    }

    /**
     * 获取所有上机历史记录 —— GET /api/charge/history
     * 查询所有上机记录（包括已完成和活跃的），按时间降序排列
     * 此接口复用OnlineRecordDao.findAll()方法
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleHistoryRecords(HttpExchange exchange) throws IOException {
        /* 获取所有用户的上机记录 —— 通过getRecordsByUserId方式不太合适，
         * 这里直接使用ChargeService获取所有记录，但ChargeService没有getAll方法。
         * 这里采用折中方案：获取活跃+使用OnlineRecordDao的findAll */
        com.internetcafe.dao.OnlineRecordDao onlineRecordDao = new com.internetcafe.dao.OnlineRecordDao();
        List<OnlineRecord> records = onlineRecordDao.findAll();

        Map<String, Object> response = new HashMap<>();
        response.put("list", records);
        response.put("total", records.size());

        sendJson(exchange, response);
    }

    /**
     * 根据用户ID查询上机记录 —— GET /api/charge/records/{userId}
     * 查询指定用户的所有上机记录（包括历史和活跃），按时间降序排列
     *
     * @param exchange HTTP交换对象
     * @param userId   用户ID
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleRecordsByUser(HttpExchange exchange, Integer userId) throws IOException {
        /* 先检查用户是否存在 */
        User user = userService.getUserById(userId);
        if (user == null) {
            sendError(exchange, 404, "用户不存在，ID: " + userId);
            return;
        }

        /* 调用服务层获取该用户的所有上机记录 */
        List<OnlineRecord> records = chargeService.getRecordsByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("list", records);
        response.put("total", records.size());
        response.put("username", user.getUsername());

        sendJson(exchange, response);
    }
}