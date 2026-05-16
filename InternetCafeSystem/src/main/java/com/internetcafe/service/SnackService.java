package com.internetcafe.service;

import com.internetcafe.dao.ConsumeRecordDao;
import com.internetcafe.dao.SnackOrderDao;
import com.internetcafe.dao.SnackProductDao;
import com.internetcafe.dao.UserDao;
import com.internetcafe.entity.ConsumeRecord;
import com.internetcafe.entity.SnackOrder;
import com.internetcafe.entity.SnackProduct;
import com.internetcafe.entity.User;
import com.internetcafe.util.DBUtil;
import com.internetcafe.util.DateUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

public class SnackService {

    private SnackProductDao snackProductDao;
    private SnackOrderDao snackOrderDao;
    private UserDao userDao;
    private ConsumeRecordDao consumeRecordDao;

    public SnackService() {
        this.snackProductDao = new SnackProductDao();
        this.snackOrderDao = new SnackOrderDao();
        this.userDao = new UserDao();
        this.consumeRecordDao = new ConsumeRecordDao();
    }

    public List<SnackProduct> getAllProducts() {
        return snackProductDao.findAll();
    }

    public List<SnackProduct> getAllProductsAdmin() {
        return snackProductDao.findAllIncludeDisabled();
    }

    public int addProduct(SnackProduct product) {
        return snackProductDao.insert(product);
    }

    public int updateProduct(SnackProduct product) {
        return snackProductDao.update(product);
    }

    public int deleteProduct(int id) {
        return snackProductDao.delete(id);
    }

    public SnackOrder placeOrder(int userId, int productId, int quantity) {
        SnackProduct product = snackProductDao.findById(productId);
        if (product == null) {
            return null;
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            return null;
        }

        BigDecimal unitPrice = product.getPrice();
        BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(quantity));

        SnackOrder order = new SnackOrder();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setTotalPrice(totalPrice);
        order.setStatus(0);

        int orderId = snackOrderDao.insert(order);
        if (orderId > 0) {
            order.setId(orderId);
            order.setOrderTime(DateUtil.getCurrentDateTime());
            return order;
        }
        return null;
    }

    public String confirmOrder(int orderId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                return "无法获取数据库连接";
            }
            DBUtil.beginTransaction(conn);

            List<SnackOrder> allOrders = snackOrderDao.findAll();
            SnackOrder order = null;
            for (SnackOrder o : allOrders) {
                if (o.getId() == orderId) {
                    order = o;
                    break;
                }
            }
            if (order == null) {
                DBUtil.rollbackTransaction(conn);
                return "订单不存在";
            }
            if (order.getStatus() != 0) {
                DBUtil.rollbackTransaction(conn);
                return "订单状态不正确，只能确认待确认的订单";
            }

            User user = userDao.findById(order.getUserId());
            if (user == null) {
                DBUtil.rollbackTransaction(conn);
                return "用户不存在";
            }

            BigDecimal totalPrice = order.getTotalPrice();
            BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
            if (currentBalance.compareTo(totalPrice) < 0) {
                DBUtil.rollbackTransaction(conn);
                return "用户余额不足，当前余额：" + currentBalance + "，需要：" + totalPrice;
            }

            BigDecimal newBalance = currentBalance.subtract(totalPrice);
            int updateResult = userDao.updateBalance(order.getUserId(), newBalance);
            if (updateResult <= 0) {
                DBUtil.rollbackTransaction(conn);
                return "更新用户余额失败";
            }

            ConsumeRecord record = new ConsumeRecord();
            record.setUserId(order.getUserId());
            record.setAmount(totalPrice);
            record.setConsumeType("零食消费");
            record.setCreateTime(DateUtil.getCurrentDateTime());
            boolean insertResult = consumeRecordDao.insert(record);
            if (!insertResult) {
                DBUtil.rollbackTransaction(conn);
                return "创建消费记录失败";
            }

            String confirmTime = DateUtil.getCurrentDateTime();
            int statusResult = snackOrderDao.updateStatus(orderId, 1, confirmTime);
            if (statusResult <= 0) {
                DBUtil.rollbackTransaction(conn);
                return "更新订单状态失败";
            }

            DBUtil.commitTransaction(conn);
            return "success";
        } catch (Exception e) {
            DBUtil.rollbackTransaction(conn);
            System.err.println("确认零食订单异常: " + e.getMessage());
            return "服务器内部错误: " + e.getMessage();
        } finally {
            DBUtil.closeAll(conn, null);
        }
    }

    public boolean cancelOrder(int orderId) {
        return snackOrderDao.updateStatus(orderId, 2, null) > 0;
    }

    public List<SnackOrder> getOrders() {
        return snackOrderDao.findAll();
    }

    public List<SnackOrder> getUserOrders(int userId) {
        return snackOrderDao.findByUserId(userId);
    }

    public List<SnackOrder> getPendingOrders() {
        return snackOrderDao.findPending();
    }
}