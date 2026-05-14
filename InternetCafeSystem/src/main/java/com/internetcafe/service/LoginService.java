package com.internetcafe.service;

import com.internetcafe.dao.AdminDao;
import com.internetcafe.dao.SystemLogDao;
import com.internetcafe.entity.Admin;
import com.internetcafe.entity.SystemLog;
import com.internetcafe.util.DateUtil;
import com.internetcafe.util.PasswordUtil;

/**
 * 登录服务类
 * 负责管理员的登录、登出认证以及当前登录状态的管理。
 * 每次登录和登出操作都会自动记录到系统日志中，便于后续审计追踪。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class LoginService {

    /** 管理员数据访问对象，用于查询管理员账号信息 */
    private AdminDao adminDao;

    /** 系统日志数据访问对象，用于记录登录/登出操作日志 */
    private SystemLogDao systemLogDao;

    /** 当前已登录的管理员对象，未登录时为null */
    private Admin currentAdmin;

    /**
     * 构造方法
     * 初始化DAO对象，当前管理员状态默认为null（未登录状态）
     */
    public LoginService() {
        this.adminDao = new AdminDao();
        this.systemLogDao = new SystemLogDao();
        this.currentAdmin = null;
    }

    /**
     * 管理员登录
     * 根据用户名查找管理员账号，然后使用SHA-256加密后的密码进行比对校验。
     * 登录成功后会将管理员信息保存到currentAdmin中，并自动记录一条登录日志。
     *
     * @param username 管理员用户名
     * @param password 管理员输入的明文密码
     * @return 登录成功返回true，用户名不存在或密码错误返回false
     */
    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.err.println("登录失败：用户名或密码不能为空");
            return false;
        }

        Admin admin = adminDao.findByUsername(username.trim());
        if (admin == null) {
            System.err.println("登录失败：用户名 [" + username + "] 不存在");
            return false;
        }

        boolean passwordValid = PasswordUtil.verify(password, admin.getPassword());
        if (!passwordValid) {
            System.err.println("登录失败：用户 [" + username + "] 密码错误");
            return false;
        }

        this.currentAdmin = admin;

        SystemLog loginLog = new SystemLog();
        loginLog.setOperatorName(admin.getUsername());
        loginLog.setOperationType("登录");
        loginLog.setOperationContent("管理员 [" + admin.getUsername() + "] 登录系统，角色：" + admin.getRole());
        loginLog.setCreateTime(DateUtil.getCurrentDateTime());
        systemLogDao.insert(loginLog);

        System.out.println("管理员 [" + admin.getUsername() + "] 登录成功");
        return true;
    }

    /**
     * 管理员登出
     * 记录一条登出日志后，将当前登录的管理员信息清空。
     * 如果当前没有管理员登录，则不做任何操作。
     */
    public void logout() {
        if (currentAdmin != null) {
            SystemLog logoutLog = new SystemLog();
            logoutLog.setOperatorName(currentAdmin.getUsername());
            logoutLog.setOperationType("登出");
            logoutLog.setOperationContent("管理员 [" + currentAdmin.getUsername() + "] 退出系统");
            logoutLog.setCreateTime(DateUtil.getCurrentDateTime());
            systemLogDao.insert(logoutLog);

            System.out.println("管理员 [" + currentAdmin.getUsername() + "] 已登出");
            this.currentAdmin = null;
        }
    }

    /**
     * 获取当前已登录的管理员对象
     *
     * @return 当前登录的Admin对象，如果未登录则返回null
     */
    public Admin getCurrentAdmin() {
        return currentAdmin;
    }

    /**
     * 判断当前是否有管理员已登录
     *
     * @return 已登录返回true，未登录返回false
     */
    public boolean isLoggedIn() {
        return currentAdmin != null;
    }

    /**
     * 判断当前登录的管理员是否为超级管理员（admin角色）
     * 需要先调用 isLoggedIn() 确认已登录后再使用此方法。
     *
     * @return 当前用户为admin角色返回true，未登录或非admin角色返回false
     */
    public boolean isAdmin() {
        return currentAdmin != null && "admin".equals(currentAdmin.getRole());
    }
}