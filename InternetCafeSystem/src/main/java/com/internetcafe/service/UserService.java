package com.internetcafe.service;

import com.internetcafe.dao.RechargeRecordDao;
import com.internetcafe.dao.UserDao;
import com.internetcafe.entity.RechargeRecord;
import com.internetcafe.entity.User;
import com.internetcafe.util.DateUtil;
import com.internetcafe.util.PasswordUtil;
import com.internetcafe.util.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户服务类
 * 负责网吧会员用户的管理，包括用户的增删改查、分页搜索、模糊查询以及账户充值等功能。
 * 所有涉及密码的操作均通过 PasswordUtil 进行SHA-256加密处理，
 * 所有手机号、身份证号的格式校验均通过 ValidationUtil 完成。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class UserService {

    /** 用户数据访问对象，负责所有与用户表相关的数据库操作 */
    private UserDao userDao;

    /** 充值记录数据访问对象，用于在充值时创建充值记录 */
    private RechargeRecordDao rechargeRecordDao;

    /**
     * 构造方法
     * 初始化用户DAO和充值记录DAO对象
     */
    public UserService() {
        this.userDao = new UserDao();
        this.rechargeRecordDao = new RechargeRecordDao();
    }

    /**
     * 新增用户
     * 对用户信息进行完整的业务校验后，将用户数据插入数据库。
     * 校验内容包括：用户名是否重复、手机号格式是否正确、身份证号格式是否正确。
     * 密码在入库前会通过 PasswordUtil.encrypt() 进行SHA-256加密处理。
     *
     * @param user 待新增的用户对象，需包含 username、password、realName、idCard、phone 等必填字段
     * @return 操作结果提示信息字符串，如"新增用户成功"或具体的失败原因
     */
    public String addUser(User user) {
        if (user == null) {
            return "新增用户失败：用户对象不能为空";
        }

        if (ValidationUtil.isEmpty(user.getUsername())) {
            return "新增用户失败：用户名不能为空";
        }

        if (ValidationUtil.isEmpty(user.getPassword())) {
            return "新增用户失败：密码不能为空";
        }

        User existingUser = userDao.findByUsername(user.getUsername().trim());
        if (existingUser != null) {
            return "新增用户失败：用户名 [" + user.getUsername() + "] 已存在，请更换用户名";
        }

        if (ValidationUtil.isNotEmpty(user.getPhone()) && !ValidationUtil.isValidPhone(user.getPhone())) {
            return "新增用户失败：手机号 [" + user.getPhone() + "] 格式不正确";
        }

        if (ValidationUtil.isNotEmpty(user.getIdCard()) && !ValidationUtil.isValidIdCard(user.getIdCard())) {
            return "新增用户失败：身份证号 [" + user.getIdCard() + "] 格式不正确";
        }

        user.setPassword(PasswordUtil.encrypt(user.getPassword()));

        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }
        if (user.getVipLevel() == null) {
            user.setVipLevel(1);
        }
        if (user.getPoints() == null) {
            user.setPoints(0);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        int result = userDao.insert(user);
        if (result > 0) {
            System.out.println("新增用户成功：用户名=" + user.getUsername() + "，真实姓名=" + user.getRealName());
            return "新增用户成功";
        } else {
            return "新增用户失败：数据库操作异常，请稍后重试";
        }
    }

    /**
     * 更新用户信息
     * 根据用户ID更新数据库中对应记录的所有可修改字段。
     * 如果传入了新密码，则在更新前对密码进行SHA-256加密处理。
     *
     * @param user 待更新的用户对象，必须包含有效的id字段
     * @return 操作结果提示信息字符串，如"更新用户信息成功"或具体的失败原因
     */
    public String updateUser(User user) {
        if (user == null || user.getId() == null) {
            return "更新用户失败：用户对象或用户ID不能为空";
        }

        User existingUser = userDao.findById(user.getId());
        if (existingUser == null) {
            return "更新用户失败：用户ID [" + user.getId() + "] 对应的用户不存在";
        }

        if (ValidationUtil.isNotEmpty(user.getPhone()) && !ValidationUtil.isValidPhone(user.getPhone())) {
            return "更新用户失败：手机号 [" + user.getPhone() + "] 格式不正确";
        }

        if (ValidationUtil.isNotEmpty(user.getIdCard()) && !ValidationUtil.isValidIdCard(user.getIdCard())) {
            return "更新用户失败：身份证号 [" + user.getIdCard() + "] 格式不正确";
        }

        if (ValidationUtil.isNotEmpty(user.getPassword())) {
            user.setPassword(PasswordUtil.encrypt(user.getPassword()));
        } else {
            user.setPassword(existingUser.getPassword());
        }

        int result = userDao.update(user);
        if (result > 0) {
            System.out.println("更新用户信息成功：用户ID=" + user.getId() + "，用户名=" + user.getUsername());
            return "更新用户信息成功";
        } else {
            return "更新用户失败：数据库操作异常，请稍后重试";
        }
    }

    /**
     * 删除用户
     * 根据用户ID删除数据库中对应的用户记录。
     *
     * @param id 待删除的用户ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteUser(Integer id) {
        if (id == null) {
            System.err.println("删除用户失败：用户ID不能为空");
            return false;
        }

        int result = userDao.delete(id);
        if (result > 0) {
            System.out.println("删除用户成功：用户ID=" + id);
            return true;
        } else {
            System.err.println("删除用户失败：用户ID=" + id + "，可能该用户不存在");
            return false;
        }
    }

    /**
     * 根据用户ID查询用户
     * 查询结果会关联会员等级表，同时返回会员等级名称。
     *
     * @param id 用户ID
     * @return 匹配的User对象，如果未找到则返回null
     */
    public User getUserById(Integer id) {
        if (id == null) {
            System.err.println("查询用户失败：用户ID不能为空");
            return null;
        }
        return userDao.findById(id);
    }

    /**
     * 根据用户名查询用户
     * 查询结果会关联会员等级表，同时返回会员等级名称。
     *
     * @param username 用户名
     * @return 匹配的User对象，如果未找到则返回null
     */
    public User getUserByUsername(String username) {
        if (ValidationUtil.isEmpty(username)) {
            System.err.println("查询用户失败：用户名不能为空");
            return null;
        }
        return userDao.findByUsername(username.trim());
    }

    /**
     * 获取所有用户列表
     * 返回系统中所有用户的完整列表，关联会员等级表以获取等级名称。
     *
     * @return 包含所有用户的List集合，如果没有用户则返回空列表
     */
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    /**
     * 模糊搜索用户
     * 根据关键词在用户名、真实姓名、手机号三个字段中进行模糊匹配查询。
     * 如果关键词为null或空字符串，则返回所有用户。
     *
     * @param keyword 搜索关键词
     * @return 匹配的用户列表，如果没有匹配结果则返回空列表
     */
    public List<User> searchUsers(String keyword) {
        if (ValidationUtil.isEmpty(keyword)) {
            return userDao.findAll();
        }
        return userDao.searchByKeyword(keyword.trim());
    }

    /**
     * 分页查询用户（支持关键词搜索）
     * 根据页码和每页大小返回对应页的用户数据，支持按关键词进行模糊过滤。
     * 查询结果会关联会员等级表，同时返回会员等级名称。
     *
     * @param pageNum  页码，从1开始
     * @param pageSize 每页显示的记录数
     * @param keyword  搜索关键词，可以为null表示不进行过滤
     * @return 当前页的用户列表，如果没有数据则返回空列表
     */
    public List<User> getUsersByPage(int pageNum, int pageSize, String keyword) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 10;
        }
        return userDao.findByPage(pageNum, pageSize, keyword);
    }

    /**
     * 获取匹配关键词的用户总数
     * 用于分页查询时计算总页数，返回符合条件的用户记录总条数。
     *
     * @param keyword 搜索关键词，可以为null表示统计所有用户
     * @return 符合条件的用户总数量
     */
    public int getTotalCount(String keyword) {
        return userDao.getTotalCount(keyword);
    }

    /**
     * 用户账户充值
     * 对指定用户的账户余额进行充值操作，同时生成一条充值记录存入数据库。
     * 操作流程：先获取用户当前余额，计算充值后的新余额，
     * 更新用户余额，然后创建充值记录。
     *
     * @param userId       待充值的用户ID
     * @param amount       充值金额，必须大于0
     * @param operatorName 操作员名称，用于记录在充值记录中
     * @return 充值成功返回true，失败返回false
     */
    public boolean recharge(Integer userId, BigDecimal amount, String operatorName) {
        if (userId == null) {
            System.err.println("充值失败：用户ID不能为空");
            return false;
        }

        if (!ValidationUtil.isValidAmount(amount)) {
            System.err.println("充值失败：充值金额必须大于0，当前金额=" + amount);
            return false;
        }

        User user = userDao.findById(userId);
        if (user == null) {
            System.err.println("充值失败：用户ID [" + userId + "] 对应的用户不存在");
            return false;
        }

        BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(amount);

        int updateResult = userDao.updateBalance(userId, newBalance);
        if (updateResult <= 0) {
            System.err.println("充值失败：更新用户余额时发生数据库异常，用户ID=" + userId);
            return false;
        }

        RechargeRecord record = new RechargeRecord();
        record.setUserId(userId);
        record.setUsername(user.getUsername());
        record.setAmount(amount);
        record.setRechargeTime(DateUtil.getCurrentDateTime());
        record.setOperatorName(operatorName);

        Integer recordId = rechargeRecordDao.insert(record);
        if (recordId == null || recordId <= 0) {
            System.err.println("警告：用户余额已更新，但充值记录创建失败，用户ID=" + userId + "，充值金额=" + amount);
            return false;
        }

        System.out.println("充值成功：用户 [" + user.getUsername() + "] 充值 " + amount + " 元，"
                + "操作员=" + operatorName + "，充值后余额=" + newBalance);
        return true;
    }
}