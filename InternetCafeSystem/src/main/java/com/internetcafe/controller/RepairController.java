package com.internetcafe.controller;

import com.internetcafe.dao.OnlineRecordDao;
import com.internetcafe.dao.RepairRequestDao;
import com.internetcafe.dao.UserDao;
import com.internetcafe.entity.Admin;
import com.internetcafe.entity.OnlineRecord;
import com.internetcafe.entity.RepairRequest;
import com.internetcafe.entity.User;
import com.internetcafe.service.LogService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepairController extends BaseController implements HttpHandler {

    private RepairRequestDao repairRequestDao;
    private UserDao userDao;
    private OnlineRecordDao onlineRecordDao;
    private LogService logService;

    public RepairController() {
        this.repairRequestDao = new RepairRequestDao();
        this.userDao = new UserDao();
        this.onlineRecordDao = new OnlineRecordDao();
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
            if ("GET".equals(method) && "/api/repair".equals(path)) {
                handleGetRepairs(exchange);
            } else if ("GET".equals(method) && path.equals("/api/repair/pending-count")) {
                handlePendingCount(exchange);
            } else if ("POST".equals(method) && "/api/repair".equals(path)) {
                handleCreateRepair(exchange);
            } else if ("PUT".equals(method) && path.startsWith("/api/repair/") && path.endsWith("/done")) {
                String idStr = path.replace("/api/repair/", "").replace("/done", "");
                handleDoneRepair(exchange, Integer.parseInt(idStr));
            } else {
                sendError(exchange, 404, "接口不存在: " + method + " " + path);
            }
        } catch (Exception e) {
            handleException(exchange, e, "RepairController处理请求异常");
        }
    }

    private void handleGetRepairs(HttpExchange exchange) throws IOException {
        Admin admin = getSession(exchange);
        User user = getUserSession(exchange);

        if (admin == null && user == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        List<RepairRequest> repairs;
        if (admin != null) {
            repairs = repairRequestDao.findAll();
        } else {
            repairs = repairRequestDao.findByUserId(user.getId());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("data", repairs);
        sendJson(exchange, response);
    }

    private void handlePendingCount(HttpExchange exchange) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        int count = repairRequestDao.countPending();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        sendJson(exchange, response);
    }

    private void handleCreateRepair(HttpExchange exchange) throws IOException {
        User user = getUserSession(exchange);
        if (user == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        OnlineRecord active = onlineRecordDao.findActiveByUserId(user.getId());
        if (active == null) {
            sendError(exchange, 400, "您当前没有正在上机的记录，无法报修");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> params = parseBody(exchange, Map.class);
        String description = params != null ? (String) params.get("description") : null;
        if (description == null || description.trim().isEmpty()) {
            sendError(exchange, 400, "请描述故障情况");
            return;
        }

        RepairRequest request = new RepairRequest();
        request.setUserId(user.getId());
        request.setUsername(user.getUsername());
        request.setMachineNo(active.getMachineNo());
        request.setDescription(description.trim());
        request.setCreateTime(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));

        Integer id = repairRequestDao.insert(request);
        if (id == null || id == -1) {
            sendError(exchange, 500, "报修提交失败");
            return;
        }

        logService.addLog(user.getUsername(), "提交报修",
                "用户 [" + user.getUsername() + "] 对机器 [" + active.getMachineNo() + "] 提交报修: " + description);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", id);
        sendJson(exchange, response);
    }

    private void handleDoneRepair(HttpExchange exchange, Integer id) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        String now = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        boolean result = repairRequestDao.updateStatus(id, 1, now);
        if (!result) {
            sendError(exchange, 500, "操作失败");
            return;
        }

        logService.addLog(admin.getUsername(), "处理报修",
                "管理员 [" + admin.getUsername() + "] 已将报修单 #" + id + " 标记为已处理");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        sendJson(exchange, response);
    }

    }
