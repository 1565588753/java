package com.internetcafe.service;

import com.internetcafe.dao.ConsumeRecordDao;
import com.internetcafe.dao.OnlineRecordDao;
import com.internetcafe.dao.SystemConfigDao;
import com.internetcafe.dao.UserDao;
import com.internetcafe.dao.VipLevelDao;
import com.internetcafe.entity.ConsumeRecord;
import com.internetcafe.entity.OnlineRecord;
import com.internetcafe.entity.User;
import com.internetcafe.entity.VipLevel;
import com.internetcafe.util.DBUtil;
import com.internetcafe.util.DateUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.Calendar;
import java.util.List;

/**
 * 计费服务类
 * 网吧系统的核心业务服务，负责管理用户上机、下机、费用计算等计费相关操作。
 * 本类封装了所有计费业务逻辑，协调多个DAO层完成数据持久化，
 * 确保计费流程的原子性和数据一致性。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class ChargeService {

    /** 上机记录数据访问对象，负责 online_record 表的数据库操作 */
    private OnlineRecordDao onlineRecordDao;

    /** 用户数据访问对象，负责 user 表的数据库操作 */
    private UserDao userDao;

    /** 消费记录数据访问对象，负责 consume_record 表的数据库操作 */
    private ConsumeRecordDao consumeRecordDao;

    /** 会员等级数据访问对象，负责 vip_level 表的数据库操作 */
    private VipLevelDao vipLevelDao;

    /** 系统配置数据访问对象，负责 system_config 表的数据库操作 */
    private SystemConfigDao systemConfigDao;

    private LogService logService;

    /**
     * 标准每小时单价（元）
     * 所有计费以此为基础，再根据会员等级折扣进行计算
     */
    public static final BigDecimal NORMAL_PRICE_PER_HOUR = new BigDecimal("5.00");

    /**
     * 高峰时段每小时单价（元）
     * 每日 18:00 - 24:00 为高峰时段，采用较高费率
     */
    public static final BigDecimal PEAK_PRICE_PER_HOUR = new BigDecimal("8.00");

    /**
     * 深夜时段每小时单价（元）
     * 每日 00:00 - 08:00 为深夜时段，采用优惠费率
     */
    public static final BigDecimal NIGHT_PRICE_PER_HOUR = new BigDecimal("3.00");

    /**
     * 定时器扫描间隔（毫秒）
     * 用于定时检查余额不足的上机用户，默认每秒检查一次
     */
    public static final int TIMER_INTERVAL = 1000;

    /**
     * 构造方法
     * 初始化所有数据访问对象实例，为后续业务操作做好准备。
     */
    public ChargeService() {
        this.onlineRecordDao = new OnlineRecordDao();
        this.userDao = new UserDao();
        this.consumeRecordDao = new ConsumeRecordDao();
        this.vipLevelDao = new VipLevelDao();
        this.systemConfigDao = new SystemConfigDao();
        this.logService = new LogService();
    }

    /**
     * 开始上机 —— 为用户开启一个计费会话
     * 执行以下校验步骤：
     * 1. 检查用户是否存在且状态为正常（status=1）
     * 2. 检查用户当前是否已经在上机中（不允许重复上机）
     * 3. 检查指定机器是否已被其他用户占用
     * 4. 检查用户账户余额是否大于0
     * 5. 所有校验通过后，创建一条新的上机记录并返回记录ID
     *
     * @param userId    要上机的用户ID
     * @param machineNo 用户使用的机器编号
     * @return 新创建的上机记录ID
     * @throws RuntimeException 当校验不通过时抛出异常，异常消息描述具体原因：
     *                          "用户不存在" - 用户ID在数据库中不存在
     *                          "用户已被禁用，无法上机" - 用户状态不是正常状态
     *                          "该用户正在上机中，无法重复上机" - 用户已有活跃的上机记录
     *                          "该机器已被占用" - 指定机器已有其他用户在上机
     *                          "账户余额不足，请先充值" - 用户余额为0或负数
     *                          "创建上机记录失败" - 数据库插入操作失败
     */
    public Integer startOnline(Integer userId, String machineNo) {
        try {
            User user = userDao.findById(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            if (user.getStatus() == null || user.getStatus() != 1) {
                throw new RuntimeException("用户已被禁用，无法上机");
            }

            OnlineRecord activeRecord = onlineRecordDao.findActiveByUserId(userId);
            if (activeRecord != null) {
                throw new RuntimeException("该用户正在上机中，无法重复上机");
            }

            OnlineRecord machineRecord = onlineRecordDao.findActiveByMachineNo(machineNo);
            if (machineRecord != null) {
                throw new RuntimeException("该机器已被占用");
            }

            if (user.getBalance() == null || user.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("账户余额不足，请先充值");
            }

            OnlineRecord record = new OnlineRecord();
            record.setUserId(userId);
            record.setMachineNo(machineNo);
            record.setLoginTime(DateUtil.getCurrentDateTime());
            record.setDuration(0L);
            record.setCost(BigDecimal.ZERO);
            record.setStatus(1);

            Integer recordId = onlineRecordDao.insert(record);
            if (recordId == null || recordId == -1) {
                throw new RuntimeException("创建上机记录失败");
            }

            return recordId;
        } catch (RuntimeException e) {
            logService.addErrorLog("SYSTEM", "开始上机失败: userId=" + userId + ", machineNo=" + machineNo + ", " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logService.addErrorLog("SYSTEM", "开始上机异常: userId=" + userId + ", machineNo=" + machineNo + ", " + e.getMessage());
            throw new RuntimeException("数据库连接失败: " + e.getMessage());
        }
    }

    /**
     * 停止上机 —— 结束计费并进行结算
     * 执行以下步骤完成下机结算流程：
     * 1. 根据记录ID获取上机记录
     * 2. 计算上机时长（从登录时间到当前时间的分钟数）
     * 3. 根据用户会员等级计算本次上机费用
     * 4. 检查用户余额是否足够支付，如果不足则按实际余额调整费用
     * 5. 更新上机记录：设置下机时间、时长、费用，将状态改为已下机（status=2）
     * 6. 从用户账户余额中扣除本次消费金额
     * 7. 创建一条消费记录用于对账
     * 8. 为用户增加积分（每消费1元获得1积分）
     *
     * @param recordId 要结束的上机记录ID
     * @return 本次上机的实际消费金额
     * @throws RuntimeException 当结算过程中出现异常时抛出，异常消息描述具体原因：
     *                          "上机记录不存在" - 指定的记录ID在数据库中不存在
     *                          "上机记录结算失败" - 更新上机记录状态失败
     *                          "余额扣款失败" - 更新用户余额失败
     */
    public BigDecimal stopOnline(Integer recordId) {
        OnlineRecord record = onlineRecordDao.findById(recordId);
        if (record == null) {
            throw new RuntimeException("上机记录不存在");
        }

        String now = DateUtil.getCurrentDateTime();
        long durationMinutes = DateUtil.getMinutesBetween(record.getLoginTime(), now);
        if (durationMinutes < 1) {
            durationMinutes = 1;
        }

        BigDecimal cost = calculateFee(record.getUserId(), durationMinutes);

        User user = userDao.findById(record.getUserId());
        if (user != null) {
            BigDecimal balance = user.getBalance();
            if (balance == null || balance.compareTo(cost) < 0) {
                cost = balance != null ? balance : BigDecimal.ZERO;
            }
        }

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                throw new RuntimeException("无法获取数据库连接");
            }
            DBUtil.beginTransaction(conn);

            boolean updateResult = onlineRecordDao.stopOnline(recordId, now, durationMinutes, cost);
            if (!updateResult) {
                DBUtil.rollbackTransaction(conn);
                throw new RuntimeException("上机记录结算失败");
            }

            if (user != null) {
                BigDecimal newBalance = user.getBalance().subtract(cost);
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    newBalance = BigDecimal.ZERO;
                }
                int result = userDao.updateBalance(record.getUserId(), newBalance);
                if (result <= 0) {
                    DBUtil.rollbackTransaction(conn);
                    throw new RuntimeException("余额扣款失败");
                }
            }

            ConsumeRecord consumeRecord = new ConsumeRecord();
            consumeRecord.setUserId(record.getUserId());
            consumeRecord.setAmount(cost);
            consumeRecord.setConsumeType("上机消费");
            consumeRecord.setCreateTime(now);
            consumeRecordDao.insert(consumeRecord);

            if (user != null && cost.compareTo(BigDecimal.ZERO) > 0) {
                int earnedPoints = cost.setScale(0, RoundingMode.DOWN).intValue();
                if (earnedPoints > 0) {
                    int newPoints = (user.getPoints() != null ? user.getPoints() : 0) + earnedPoints;
                    userDao.updatePoints(record.getUserId(), newPoints);
                }
            }

            DBUtil.commitTransaction(conn);
            logService.addOperationLog("SYSTEM", "下机结算",
                    "下机结算完成: recordId=" + recordId + ", userId=" + record.getUserId() + ", cost=" + cost);
            return cost;
        } catch (RuntimeException e) {
            logService.addErrorLog("SYSTEM", "下机结算失败: recordId=" + recordId + ", " + e.getMessage());
            throw e;
        } catch (Exception e) {
            DBUtil.rollbackTransaction(conn);
            logService.addErrorLog("SYSTEM", "下机结算异常: recordId=" + recordId + ", " + e.getMessage());
            throw new RuntimeException("结算异常：" + e.getMessage());
        } finally {
            DBUtil.closeAll(conn, null);
        }
    }

    /**
     * 计算上机费用（含时段费率 + 会员折扣 + 积分折扣）
     *
     * 计费规则：
     * 1. 根据当前时间确定基础单价（深夜/普通/高峰）
     * 2. 应用会员等级折扣率
     * 3. 应用积分兑换的额外折扣
     *
     * @param userId          用户ID
     * @param durationMinutes 上机时长（分钟）
     * @return 计算后的实际费用（元），保留两位小数
     */
    public BigDecimal calculateFee(Integer userId, Long durationMinutes) {
        User user = userDao.findById(userId);
        if (user == null) {
            return calculateNormalFee(durationMinutes);
        }

        BigDecimal basePricePerHour = getCurrentTimePrice();

        Integer vipLevelId = user.getVipLevel();
        VipLevel vipLevel = null;
        BigDecimal discountRate = new BigDecimal("1.00");

        if (vipLevelId != null) {
            vipLevel = vipLevelDao.findById(vipLevelId);
        }

        if (vipLevel != null && vipLevel.getDiscountRate() != null) {
            discountRate = vipLevel.getDiscountRate();
        }

        BigDecimal pointsDiscount = BigDecimal.ZERO;
        if (user.getPoints() != null && user.getPoints() > 0) {
            pointsDiscount = new VipService().getPointsDiscount(user.getPoints());
        }

        BigDecimal effectiveDiscount = discountRate.subtract(pointsDiscount);
        if (effectiveDiscount.compareTo(new BigDecimal("0.50")) < 0) {
            effectiveDiscount = new BigDecimal("0.50");
        }

        BigDecimal hours = new BigDecimal(durationMinutes)
                .divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);

        BigDecimal fee = basePricePerHour
                .multiply(hours)
                .multiply(effectiveDiscount)
                .setScale(2, RoundingMode.HALF_UP);

        return fee;
    }

    /**
     * 根据当前时间获取对应时段的基础单价
     * 深夜时段 00:00-08:00 → 3元/小时
     * 普通时段 08:00-18:00 → 5元/小时
     * 高峰时段 18:00-24:00 → 8元/小时
     */
    private BigDecimal getCurrentTimePrice() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 0 && hour < 8) {
            return getNightPrice();
        } else if (hour >= 18 && hour < 24) {
            return getPeakPrice();
        } else {
            return getBasePrice();
        }
    }

    public BigDecimal getBasePrice() {
        String value = systemConfigDao.getConfig("base_price");
        if (value != null) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                System.err.println("基础价格配置值无效: " + value);
            }
        }
        return NORMAL_PRICE_PER_HOUR;
    }

    public BigDecimal getPeakPrice() {
        String value = systemConfigDao.getConfig("peak_price");
        if (value != null) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                System.err.println("高峰价格配置值无效: " + value);
            }
        }
        return PEAK_PRICE_PER_HOUR;
    }

    public BigDecimal getNightPrice() {
        String value = systemConfigDao.getConfig("night_price");
        if (value != null) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                System.err.println("深夜价格配置值无效: " + value);
            }
        }
        return NIGHT_PRICE_PER_HOUR;
    }

    /**
     * 按标准价格计算费用（不使用任何折扣）
     * 用于用户不存在或会员等级信息获取失败时的兜底计费。
     *
     * @param durationMinutes 上机时长（分钟）
     * @return 标准费用（元），保留两位小数
     */
    private BigDecimal calculateNormalFee(Long durationMinutes) {
        BigDecimal hours = new BigDecimal(durationMinutes)
                .divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);

        return getBasePrice()
                .multiply(hours)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取所有当前正在上机中的记录
     * 查询状态为1（上机中）的所有记录，用于管理员查看当前网吧上机情况。
     *
     * @return 所有活跃上机记录列表，如果没有则返回空列表
     */
    public List<OnlineRecord> getActiveRecords() {
        return onlineRecordDao.findByStatus(1);
    }

    /**
     * 根据记录ID查询上机记录
     *
     * @param recordId 记录ID
     * @return 上机记录，不存在返回null
     */
    public OnlineRecord getRecordById(Integer recordId) {
        return onlineRecordDao.findById(recordId);
    }

    /**
     * 根据用户ID查询该用户的所有上机记录
     * 包括历史记录和当前活跃记录，按上机时间降序排列。
     *
     * @param userId 用户ID
     * @return 该用户的所有上机记录列表，如果没有则返回空列表
     */
    public List<OnlineRecord> getRecordsByUserId(Integer userId) {
        return onlineRecordDao.findByUserId(userId);
    }

    /**
     * 检查指定用户当前是否正在上机中
     * 通过查询该用户是否存在状态为1（上机中）的活跃记录来判断。
     *
     * @param userId 用户ID
     * @return true表示用户正在上机中，false表示用户未上机
     */
    public boolean checkUserOnline(Integer userId) {
        OnlineRecord activeRecord = onlineRecordDao.findActiveByUserId(userId);
        return activeRecord != null;
    }

    /**
     * 自动停止余额不足的用户上机
     * 遍历所有当前正在上机的用户，检查其账户余额。
     * 对于余额已不足支付当前计费周期费用的用户，自动执行下机操作。
     * 此方法通常由定时器（Timer）周期性调用，调用间隔由 TIMER_INTERVAL 常量定义。
     * 例如每秒钟检查一次，确保余额不足的用户能被及时断网。
     */
    public void autoStopLowBalance() {
        List<OnlineRecord> activeRecords = getActiveRecords();

        for (OnlineRecord record : activeRecords) {
            try {
                User user = userDao.findById(record.getUserId());
                if (user == null) {
                    continue;
                }

                if (user.getBalance() == null || user.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                    stopOnline(record.getId());
                    System.out.println("自动下机：用户ID=" + record.getUserId()
                            + "，上机记录ID=" + record.getId() + "，原因：余额不足");
                    logService.addOperationLog("SYSTEM", "自动下机",
                            "余额不足自动下机: userId=" + record.getUserId() + ", recordId=" + record.getId());
                }
            } catch (Exception e) {
                System.err.println("自动检查余额时发生异常，上机记录ID=" + record.getId()
                        + "：" + e.getMessage());
                logService.addErrorLog("SYSTEM", "自动余额检查异常: recordId=" + record.getId() + ", " + e.getMessage());
            }
        }
    }

    /**
     * 断点续计 —— 系统异常重启后恢复异常中断的上机记录
     *
     * 恢复策略：
     * 1. 查找所有状态为1（上机中）或3（异常下机）的记录
     * 2. 对于异常下机的记录，自动执行结算操作
     * 3. 对于仍在"上机中"的记录，保持状态不变继续计费
     *
     * @return 恢复的上机记录数量
     */
    public int recoverInterruptedSessions() {
        int recoveredCount = 0;
        List<OnlineRecord> interruptedRecords = onlineRecordDao.findByStatus(3);

        for (OnlineRecord record : interruptedRecords) {
            try {
                stopOnline(record.getId());
                recoveredCount++;
                System.out.println("断点续计：已结算异常下机记录，记录ID=" + record.getId()
                        + "，用户ID=" + record.getUserId());
            } catch (Exception e) {
                System.err.println("断点续计失败：记录ID=" + record.getId()
                        + "，" + e.getMessage());
            }
        }

        List<OnlineRecord> activeRecords = onlineRecordDao.findByStatus(1);
        if (activeRecords != null && !activeRecords.isEmpty()) {
            System.out.println("发现 " + activeRecords.size() + " 条活跃上机记录，将恢复计费监控");
            recoveredCount += activeRecords.size();
        }

        return recoveredCount;
    }
}