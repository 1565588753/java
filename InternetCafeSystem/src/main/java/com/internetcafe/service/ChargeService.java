package com.internetcafe.service;

import com.internetcafe.dao.ConsumeRecordDao;
import com.internetcafe.dao.OnlineRecordDao;
import com.internetcafe.dao.UserDao;
import com.internetcafe.dao.VipLevelDao;
import com.internetcafe.entity.ConsumeRecord;
import com.internetcafe.entity.OnlineRecord;
import com.internetcafe.entity.User;
import com.internetcafe.entity.VipLevel;
import com.internetcafe.util.DateUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    /**
     * 标准每小时单价（元）
     * 所有计费以此为基础，再根据会员等级折扣进行计算
     */
    public static final BigDecimal NORMAL_PRICE_PER_HOUR = new BigDecimal("5.00");

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
        /* 第一步：检查用户是否存在 */
        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        /* 第二步：检查用户状态是否正常（status=1 表示正常，0 表示禁用） */
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用，无法上机");
        }

        /* 第三步：检查用户是否已经在上机中（每个用户同时只能有一个活跃的上机会话） */
        OnlineRecord activeRecord = onlineRecordDao.findActiveByUserId(userId);
        if (activeRecord != null) {
            throw new RuntimeException("该用户正在上机中，无法重复上机");
        }

        /* 第四步：检查指定机器是否已被占用（每台机器同时只能有一个用户使用） */
        OnlineRecord machineRecord = onlineRecordDao.findActiveByMachineNo(machineNo);
        if (machineRecord != null) {
            throw new RuntimeException("该机器已被占用");
        }

        /* 第五步：检查用户账户余额是否大于0 */
        if (user.getBalance() == null || user.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("账户余额不足，请先充值");
        }

        /* 第六步：创建上机记录 */
        OnlineRecord record = new OnlineRecord();
        record.setUserId(userId);
        record.setMachineNo(machineNo);
        record.setLoginTime(DateUtil.getCurrentDateTime());
        record.setStatus(1); /* 状态：1=上机中 */

        Integer recordId = onlineRecordDao.insert(record);
        if (recordId == null || recordId == -1) {
            throw new RuntimeException("创建上机记录失败");
        }

        return recordId;
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
        /* 第一步：获取上机记录 */
        OnlineRecord record = onlineRecordDao.findById(recordId);
        if (record == null) {
            throw new RuntimeException("上机记录不存在");
        }

        /* 第二步：计算上机时长（分钟），使用DateUtil工具类计算时间差 */
        String now = DateUtil.getCurrentDateTime();
        long durationMinutes = DateUtil.getMinutesBetween(record.getLoginTime(), now);
        /* 最少按1分钟计费，避免0分钟的情况 */
        if (durationMinutes < 1) {
            durationMinutes = 1;
        }

        /* 第三步：根据用户会员等级计算本次费用 */
        BigDecimal cost = calculateFee(record.getUserId(), durationMinutes);

        /* 第四步：检查用户余额是否足够支付 */
        User user = userDao.findById(record.getUserId());
        if (user != null) {
            BigDecimal balance = user.getBalance();
            if (balance == null || balance.compareTo(cost) < 0) {
                /* 余额不足时，按实际余额扣费（最多扣到0） */
                cost = balance != null ? balance : BigDecimal.ZERO;
            }
        }

        /* 第五步：更新上机记录为已下机状态 */
        boolean updateResult = onlineRecordDao.stopOnline(recordId, now, durationMinutes, cost);
        if (!updateResult) {
            throw new RuntimeException("上机记录结算失败");
        }

        /* 第六步：从用户余额中扣除本次消费金额 */
        if (user != null) {
            BigDecimal newBalance = user.getBalance().subtract(cost);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                newBalance = BigDecimal.ZERO;
            }
            int result = userDao.updateBalance(record.getUserId(), newBalance);
            if (result <= 0) {
                throw new RuntimeException("余额扣款失败");
            }
        }

        /* 第七步：创建消费记录 */
        ConsumeRecord consumeRecord = new ConsumeRecord();
        consumeRecord.setUserId(record.getUserId());
        consumeRecord.setAmount(cost);
        consumeRecord.setConsumeType("上机消费");
        consumeRecord.setCreateTime(now);
        consumeRecordDao.insert(consumeRecord);

        /* 第八步：为用户增加积分（每消费1元获得1积分） */
        if (user != null && cost.compareTo(BigDecimal.ZERO) > 0) {
            int earnedPoints = cost.setScale(0, RoundingMode.DOWN).intValue();
            if (earnedPoints > 0) {
                int newPoints = (user.getPoints() != null ? user.getPoints() : 0) + earnedPoints;
                userDao.updatePoints(record.getUserId(), newPoints);
            }
        }

        return cost;
    }

    /**
     * 计算上机费用
     * 根据用户所属的会员等级获取对应的折扣率，然后计算实际费用。
     * 计算公式：标准单价（5元/小时） × 上机时长（小时） × 会员折扣率
     * 使用 BigDecimal 保证金额计算精度，结果四舍五入保留两位小数。
     *
     * @param userId          用户ID
     * @param durationMinutes 上机时长（分钟）
     * @return 计算后的实际费用（元），保留两位小数
     */
    public BigDecimal calculateFee(Integer userId, Long durationMinutes) {
        /* 获取用户信息，确定会员等级 */
        User user = userDao.findById(userId);
        if (user == null) {
            /* 用户不存在时按标准价格计算 */
            return calculateNormalFee(durationMinutes);
        }

        /* 获取用户的会员等级信息 */
        Integer vipLevelId = user.getVipLevel();
        VipLevel vipLevel = null;
        BigDecimal discountRate = new BigDecimal("1.00"); /* 默认不打折 */

        if (vipLevelId != null) {
            vipLevel = vipLevelDao.findById(vipLevelId);
        }

        if (vipLevel != null && vipLevel.getDiscountRate() != null) {
            discountRate = vipLevel.getDiscountRate();
        }

        /* 将分钟转换为小时（使用BigDecimal保证精度） */
        BigDecimal hours = new BigDecimal(durationMinutes)
                .divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);

        /* 计算费用：标准单价 × 小时数 × 折扣率 */
        BigDecimal fee = NORMAL_PRICE_PER_HOUR
                .multiply(hours)
                .multiply(discountRate)
                .setScale(2, RoundingMode.HALF_UP);

        return fee;
    }

    /**
     * 按标准价格计算费用（不使用任何折扣）
     * 用于用户不存在或会员等级信息获取失败时的兜底计费。
     *
     * @param durationMinutes 上机时长（分钟）
     * @return 标准费用（元），保留两位小数
     */
    private BigDecimal calculateNormalFee(Long durationMinutes) {
        /* 将分钟转换为小时 */
        BigDecimal hours = new BigDecimal(durationMinutes)
                .divide(new BigDecimal("60"), 4, RoundingMode.HALF_UP);

        /* 标准价格 × 小时数 */
        return NORMAL_PRICE_PER_HOUR
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
        /* 获取所有当前正在上机的记录 */
        List<OnlineRecord> activeRecords = getActiveRecords();

        for (OnlineRecord record : activeRecords) {
            try {
                /* 获取用户信息并检查余额 */
                User user = userDao.findById(record.getUserId());
                if (user == null) {
                    continue;
                }

                /* 如果用户余额小于等于0，自动执行下机结算 */
                if (user.getBalance() == null || user.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                    stopOnline(record.getId());
                    System.out.println("自动下机：用户ID=" + record.getUserId()
                            + "，上机记录ID=" + record.getId() + "，原因：余额不足");
                }
            } catch (Exception e) {
                /* 单条记录处理失败不影响其他记录的检查 */
                System.err.println("自动检查余额时发生异常，上机记录ID=" + record.getId()
                        + "：" + e.getMessage());
            }
        }
    }
}