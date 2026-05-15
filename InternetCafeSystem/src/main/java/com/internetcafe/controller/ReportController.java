package com.internetcafe.controller;

import com.internetcafe.service.ReportService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表统计控制器
 * 负责处理网吧运营数据统计分析的Web API请求，包括今日/本月营收概览、
 * 用户消费排行榜以及月度营收趋势等报表功能。
 * 所有查询操作无需登录验证（可根据需要添加）。
 *
 * 路由映射：
 *   GET /api/report/summary  —— 获取概览统计数据
 *   GET /api/report/ranking  —— 获取用户消费排行榜
 *   GET /api/report/monthly  —— 获取月度营收统计
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class ReportController extends BaseController implements HttpHandler {

    /** 报表服务对象，封装营收统计、排行榜、会员分布等统计分析逻辑 */
    private ReportService reportService;

    /**
     * 构造方法 —— 初始化报表服务对象
     */
    public ReportController() {
        this.reportService = new ReportService();
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
            /* GET /api/report/summary —— 获取概览统计 */
            if ("GET".equals(method) && "/api/report/summary".equals(path)) {
                handleSummary(exchange);
                return;
            }

            /* GET /api/report/ranking —— 获取消费排行榜 */
            if ("GET".equals(method) && "/api/report/ranking".equals(path)) {
                handleRanking(exchange);
                return;
            }

            /* GET /api/report/monthly —— 获取月度营收统计 */
            if ("GET".equals(method) && "/api/report/monthly".equals(path)) {
                handleMonthly(exchange);
                return;
            }

            /* GET /api/report/growth —— 获取会员增长趋势 */
            if ("GET".equals(method) && "/api/report/growth".equals(path)) {
                handleGrowth(exchange);
                return;
            }

            sendError(exchange, 404, "接口不存在: " + method + " " + path);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 获取概览统计数据 —— GET /api/report/summary
     * 返回今日和本月的核心运营指标概览，包括：
     *   - todayRevenue   : 今日营收总额
     *   - monthRevenue   : 本月营收总额
     *   - todayRecharge  : 今日充值总额
     *   - monthRecharge  : 本月充值总额
     *   - todayOnlineCount : 今日上机人次
     *   - totalUsers     : 系统用户总数
     *   - activeUsers    : 活跃用户数量
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleSummary(HttpExchange exchange) throws IOException {
        /* 获取各项统计数据 */
        BigDecimal todayRevenue = reportService.getTodayRevenue();
        BigDecimal monthRevenue = reportService.getMonthRevenue();
        BigDecimal todayRecharge = reportService.getTodayRecharge();
        BigDecimal monthRecharge = reportService.getMonthRecharge();
        int todayOnlineCount = reportService.getTodayOnlineCount();
        int totalUsers = reportService.getTotalUsers();
        int activeUsers = reportService.getActiveUsers();

        /* 构建响应数据 */
        Map<String, Object> response = new HashMap<>();
        response.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);
        response.put("monthRevenue", monthRevenue != null ? monthRevenue : BigDecimal.ZERO);
        response.put("todayRecharge", todayRecharge != null ? todayRecharge : BigDecimal.ZERO);
        response.put("monthRecharge", monthRecharge != null ? monthRecharge : BigDecimal.ZERO);
        response.put("todayOnlineCount", todayOnlineCount);
        response.put("totalUsers", totalUsers);
        response.put("activeUsers", activeUsers);

        sendJson(exchange, response);
    }

    /**
     * 获取用户消费排行榜 —— GET /api/report/ranking
     * 按消费总额降序排列，展示每个用户的消费总金额和消费次数
     * 返回格式：[{"username": "张三", "totalAmount": 500.00, "consumeCount": 30}, ...]
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleRanking(HttpExchange exchange) throws IOException {
        /* 调用报表服务获取消费排行榜数据 */
        List<Map<String, Object>> ranking = reportService.getUserConsumeRanking();

        Map<String, Object> response = new HashMap<>();
        response.put("ranking", ranking);
        response.put("total", ranking.size());

        sendJson(exchange, response);
    }

    /**
     * 获取月度营收统计 —— GET /api/report/monthly
     * 查询参数：
     *   year —— 要统计的年份（如 2025），默认为当前年份
     * 返回指定年份1-12月每个月的消费总额
     * 返回格式：[{"month": 1, "amount": 3500.00}, {"month": 2, "amount": 4200.00}, ...]
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleMonthly(HttpExchange exchange) throws IOException {
        /* 解析查询参数中的年份 */
        int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        String yearStr = getQueryParam(exchange, "year");
        if (yearStr != null && !yearStr.isEmpty()) {
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {
                /* 年份格式无效时使用当前年份 */
            }
        }

        /* 调用报表服务获取月度统计数据 */
        List<Map<String, Object>> monthlyStats = reportService.getMonthlyStats(year);

        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("monthlyStats", monthlyStats);

        sendJson(exchange, response);
    }

    private void handleGrowth(HttpExchange exchange) throws IOException {
        int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        String yearStr = getQueryParam(exchange, "year");
        if (yearStr != null && !yearStr.isEmpty()) {
            try {
                year = Integer.parseInt(yearStr);
            } catch (NumberFormatException e) {}
        }

        List<Map<String, Object>> growthTrend = reportService.getMemberGrowthTrend(year);

        Map<String, Object> response = new HashMap<>();
        response.put("year", year);
        response.put("growthTrend", growthTrend);

        sendJson(exchange, response);
    }
}