package com.internetcafe.service;

import com.internetcafe.dao.UserDao;
import com.internetcafe.dao.VipLevelDao;
import com.internetcafe.entity.User;
import com.internetcafe.entity.VipLevel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 会员服务类
 * 负责管理网吧会员等级体系相关的所有业务操作，
 * 包括会员等级查询、折扣率获取、会员升级、积分管理等。
 * 本类封装了会员业务的核心逻辑，作为控制器层与数据访问层之间的桥梁。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class VipService {

    /** 会员等级数据访问对象，负责 vip_level 表的数据库操作 */
    private VipLevelDao vipLevelDao;

    /** 用户数据访问对象，负责 user 表的数据库操作 */
    private UserDao userDao;

    /**
     * 构造方法
     * 初始化会员等级和用户的数据访问对象实例。
     */
    public VipService() {
        this.vipLevelDao = new VipLevelDao();
        this.userDao = new UserDao();
    }

    /**
     * 获取系统中所有的会员等级
     * 返回按ID升序排列的全部会员等级列表，用于会员等级管理页面展示。
     * 每个会员等级包含等级名称、折扣率、等级描述等信息。
     *
     * @return 所有会员等级的列表，如果没有数据则返回空列表
     */
    public List<VipLevel> getAllVipLevels() {
        return vipLevelDao.findAll();
    }

    /**
     * 根据会员等级ID获取单个会员等级信息
     * 通过主键ID精确查找对应的会员等级详情。
     * 常用于查看某个等级的折扣率、描述等具体信息。
     *
     * @param id 会员等级ID
     * @return 匹配的会员等级对象，如果未找到则返回 null
     */
    public VipLevel getVipLevelById(Integer id) {
        if (id == null) {
            return null;
        }
        return vipLevelDao.findById(id);
    }

    /**
     * 根据会员等级ID获取对应的折扣率
     * 折扣率决定了该等级会员在上机消费时可以享受的价格优惠。
     * 例如：折扣率为0.80表示享受8折优惠（即按标准价格的80%计费）。
     *
     * 返回值的含义：
     * 1.00 = 原价（无折扣）
     * 0.90 = 9折
     * 0.80 = 8折
     * 以此类推...
     *
     * @param vipLevelId 会员等级ID
     * @return 该会员等级对应的折扣率（BigDecimal），如果等级不存在或折扣率为空则返回 1.00（即不打折）
     */
    public BigDecimal getDiscountRate(Integer vipLevelId) {
        if (vipLevelId == null) {
            /* 未指定会员等级时，默认不打折 */
            return new BigDecimal("1.00");
        }

        VipLevel vipLevel = vipLevelDao.findById(vipLevelId);
        if (vipLevel == null) {
            /* 会员等级不存在时，默认不打折 */
            return new BigDecimal("1.00");
        }

        if (vipLevel.getDiscountRate() == null) {
            /* 折扣率为空时，默认不打折 */
            return new BigDecimal("1.00");
        }

        return vipLevel.getDiscountRate();
    }

    /**
     * 升级用户会员等级
     * 将指定用户的会员等级修改为新的等级。
     * 执行流程：
     * 1. 验证用户是否存在
     * 2. 验证新的会员等级是否存在
     * 3. 验证新等级与当前等级是否相同（相同则无需升级）
     * 4. 更新用户的会员等级字段
     * 5. 如果更新失败则抛出异常
     *
     * @param userId      要升级的用户ID
     * @param newLevelId  新的会员等级ID
     * @throws RuntimeException 当升级过程中出现异常时抛出，异常消息描述具体原因：
     *                          "用户不存在" - 指定的用户ID在数据库中不存在
     *                          "会员等级不存在" - 指定的会员等级ID在数据库中不存在
     *                          "用户当前已是该等级，无需升级" - 新旧等级相同
     *                          "会员等级升级失败" - 数据库更新操作失败
     */
    public void upgradeVipLevel(Integer userId, Integer newLevelId) {
        /* 第一步：验证用户是否存在 */
        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        /* 第二步：验证新的会员等级是否存在 */
        VipLevel newLevel = vipLevelDao.findById(newLevelId);
        if (newLevel == null) {
            throw new RuntimeException("会员等级不存在");
        }

        /* 第三步：检查是否与当前等级相同，相同则无需升级 */
        if (user.getVipLevel() != null && user.getVipLevel().equals(newLevelId)) {
            throw new RuntimeException("用户当前已是该等级，无需升级");
        }

        /* 第四步：执行会员等级更新 */
        int result = userDao.updateVipLevel(userId, newLevelId);
        if (result <= 0) {
            throw new RuntimeException("会员等级升级失败");
        }
    }

    /**
     * 为用户增加积分
     * 将指定数量的积分累加到用户当前的积分余额中。
     * 积分可用于兑换优惠或参与会员活动，是网吧会员体系的激励手段。
     *
     * 执行流程：
     * 1. 验证用户是否存在
     * 2. 获取用户当前积分
     * 3. 计算新的积分总数（当前积分 + 新增积分）
     * 4. 更新用户积分字段
     * 5. 如果更新失败则抛出异常
     *
     * @param userId 用户ID
     * @param points 要增加的积分数量（必须为正整数）
     * @throws RuntimeException 当操作过程中出现异常时抛出，异常消息描述具体原因：
     *                          "用户不存在" - 指定的用户ID在数据库中不存在
     *                          "增加积分失败" - 数据库更新操作失败
     */
    public void addPoints(Integer userId, Integer points) {
        /* 第一步：验证用户是否存在 */
        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        /* 第二步：计算新的积分总数 */
        int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
        int newPoints = currentPoints + points;

        /* 第三步：执行积分更新 */
        int result = userDao.updatePoints(userId, newPoints);
        if (result <= 0) {
            throw new RuntimeException("增加积分失败");
        }
    }

    /**
     * 根据积分计算可获得的折扣
     * 这是一个可选功能，允许用户使用积分兑换一定比例的消费折扣。
     *
     * 积分兑换规则：
     * - 每100积分可兑换1%的折扣（即0.01的折扣率减免）
     * - 最高可兑换20%的折扣（即最多减免0.20）
     * - 超过2000积分后不再增加折扣额度
     *
     * 例如：
     * 500积分  → 5%折扣  → 折扣减免值为 0.05
     * 1000积分 → 10%折扣 → 折扣减免值为 0.10
     * 2000积分 → 20%折扣 → 折扣减免值为 0.20（封顶）
     * 3000积分 → 20%折扣 → 折扣减免值为 0.20（封顶）
     *
     * @param points 用户当前的积分数量
     * @return 可获得的折扣减免值（范围：0.00 ~ 0.20），保留两位小数
     */
    public BigDecimal getPointsDiscount(Integer points) {
        if (points == null || points <= 0) {
            /* 积分为0或负数时，无折扣 */
            return BigDecimal.ZERO;
        }

        /* 每100积分可兑换0.01（即1%）的折扣减免 */
        BigDecimal discount = new BigDecimal(points)
                .divide(new BigDecimal("100"), 0, RoundingMode.DOWN)
                .multiply(new BigDecimal("0.01"));

        /* 折扣封顶：最高不超过0.20（即20%） */
        BigDecimal maxDiscount = new BigDecimal("0.20");
        if (discount.compareTo(maxDiscount) > 0) {
            discount = maxDiscount;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }
}