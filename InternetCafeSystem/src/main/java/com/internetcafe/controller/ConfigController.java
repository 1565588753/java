package com.internetcafe.controller;

import com.internetcafe.dao.SystemConfigDao;
import com.internetcafe.entity.Admin;
import com.internetcafe.service.LogService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigController extends BaseController implements HttpHandler {

    private SystemConfigDao systemConfigDao;
    private LogService logService;

    public ConfigController() {
        this.systemConfigDao = new SystemConfigDao();
        this.logService = new LogService();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(method) && "/api/config".equals(path)) {
                handleGetConfig(exchange);
            } else if ("PUT".equals(method) && "/api/config".equals(path)) {
                handleUpdateConfig(exchange);
            } else {
                sendError(exchange, 404, "接口不存在: " + method + " " + path);
            }
        } catch (Exception e) {
            handleException(exchange, e, "ConfigController处理请求异常");
        }
    }

    private void handleGetConfig(HttpExchange exchange) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        Map<String, String> configs = systemConfigDao.getAllConfigs();
        Map<String, Object> response = new HashMap<>();
        String basePrice = configs.getOrDefault("base_price", "5.0");
        String peakPrice = configs.getOrDefault("peak_price", "8.0");
        String nightPrice = configs.getOrDefault("night_price", "3.0");
        response.put("basePrice", basePrice);
        response.put("peakPrice", peakPrice);
        response.put("nightPrice", nightPrice);

        sendJson(exchange, response);
    }

    private void handleUpdateConfig(HttpExchange exchange) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> params = parseBody(exchange, Map.class);

        if (params.containsKey("basePrice")) {
            systemConfigDao.updateConfig("base_price", String.valueOf(params.get("basePrice")));
        }
        if (params.containsKey("peakPrice")) {
            systemConfigDao.updateConfig("peak_price", String.valueOf(params.get("peakPrice")));
        }
        if (params.containsKey("nightPrice")) {
            systemConfigDao.updateConfig("night_price", String.valueOf(params.get("nightPrice")));
        }

        logService.addLog(admin.getUsername(), "修改配置",
                "管理员 [" + admin.getUsername() + "] 修改了系统价格配置");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "配置更新成功");
        sendJson(exchange, response);
    }
}