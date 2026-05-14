package com.internetcafe.test;

import com.internetcafe.dao.*;
import com.internetcafe.entity.*;
import com.internetcafe.service.*;
import com.internetcafe.util.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 网吧计费管理系统 - 系统功能测试类
 *
 * 包含各模块的功能测试，验证系统核心业务逻辑是否正确
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class SystemTest {

    private static LoginService loginService = new LoginService();
    private static UserService userService = new UserService();
    private static ChargeService chargeService = new ChargeService();
    private static VipService vipService = new VipService();
    private static ReportService reportService = new ReportService();
    private static LogService logService = new LogService();

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  网吧计费管理系统 - 功能测试");
        System.out.println("========================================\n");

        testDatabaseConnection();
        testPasswordEncryption();
        testValidation();
        testLogin();
        testUserManagement();
        testVipService();
        testChargeService();
        testReportService();
        testLogService();

        System.out.println("\n========================================");
        System.out.println("  测试结果汇总");
        System.out.println("========================================");
        System.out.println("  通过: " + passed + " 项");
        System.out.println("  失败: " + failed + " 项");
        System.out.println("  总计: " + (passed + failed) + " 项");
        System.out.println("========================================");
    }

    /**
     * 测试数据库连接
     */
    private static void testDatabaseConnection() {
        System.out.println("【测试1】数据库连接测试");
        try {
            boolean result = DBUtil.testConnection();
            assertTrue(result, "数据库连接测试");
        } catch (Exception e) {
            assertTrue(false, "数据库连接测试 - 异常: " + e.getMessage());
        }
    }

    /**
     * 测试密码加密功能
     */
    private static void testPasswordEncryption() {
        System.out.println("\n【测试2】密码加密测试");
        try {
            String password = "admin123";
            String encrypted = PasswordUtil.encrypt(password);
            assertNotNull(encrypted, "密码加密结果不为空");
            assertTrue(encrypted.length() == 64, "SHA-256加密结果长度为64");

            boolean verified = PasswordUtil.verify(password, encrypted);
            assertTrue(verified, "密码验证成功");

            boolean wrongVerify = PasswordUtil.verify("wrongpassword", encrypted);
            assertTrue(!wrongVerify, "错误密码验证失败");
        } catch (Exception e) {
            assertTrue(false, "密码加密测试 - 异常: " + e.getMessage());
        }
    }

    /**
     * 测试数据校验功能
     */
    private static void testValidation() {
        System.out.println("\n【测试3】数据校验测试");
        try {
            assertTrue(ValidationUtil.isValidPhone("13800138001"), "合法手机号校验");
            assertTrue(!ValidationUtil.isValidPhone("12345"), "非法手机号校验");
            assertTrue(!ValidationUtil.isValidPhone(""), "空手机号校验");

            assertTrue(ValidationUtil.isValidIdCard("110101199001011234"), "合法身份证号校验");
            assertTrue(!ValidationUtil.isValidIdCard("12345"), "非法身份证号校验");

            assertTrue(ValidationUtil.isValidAmount(new BigDecimal("100")), "合法金额校验");
            assertTrue(!ValidationUtil.isValidAmount(new BigDecimal("-10")), "负数金额校验");
            assertTrue(!ValidationUtil.isValidAmount(BigDecimal.ZERO), "零金额校验");

            assertTrue(ValidationUtil.isEmpty(""), "空字符串判断");
            assertTrue(ValidationUtil.isEmpty(null), "null字符串判断");
            assertTrue(!ValidationUtil.isEmpty("hello"), "非空字符串判断");
        } catch (Exception e) {
            assertTrue(false, "数据校验测试 - 异常: " + e.getMessage());
        }
    }

    /**
     * 测试登录功能
     */
    private static void testLogin() {
        System.out.println("\n【测试4】登录功能测试");
        try {
            // 测试正确登录
            boolean loginResult = loginService.login("admin", "admin123");
            assertTrue(loginResult, "管理员登录成功");
            assertTrue(loginService.isLoggedIn(), "登录状态正确");
            assertTrue(loginService.isAdmin(), "角色为admin");

            Admin admin = loginService.getCurrentAdmin();
            assertNotNull(admin, "获取当前管理员成功");
            System.out.println("  当前登录管理员: " + admin.getUsername() + " (角色: " + admin.getRole() + ")");

            // 测试错误密码
            loginService.logout();
            boolean failLogin = loginService.login("admin", "wrongpassword");
            assertTrue(!failLogin, "错误密码登录失败");

            // 测试不存在的用户
            boolean noUserLogin = loginService.login("nonexistent", "admin123");
            assertTrue(!noUserLogin, "不存在用户登录失败");
        } catch (Exception e) {
            assertTrue(false, "登录测试 - 异常: " + e.getMessage());
        }
    }

    /**
     * 测试用户管理功能
     */
    private static void testUserManagement() {
        System.out.println("\n【测试5】用户管理测试");
        try {
            // 测试查询所有用户
            List<User> users = userService.getAllUsers();
            assertNotNull(users, "获取用户列表成功");
            System.out.println("  当前用户总数: " + users.size());

            // 测试模糊搜索
            List<User> searchResult = userService.searchUsers("test");
            assertNotNull(searchResult, "模糊搜索成功");
            System.out.println("  搜索'test'结果数: " + searchResult.size());

            // 测试分页
            List<User> pageResult = userService.getUsersByPage(1, 2, "");
            assertNotNull(pageResult, "分页查询成功");

            // 测试总数
            int total = userService.getTotalCount("");
            assertTrue(total >= 0, "获取总数成功");

            // 测试按用户名查询
            User user = userService.getUserByUsername("test001");
            assertNotNull(user, "按用户名查询成功");
            if (user != null) {
                System.out.println("  查询用户: " + user.getUsername() + " - " + user.getRealName());
                System.out.println("  余额: " + user.getBalance() + " 会员等级: " + user.getVipLevelName());
            }
        } catch (Exception e) {
            assertTrue(false, "用户管理测试 - 异常: " + e.getMessage());
        }
    }

    /**
     * 测试会员服务
     */
    private static void testVipService() {
        System.out.println("\n【测试6】会员服务测试");
        try {
            List<VipLevel> levels = vipService.getAllVipLevels();
            assertNotNull(levels, "获取会员等级列表成功");
            System.out.println("  会员等级数量: " + levels.size());
            for (VipLevel level : levels) {
                System.out.println("    " + level.getLevelName() + " - 折扣率: " + level.getDiscountRate());
            }

            // 测试获取折扣率
            BigDecimal discount = vipService.getDiscountRate(1);
            assertNotNull(discount, "获取折扣率成功");
            System.out.println("  普通会员折扣率: " + discount);

            BigDecimal vipDiscount = vipService.getDiscountRate(2);
            System.out.println("  VIP会员折扣率: " + vipDiscount);

            BigDecimal svipDiscount = vipService.getDiscountRate(3);
            System.out.println("  SVIP会员折扣率: " + svipDiscount);
        } catch (Exception e) {
            assertTrue(false, "会员服务测试 - 异常: " + e.getMessage());
        }
    }

    /**
     * 测试计费服务
     */
    private static void testChargeService() {
        System.out.println("\n【测试7】计费服务测试");
        try {
            // 测试费用计算
            User user = userService.getUserByUsername("test001");
            if (user != null) {
                BigDecimal fee1 = chargeService.calculateFee(user.getId(), 60L);
                System.out.println("  用户" + user.getUsername() + " 上机60分钟费用: " + fee1 + "元");

                BigDecimal fee2 = chargeService.calculateFee(user.getId(), 30L);
                System.out.println("  用户" + user.getUsername() + " 上机30分钟费用: " + fee2 + "元");
            }

            // 测试VIP用户费用
            User vipUser = userService.getUserByUsername("test002");
            if (vipUser != null) {
                BigDecimal vipFee = chargeService.calculateFee(vipUser.getId(), 60L);
                System.out.println("  VIP用户" + vipUser.getUsername() + " 上机60分钟费用: " + vipFee + "元");
            }

            // 测试活跃记录
            List<OnlineRecord> activeRecords = chargeService.getActiveRecords();
            System.out.println("  当前上机用户数: " + activeRecords.size());
        } catch (Exception e) {
            assertTrue(false, "计费服务测试 - 异常: " + e.getMessage());
        }
    }

    /**
     * 测试报表服务
     */
    private static void testReportService() {
        System.out.println("\n【测试8】报表统计测试");
        try {
            BigDecimal todayRevenue = reportService.getTodayRevenue();
            System.out.println("  今日营业额: " + todayRevenue + "元");

            BigDecimal monthRevenue = reportService.getMonthRevenue();
            System.out.println("  本月营业额: " + monthRevenue + "元");

            BigDecimal todayRecharge = reportService.getTodayRecharge();
            System.out.println("  今日充值: " + todayRecharge + "元");

            int todayOnline = reportService.getTodayOnlineCount();
            System.out.println("  今日上机次数: " + todayOnline);

            int totalUsers = reportService.getTotalUsers();
            System.out.println("  用户总数: " + totalUsers);

            int activeUsers = reportService.getActiveUsers();
            System.out.println("  活跃用户数: " + activeUsers);

            // 测试消费排行
            java.util.List<java.util.Map<String, Object>> ranking = reportService.getUserConsumeRanking();
            System.out.println("  消费排行记录数: " + ranking.size());

            // 测试会员分布
            java.util.List<java.util.Map<String, Object>> vipDist = reportService.getVipDistribution();
            System.out.println("  会员分布记录数: " + vipDist.size());
        } catch (Exception e) {
            assertTrue(false, "报表统计测试 - 异常: " + e.getMessage());
        }
    }

    /**
     * 测试日志服务
     */
    private static void testLogService() {
        System.out.println("\n【测试9】系统日志测试");
        try {
            // 添加测试日志
            logService.addLog("测试员", "测试", "这是一条测试日志");
            logService.addLoginLog("测试员");
            logService.addErrorLog("测试员", "模拟错误日志");

            List<SystemLog> logs = logService.getAllLogs();
            assertNotNull(logs, "获取日志列表成功");
            System.out.println("  日志总数: " + logs.size());

            List<SystemLog> loginLogs = logService.getLogsByType("登录");
            System.out.println("  登录日志数: " + loginLogs.size());

            List<SystemLog> operatorLogs = logService.getLogsByOperator("测试员");
            System.out.println("  测试员日志数: " + operatorLogs.size());
        } catch (Exception e) {
            assertTrue(false, "系统日志测试 - 异常: " + e.getMessage());
        }
    }

    /**
     * 断言方法 - 条件为真
     */
    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            System.out.println("  [通过] " + testName);
            passed++;
        } else {
            System.out.println("  [失败] " + testName);
            failed++;
        }
    }

    /**
     * 断言方法 - 对象不为空
     */
    private static void assertNotNull(Object obj, String testName) {
        assertTrue(obj != null, testName);
    }
}