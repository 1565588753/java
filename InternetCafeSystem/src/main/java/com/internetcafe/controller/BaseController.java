package com.internetcafe.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.internetcafe.entity.Admin;
import com.internetcafe.entity.User;
import com.internetcafe.service.LogService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.net.httpserver.HttpExchange;

/**
 * 控制器基类
 * 为所有Web API控制器提供通用功能，包括JSON序列化、会话管理、
 * CORS跨域支持、请求体解析等基础能力。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class BaseController {

    /** Gson实例，用于Java对象与JSON字符串之间的相互转换 */
    protected static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    /**
     * 会话管理Map —— 使用线程安全的ConcurrentHashMap存储管理员登录会话
     * Key   : 会话令牌（token），由UUID生成，保证全局唯一
     * Value : 已登录的管理员对象（Admin）
     * 通过此Map实现无状态HTTP服务中的会话保持机制
     */
    protected static final Map<String, Admin> sessionMap = new ConcurrentHashMap<>();

    protected static final Map<String, User> userSessionMap = new ConcurrentHashMap<>();

    /**
     * 将Java对象序列化为JSON字符串，并通过HTTP响应发送给客户端
     * 设置Content-Type为application/json，使用UTF-8编码
     *
     * @param exchange HTTP交换对象，用于获取响应流
     * @param obj      需要序列化并发送的Java对象
     * @throws IOException 当写入响应流发生I/O错误时抛出
     */
    protected static void sendJson(HttpExchange exchange, Object obj) throws IOException {
        /* 将Java对象转换为JSON字符串 */
        String json = gson.toJson(obj);
        /* 获取JSON字符串的UTF-8字节数组 */
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        /* 设置响应头：内容类型为JSON，字符编码为UTF-8 */
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        /* 发送响应头（状态码200）并指定响应体长度 */
        exchange.sendResponseHeaders(200, bytes.length);

        /* 将JSON字节流写入响应体 */
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    /**
     * 向客户端发送JSON格式的错误响应
     * 构建一个包含错误码和错误消息的Map，序列化为JSON后发送
     *
     * @param exchange HTTP交换对象
     * @param code     HTTP状态码，如400（请求错误）、401（未授权）、404（未找到）、500（服务器错误）
     * @param message  错误提示消息，用于前端展示
     * @throws IOException 当写入响应流发生I/O错误时抛出
     */
    protected static void sendError(HttpExchange exchange, int code, String message) throws IOException {
        /* 构建错误信息Map */
        Map<String, Object> error = new java.util.HashMap<>();
        error.put("code", code);
        error.put("message", message);

        /* 将错误信息转换为JSON */
        String json = gson.toJson(error);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        /* 设置响应头 */
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        /* 发送错误响应 */
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    /**
     * 解析HTTP请求体中的JSON数据，将其反序列化为指定类型的Java对象
     * 用于处理POST/PUT请求中的JSON请求体
     *
     * @param exchange HTTP交换对象，用于读取请求体输入流
     * @param clazz    目标Java类的Class对象，用于确定反序列化的目标类型
     * @param <T>      泛型参数，表示目标类型
     * @return 反序列化后的Java对象
     * @throws IOException 当读取请求体发生I/O错误时抛出
     */
    protected static <T> T parseBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        /* 从请求体中读取原始JSON字符串 */
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String body = sb.toString();

        /* 使用Gson将JSON字符串反序列化为目标类型对象 */
        return gson.fromJson(body, clazz);
    }

    /**
     * 解析HTTP请求体中的JSON数据，支持带泛型的复杂类型反序列化
     * 例如：parseBody(exchange, new TypeToken<List<User>>(){}.getType())
     *
     * @param exchange HTTP交换对象
     * @param type     目标类型（可包含泛型信息）
     * @param <T>      泛型参数
     * @return 反序列化后的Java对象
     * @throws IOException 当读取请求体发生I/O错误时抛出
     */
    protected static <T> T parseBody(HttpExchange exchange, Type type) throws IOException {
        /* 从请求体中读取原始JSON字符串 */
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String body = sb.toString();

        /* 使用Gson将JSON字符串反序列化为目标类型对象 */
        return gson.fromJson(body, type);
    }

    /**
     * 从HTTP请求中获取当前已登录的管理员对象
     * 支持两种token传递方式（按优先级）：
     *   1. Cookie中的 "token" 字段 —— 浏览器自动携带
     *   2. 请求头中的 "Authorization: Bearer <token>" —— API调用方式
     *
     * @param exchange HTTP交换对象
     * @return 已登录的Admin对象，如果未登录或token无效则返回null
     */
    protected static Admin getSession(HttpExchange exchange) {
        String token = null;

        /* 方式一：尝试从Cookie中获取token */
        List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");
        if (cookieHeaders != null) {
            for (String cookieHeader : cookieHeaders) {
                /* 解析Cookie头中的各个Cookie键值对 */
                for (String cookieStr : cookieHeader.split(";")) {
                    cookieStr = cookieStr.trim();
                    if (cookieStr.startsWith("token=")) {
                        token = cookieStr.substring("token=".length());
                        break;
                    }
                }
                if (token != null) {
                    break;
                }
            }
        }

        /* 方式二：如果Cookie中没有找到，尝试从Authorization请求头中获取 */
        if (token == null) {
            List<String> authHeaders = exchange.getRequestHeaders().get("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String authHeader = authHeaders.get(0);
                /* 支持 "Bearer <token>" 格式 */
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring("Bearer ".length()).trim();
                }
            }
        }

        /* 根据token从会话Map中查找对应的管理员对象 */
        if (token != null && !token.isEmpty()) {
            return sessionMap.get(token);
        }

        return null;
    }

    /**
     * 从HTTP请求中获取当前已登录的用户对象（普通用户）
     * 优先从Cookie中读取token，如果没有则从Authorization请求头中读取
     *
     * @param exchange HTTP交换对象
     * @return 已登录的User对象，未登录返回null
     */
    protected static User getUserSession(HttpExchange exchange) {
        String token = null;

        List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");
        if (cookieHeaders != null) {
            for (String cookieHeader : cookieHeaders) {
                for (String cookieStr : cookieHeader.split(";")) {
                    cookieStr = cookieStr.trim();
                    if (cookieStr.startsWith("token=")) {
                        token = cookieStr.substring("token=".length());
                        break;
                    }
                }
                if (token != null) {
                    break;
                }
            }
        }

        if (token == null) {
            List<String> authHeaders = exchange.getRequestHeaders().get("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                String authHeader = authHeaders.get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring("Bearer ".length()).trim();
                }
            }
        }

        if (token != null && !token.isEmpty()) {
            return userSessionMap.get(token);
        }
        return null;
    }

    /**
     * 设置CORS（跨域资源共享）响应头
     * 允许前端页面从不同的域或端口访问后端API接口
     * 包括允许的来源、方法、请求头以及是否允许携带凭证（Cookie）
     *
     * @param exchange HTTP交换对象
     */
    protected static void setCorsHeaders(HttpExchange exchange) {
        /* 允许所有来源访问（生产环境建议限制为具体域名） */
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        /* 允许的HTTP请求方法 */
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        /* 允许的请求头字段 */
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        /* 允许携带Cookie等凭证信息 */
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        /* 预检请求的缓存时间（秒） */
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }

    /**
     * 处理OPTIONS预检请求
     * 浏览器在发送跨域请求前会先发送一个OPTIONS请求进行预检，
     * 服务器需要正确响应此预检请求，浏览器才会继续发送实际请求。
     *
     * @param exchange HTTP交换对象
     * @throws IOException 当写入响应流发生I/O错误时抛出
     */
    protected static void handleOptions(HttpExchange exchange) throws IOException {
        /* 设置CORS响应头 */
        setCorsHeaders(exchange);
        /* 返回204 No Content，表示预检通过 */
        exchange.sendResponseHeaders(204, -1);
    }

    /**
     * 获取请求URL中的查询参数值
     * 从URL的查询字符串（?key1=value1&key2=value2）中提取指定key对应的值
     *
     * @param exchange HTTP交换对象
     * @param key      参数名
     * @return 参数值，如果参数不存在则返回null
     */
    protected static String getQueryParam(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || query.isEmpty()) {
            return null;
        }

        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) {
                try {
                    return java.net.URLDecoder.decode(pair[1], "UTF-8");
                } catch (Exception e) {
                    return pair[1];
                }
            }
        }

        return null;
    }

    protected static void handleException(HttpExchange exchange, Exception e, String context) throws IOException {
        e.printStackTrace();
        try {
            LogService logService = new LogService();
            logService.addErrorLog("SYSTEM", context + ": " + e.getMessage());
        } catch (Exception logEx) {
            System.err.println("记录异常日志失败: " + logEx.getMessage());
        }
        sendError(exchange, 500, "服务器内部错误: " + e.getMessage());
    }
}