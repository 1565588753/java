package com.internetcafe.controller;

import com.internetcafe.entity.Admin;
import com.internetcafe.entity.VipLevel;
import com.internetcafe.service.LogService;
import com.internetcafe.service.ReportService;
import com.internetcafe.service.VipService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员管理控制器
 * 负责处理网吧会员等级管理相关的Web API请求，包括会员等级查询、折扣率修改、
 * 用户会员等级升级以及会员分布统计等功能。
 * 修改类操作（更新折扣率、升级会员等级）需要管理员登录后才能执行。
 *
 * 路由映射：
 *   GET    /api/vip/levels       —— 获取所有会员等级
 *   PUT    /api/vip/levels/{id}  —— 更新会员等级折扣率
 *   POST   /api/vip/upgrade      —— 升级用户会员等级
 *   GET    /api/vip/stats        —— 获取会员分布统计数据
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class VipController extends BaseController implements HttpHandler {

    /** 会员服务对象，封装会员等级相关的业务逻辑 */
    private VipService vipService;

    /** 报表服务对象，用于获取会员分布统计数据 */
    private ReportService reportService;

    /** 日志服务对象，用于记录操作日志 */
    private LogService logService;

    /**
     * 构造方法 —— 初始化服务对象
     */
    public VipController() {
        this.vipService = new VipService();
        this.reportService = new ReportService();
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
            /* POST /api/vip/upgrade —— 升级会员等级 */
            if ("POST".equals(method) && "/api/vip/upgrade".equals(path)) {
                handleUpgrade(exchange);
                return;
            }

            /* GET /api/vip/stats —— 获取会员分布统计 */
            if ("GET".equals(method) && "/api/vip/stats".equals(path)) {
                handleStats(exchange);
                return;
            }

            /* GET /api/vip/levels —— 获取所有会员等级 */
            if ("GET".equals(method) && "/api/vip/levels".equals(path)) {
                handleGetLevels(exchange);
                return;
            }

            /* PUT /api/vip/levels/{id} —— 更新会员等级折扣率 */
            if (path.startsWith("/api/vip/levels/")) {
                if ("PUT".equals(method)) {
                    String idStr = path.substring("/api/vip/levels/".length());
                    Integer id;
                    try {
                        id = Integer.parseInt(idStr);
                    } catch (NumberFormatException e) {
                        sendError(exchange, 400, "无效的会员等级ID: " + idStr);
                        return;
                    }
                    handleUpdateLevel(exchange, id);
                } else {
                    sendError(exchange, 405, "不支持的请求方法: " + method);
                }
                return;
            }

            sendError(exchange, 404, "接口不存在: " + method + " " + path);
        } catch (Exception e) {
            handleException(exchange, e, "VipController处理请求异常");
        }
    }

    /**
     * 获取所有会员等级 —— GET /api/vip/levels
     * 返回系统中定义的全部会员等级列表，包含等级名称、折扣率、描述等信息
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleGetLevels(HttpExchange exchange) throws IOException {
        /* 调用服务层获取所有会员等级 */
        List<VipLevel> levels = vipService.getAllVipLevels();

        Map<String, Object> response = new HashMap<>();
        response.put("list", levels);
        response.put("total", levels.size());

        sendJson(exchange, response);
    }

    /**
     * 更新会员等级折扣率 —— PUT /api/vip/levels/{id}
     * 需要管理员登录后才能操作
     * 请求体JSON格式：{"discountRate": 0.85}
     * 折扣率范围：0.00 ~ 1.00，例如0.85表示享受85折优惠
     *
     * @param exchange HTTP交换对象
     * @param id       会员等级ID
     * @throws IOException 当读取请求体或发送响应时发生I/O错误
     */
    private void handleUpdateLevel(HttpExchange exchange, Integer id) throws IOException {
        /* 验证管理员是否已登录 */
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        /* 查询要修改的会员等级是否存在 */
        VipLevel vipLevel = vipService.getVipLevelById(id);
        if (vipLevel == null) {
            sendError(exchange, 404, "会员等级不存在，ID: " + id);
            return;
        }

        /* 解析请求体中的折扣率参数 */
        @SuppressWarnings("unchecked")
        Map<String, Object> params = parseBody(exchange, Map.class);

        /* 提取并校验折扣率 */
        BigDecimal discountRate;
        try {
            Object rateObj = params.get("discountRate");
            if (rateObj instanceof Number) {
                discountRate = new BigDecimal(rateObj.toString());
            } else {
                discountRate = new BigDecimal(String.valueOf(rateObj));
            }
        } catch (Exception e) {
            sendError(exchange, 400, "无效的折扣率格式");
            return;
        }

        /* 校验折扣率范围：必须在0到1之间 */
        if (discountRate.compareTo(BigDecimal.ZERO) < 0 || discountRate.compareTo(BigDecimal.ONE) > 0) {
            sendError(exchange, 400, "折扣率必须在0.00到1.00之间");
            return;
        }

        /* 更新会员等级的折扣率 */
        vipLevel.setDiscountRate(discountRate);

        /* 通过VipLevelDao执行更新（VipService没有直接更新VipLevel的方法） */
        com.internetcafe.dao.VipLevelDao vipLevelDao = new com.internetcafe.dao.VipLevelDao();
        boolean success = vipLevelDao.update(vipLevel);

        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "会员等级折扣率更新成功");

            /* 记录操作日志 */
            logService.addLog(admin.getUsername(), "修改会员等级",
                    "管理员 [" + admin.getUsername() + "] 将会员等级 [" + vipLevel.getLevelName()
                            + "] 的折扣率修改为 " + discountRate);

            sendJson(exchange, response);
        } else {
            sendError(exchange, 500, "会员等级折扣率更新失败");
        }
    }

    /**
     * 升级用户会员等级 —— POST /api/vip/upgrade
     * 需要管理员登录后才能操作
     * 请求体JSON格式：{"userId": 1, "levelId": 2}
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当读取请求体或发送响应时发生I/O错误
     */
    private void handleUpgrade(HttpExchange exchange) throws IOException {
        /* 验证管理员是否已登录 */
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        /* 解析请求体中的升级参数 */
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

        /* 提取并校验会员等级ID */
        Integer levelId;
        try {
            Object levelIdObj = params.get("levelId");
            if (levelIdObj instanceof Number) {
                levelId = ((Number) levelIdObj).intValue();
            } else {
                levelId = Integer.parseInt(String.valueOf(levelIdObj));
            }
        } catch (Exception e) {
            sendError(exchange, 400, "无效的会员等级ID");
            return;
        }

        /* 调用服务层执行会员升级 */
        try {
            vipService.upgradeVipLevel(userId, levelId);

            /* 查询升级后的用户信息 */
            com.internetcafe.entity.User user = new com.internetcafe.service.UserService().getUserById(userId);
            VipLevel newLevel = vipService.getVipLevelById(levelId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "会员等级升级成功");
            if (user != null) {
                response.put("username", user.getUsername());
                response.put("vipLevelName", user.getVipLevelName());
            }

            /* 记录操作日志 */
            String levelName = newLevel != null ? newLevel.getLevelName() : "等级ID=" + levelId;
            logService.addLog(admin.getUsername(), "会员升级",
                    "管理员 [" + admin.getUsername() + "] 将用户ID=" + userId + " 升级为 [" + levelName + "]");

            sendJson(exchange, response);
        } catch (RuntimeException e) {
            sendError(exchange, 400, e.getMessage());
        }
    }

    /**
     * 获取会员分布统计数据 —— GET /api/vip/stats
     * 统计各个会员等级下的用户数量，用于会员结构分析
     * 返回格式：[{"levelName": "普通会员", "userCount": 120}, ...]
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleStats(HttpExchange exchange) throws IOException {
        /* 调用报表服务获取会员分布数据 */
        List<Map<String, Object>> distribution = reportService.getVipDistribution();

        Map<String, Object> response = new HashMap<>();
        response.put("distribution", distribution);
        response.put("total", distribution.size());

        sendJson(exchange, response);
    }
}