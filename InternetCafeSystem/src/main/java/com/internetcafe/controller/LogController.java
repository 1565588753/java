package com.internetcafe.controller;

import com.internetcafe.entity.Admin;
import com.internetcafe.entity.SystemLog;
import com.internetcafe.service.LogService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统日志控制器
 * 负责处理系统操作日志查询与清理相关的Web API请求。
 * 支持按操作人、操作类型、日期范围等多维度筛选查询日志，
 * 以及按日期清理过期日志的功能。
 *
 * 路由映射：
 *   GET    /api/logs        —— 查询系统日志（支持多条件筛选）
 *   DELETE /api/logs/clean  —— 清理指定日期之前的日志
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class LogController extends BaseController implements HttpHandler {

    /** 日志服务对象，封装日志记录与查询的业务逻辑 */
    private LogService logService;

    /**
     * 构造方法 —— 初始化日志服务对象
     */
    public LogController() {
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
            /* GET /api/logs —— 查询系统日志 */
            if ("GET".equals(method) && "/api/logs".equals(path)) {
                handleQueryLogs(exchange);
                return;
            }

            /* DELETE /api/logs/clean —— 清理日志 */
            if ("DELETE".equals(method) && "/api/logs/clean".equals(path)) {
                handleCleanLogs(exchange);
                return;
            }

            sendError(exchange, 404, "接口不存在: " + method + " " + path);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 查询系统日志 —— GET /api/logs
     * 支持以下可选查询参数进行多条件筛选：
     *   operator  —— 操作人名称（模糊匹配）
     *   type      —— 操作类型（如"登录"、"删除"、"修改"、"充值"等）
     *   startDate —— 开始日期（格式：yyyy-MM-dd）
     *   endDate   —— 结束日期（格式：yyyy-MM-dd）
     * 当所有参数均为空时，返回全部日志记录。
     * 如果有多个条件，则进行交集筛选（AND逻辑）。
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleQueryLogs(HttpExchange exchange) throws IOException {
        /* 解析查询参数 */
        String operator = getQueryParam(exchange, "operator");
        String type = getQueryParam(exchange, "type");
        String startDate = getQueryParam(exchange, "startDate");
        String endDate = getQueryParam(exchange, "endDate");

        List<SystemLog> logs;

        /* 根据是否有查询条件选择不同的查询策略 */
        boolean hasOperator = operator != null && !operator.trim().isEmpty();
        boolean hasType = type != null && !type.trim().isEmpty();
        boolean hasDateRange = startDate != null && !startDate.trim().isEmpty()
                && endDate != null && !endDate.trim().isEmpty();

        if (hasDateRange) {
            /* 有日期范围条件时，优先按日期范围查询 */
            logs = logService.getLogsByDateRange(startDate.trim(), endDate.trim());

            /* 再根据其他条件在内存中进行二次过滤 */
            if (hasOperator) {
                logs = filterByOperator(logs, operator.trim());
            }
            if (hasType) {
                logs = filterByType(logs, type.trim());
            }
        } else if (hasOperator) {
            /* 按操作人查询 */
            logs = logService.getLogsByOperator(operator.trim());

            /* 如果同时有操作类型条件，进行二次过滤 */
            if (hasType) {
                logs = filterByType(logs, type.trim());
            }
        } else if (hasType) {
            /* 按操作类型查询 */
            logs = logService.getLogsByType(type.trim());
        } else {
            /* 没有任何筛选条件时，返回全部日志 */
            logs = logService.getAllLogs();
        }

        /* 构建响应数据 */
        Map<String, Object> response = new HashMap<>();
        response.put("list", logs);
        response.put("total", logs.size());

        sendJson(exchange, response);
    }

    /**
     * 清理指定日期之前的日志 —— DELETE /api/logs/clean
     * 需要管理员登录后才能操作
     * 查询参数：
     *   date —— 截止日期（格式：yyyy-MM-dd），删除该日期之前（不含当天）的所有日志
     * 用于定期清理过期日志数据，防止日志表过大影响性能。
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当发送响应时发生I/O错误
     */
    private void handleCleanLogs(HttpExchange exchange) throws IOException {
        /* 验证管理员是否已登录 */
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        /* 解析清理日期参数 */
        String date = getQueryParam(exchange, "date");
        if (date == null || date.trim().isEmpty()) {
            sendError(exchange, 400, "请指定清理日期参数 date（格式：yyyy-MM-dd）");
            return;
        }

        /* 调用SystemLogDao的deleteBefore方法清理过期日志 */
        com.internetcafe.dao.SystemLogDao systemLogDao = new com.internetcafe.dao.SystemLogDao();
        int deletedCount = systemLogDao.deleteBefore(date.trim());

        /* 记录清理操作日志 */
        logService.addLog(admin.getUsername(), "清理日志",
                "管理员 [" + admin.getUsername() + "] 清理了 " + date + " 之前的系统日志，共删除 " + deletedCount + " 条记录");

        /* 构建响应数据 */
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "日志清理完成");
        response.put("deletedCount", deletedCount);
        response.put("cleanDate", date.trim());

        sendJson(exchange, response);
    }

    /**
     * 按操作人过滤日志列表（辅助方法）
     * 在内存中对日志列表进行二次筛选，保留操作人名称包含指定关键词的记录
     *
     * @param logs     原始日志列表
     * @param operator 操作人名称关键词
     * @return 过滤后的日志列表
     */
    private List<SystemLog> filterByOperator(List<SystemLog> logs, String operator) {
        List<SystemLog> result = new ArrayList<>();
        for (SystemLog log : logs) {
            if (log.getOperatorName() != null && log.getOperatorName().contains(operator)) {
                result.add(log);
            }
        }
        return result;
    }

    /**
     * 按操作类型过滤日志列表（辅助方法）
     * 在内存中对日志列表进行二次筛选，保留操作类型匹配的记录
     *
     * @param logs 原始日志列表
     * @param type 操作类型
     * @return 过滤后的日志列表
     */
    private List<SystemLog> filterByType(List<SystemLog> logs, String type) {
        List<SystemLog> result = new ArrayList<>();
        for (SystemLog log : logs) {
            if (type.equals(log.getOperationType())) {
                result.add(log);
            }
        }
        return result;
    }
}