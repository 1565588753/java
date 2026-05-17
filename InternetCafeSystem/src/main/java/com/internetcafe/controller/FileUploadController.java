package com.internetcafe.controller;

import com.internetcafe.entity.Admin;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileUploadController extends BaseController implements HttpHandler {

    private static final String UPLOAD_DIR = "src/main/resources/web/uploads/snacks";
    private static final String UPLOAD_URL_PREFIX = "/uploads/snacks/";

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
            if ("POST".equals(method) && "/api/upload/snack-image".equals(path)) {
                handleUpload(exchange);
            } else if ("DELETE".equals(method) && path.startsWith("/api/upload/snack-image/")) {
                handleDelete(exchange);
            } else {
                sendError(exchange, 404, "接口不存在: " + method + " " + path);
            }
        } catch (Exception e) {
            handleException(exchange, e, "FileUploadController处理请求异常");
        }
    }

    private void handleUpload(HttpExchange exchange) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            sendError(exchange, 400, "Content-Type必须是multipart/form-data");
            return;
        }

        String boundary = extractBoundary(contentType);
        if (boundary == null) {
            sendError(exchange, 400, "无法解析multipart边界");
            return;
        }

        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = is.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        byte[] allBytes = baos.toByteArray();
        String fullBody = new String(allBytes, "UTF-8");

        int idx = fullBody.indexOf("filename=\"");
        if (idx == -1) {
            sendError(exchange, 400, "未找到文件");
            return;
        }

        idx = fullBody.indexOf('"', idx + 10);
        String originalFilename = fullBody.substring(fullBody.indexOf("filename=\"") + 10, idx);

        String ext = "";
        int dotIdx = originalFilename.lastIndexOf('.');
        if (dotIdx > 0) {
            ext = originalFilename.substring(dotIdx).toLowerCase();
        }
        if (!ext.matches("\\.(png|jpg|jpeg|gif|webp|bmp)$")) {
            sendError(exchange, 400, "仅支持PNG/JPG/GIF/WEBP/BMP格式的图片");
            return;
        }

        String newFilename = UUID.randomUUID().toString() + ext;
        String filePath = UPLOAD_DIR + "/" + newFilename;

        String headerBoundary = "--" + boundary;
        int dataStart = fullBody.indexOf("\r\n\r\n", fullBody.indexOf(headerBoundary)) + 4;
        int dataEnd = fullBody.lastIndexOf(headerBoundary);
        if (dataEnd == -1) {
            dataEnd = fullBody.lastIndexOf("--" + boundary);
        }

        byte[] fileBytes = new String(allBytes, "ISO-8859-1")
                .substring(dataStart, dataEnd)
                .getBytes("ISO-8859-1");

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(fileBytes);
        }

        String fileUrl = UPLOAD_URL_PREFIX + newFilename;

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("url", fileUrl);
        response.put("filename", newFilename);
        sendJson(exchange, response);
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        Admin admin = getSession(exchange);
        if (admin == null) {
            sendError(exchange, 401, "请先登录后再操作");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String filename = path.replace("/api/upload/snack-image/", "");

        if (filename.contains("/") || filename.contains("..")) {
            sendError(exchange, 400, "非法文件名");
            return;
        }

        File file = new File(UPLOAD_DIR + "/" + filename);
        if (file.exists() && file.delete()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            sendJson(exchange, response);
        } else {
            sendError(exchange, 404, "文件不存在");
        }
    }

    private String extractBoundary(String contentType) {
        int idx = contentType.indexOf("boundary=");
        if (idx == -1) return null;
        return contentType.substring(idx + 9);
    }
}
