package com.internetcafe.controller;

import com.internetcafe.entity.Admin;
import com.internetcafe.entity.SnackOrder;
import com.internetcafe.entity.SnackProduct;
import com.internetcafe.entity.User;
import com.internetcafe.service.SnackService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnackController extends BaseController implements HttpHandler {

    private SnackService snackService;

    public SnackController() {
        this.snackService = new SnackService();
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
            if ("GET".equals(method) && "/api/snacks/products".equals(path)) {
                handleGetProducts(exchange);
                return;
            }

            if ("GET".equals(method) && "/api/snacks/products/admin".equals(path)) {
                handleGetProductsAdmin(exchange);
                return;
            }

            if ("POST".equals(method) && "/api/snacks/products".equals(path)) {
                handleAddProduct(exchange);
                return;
            }

            if ("POST".equals(method) && "/api/snacks/orders".equals(path)) {
                handlePlaceOrder(exchange);
                return;
            }

            if ("GET".equals(method) && "/api/snacks/orders".equals(path)) {
                handleGetOrders(exchange);
                return;
            }

            if ("GET".equals(method) && "/api/snacks/orders/pending".equals(path)) {
                handleGetPendingOrders(exchange);
                return;
            }

            if (path.startsWith("/api/snacks/products/")) {
                String idStr = path.substring("/api/snacks/products/".length());
                int id;
                try {
                    id = Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    sendError(exchange, 400, "无效的商品ID");
                    return;
                }

                if ("GET".equals(method)) {
                    handleGetProduct(exchange, id);
                } else if ("PUT".equals(method)) {
                    handleUpdateProduct(exchange, id);
                } else if ("DELETE".equals(method)) {
                    handleDeleteProduct(exchange, id);
                } else {
                    sendError(exchange, 405, "不支持的请求方法");
                }
                return;
            }

            if (path.startsWith("/api/snacks/orders/")) {
                String subPath = path.substring("/api/snacks/orders/".length());

                if (subPath.endsWith("/confirm")) {
                    String idStr = subPath.substring(0, subPath.length() - "/confirm".length());
                    int id;
                    try {
                        id = Integer.parseInt(idStr);
                    } catch (NumberFormatException e) {
                        sendError(exchange, 400, "无效的订单ID");
                        return;
                    }
                    if ("PUT".equals(method)) {
                        handleConfirmOrder(exchange, id);
                    } else {
                        sendError(exchange, 405, "不支持的请求方法");
                    }
                    return;
                }

                if (subPath.endsWith("/cancel")) {
                    String idStr = subPath.substring(0, subPath.length() - "/cancel".length());
                    int id;
                    try {
                        id = Integer.parseInt(idStr);
                    } catch (NumberFormatException e) {
                        sendError(exchange, 400, "无效的订单ID");
                        return;
                    }
                    if ("PUT".equals(method)) {
                        handleCancelOrder(exchange, id);
                    } else {
                        sendError(exchange, 405, "不支持的请求方法");
                    }
                    return;
                }
            }

            sendError(exchange, 404, "接口不存在: " + method + " " + path);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "服务器内部错误: " + e.getMessage());
        }
    }

    private void handleGetProducts(HttpExchange exchange) throws IOException {
        List<SnackProduct> products = snackService.getAllProducts();
        Map<String, Object> response = new HashMap<>();
        response.put("list", products);
        sendJson(exchange, response);
    }

    private void handleGetProductsAdmin(HttpExchange exchange) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }
        List<SnackProduct> products = snackService.getAllProductsAdmin();
        Map<String, Object> response = new HashMap<>();
        response.put("list", products);
        sendJson(exchange, response);
    }

    private void handleGetProduct(HttpExchange exchange, int id) throws IOException {
        SnackProduct product = snackService.getProductById(id);
        Map<String, Object> response = new HashMap<>();
        if (product != null) {
            response.put("data", product);
        } else {
            response.put("data", null);
        }
        sendJson(exchange, response);
    }

    private void handleAddProduct(HttpExchange exchange) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }
        SnackProduct product = parseBody(exchange, SnackProduct.class);
        int id = snackService.addProduct(product);
        Map<String, Object> response = new HashMap<>();
        if (id > 0) {
            response.put("success", true);
            response.put("message", "添加商品成功");
            response.put("id", id);
            sendJson(exchange, response);
        } else {
            sendError(exchange, 500, "添加商品失败");
        }
    }

    private void handleUpdateProduct(HttpExchange exchange, int id) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }
        SnackProduct product = parseBody(exchange, SnackProduct.class);
        product.setId(id);
        int result = snackService.updateProduct(product);
        Map<String, Object> response = new HashMap<>();
        if (result > 0) {
            response.put("success", true);
            response.put("message", "更新商品成功");
            sendJson(exchange, response);
        } else {
            sendError(exchange, 500, "更新商品失败");
        }
    }

    private void handleDeleteProduct(HttpExchange exchange, int id) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }
        int result = snackService.deleteProduct(id);
        Map<String, Object> response = new HashMap<>();
        if (result > 0) {
            response.put("success", true);
            response.put("message", "删除商品成功");
            sendJson(exchange, response);
        } else {
            sendError(exchange, 500, "删除商品失败");
        }
    }

    private void handlePlaceOrder(HttpExchange exchange) throws IOException {
        User user = getUserSession(exchange);
        if (user == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> params = parseBody(exchange, Map.class);

        int productId;
        try {
            Object pidObj = params.get("productId");
            if (pidObj instanceof Number) {
                productId = ((Number) pidObj).intValue();
            } else {
                productId = Integer.parseInt(String.valueOf(pidObj));
            }
        } catch (Exception e) {
            sendError(exchange, 400, "无效的商品ID");
            return;
        }

        int quantity;
        try {
            Object qtyObj = params.get("quantity");
            if (qtyObj instanceof Number) {
                quantity = ((Number) qtyObj).intValue();
            } else {
                quantity = Integer.parseInt(String.valueOf(qtyObj));
            }
        } catch (Exception e) {
            sendError(exchange, 400, "无效的数量");
            return;
        }

        if (quantity <= 0) {
            sendError(exchange, 400, "数量必须大于0");
            return;
        }

        SnackOrder order = snackService.placeOrder(user.getId(), productId, quantity);
        if (order == null) {
            sendError(exchange, 400, "下单失败，商品不存在或已下架");
            return;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "下单成功，等待管理员确认");
        response.put("order", order);
        sendJson(exchange, response);
    }

    private void handleGetOrders(HttpExchange exchange) throws IOException {
        Admin admin = getSession(exchange);
        User user = getUserSession(exchange);

        if (admin != null) {
            List<SnackOrder> orders = snackService.getOrders();
            Map<String, Object> response = new HashMap<>();
            response.put("list", orders);
            sendJson(exchange, response);
            return;
        }

        if (user != null) {
            List<SnackOrder> orders = snackService.getUserOrders(user.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("list", orders);
            sendJson(exchange, response);
            return;
        }

        sendError(exchange, 401, "请先登录后再操作");
    }

    private void handleGetPendingOrders(HttpExchange exchange) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }
        List<SnackOrder> orders = snackService.getPendingOrders();
        Map<String, Object> response = new HashMap<>();
        response.put("list", orders);
        sendJson(exchange, response);
    }

    private void handleConfirmOrder(HttpExchange exchange, int id) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }
        String result = snackService.confirmOrder(id);
        Map<String, Object> response = new HashMap<>();
        if ("success".equals(result)) {
            response.put("success", true);
            response.put("message", "订单确认成功");
            sendJson(exchange, response);
        } else {
            sendError(exchange, 400, result);
        }
    }

    private void handleCancelOrder(HttpExchange exchange, int id) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }
        boolean success = snackService.cancelOrder(id);
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("success", true);
            response.put("message", "订单取消成功");
            sendJson(exchange, response);
        } else {
            sendError(exchange, 500, "订单取消失败");
        }
    }
}