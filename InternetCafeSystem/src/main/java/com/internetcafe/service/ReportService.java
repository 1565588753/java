package com.internetcafe.service;

import com.internetcafe.dao.ConsumeRecordDao;
import com.internetcafe.dao.OnlineRecordDao;
import com.internetcafe.dao.RechargeRecordDao;
import com.internetcafe.dao.UserDao;
import com.internetcafe.entity.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表统计服务类
 * 负责网吧运营数据的统计分析，包括营收统计、用户统计、排行榜和会员分布等功能。
 * 本服务类作为业务逻辑层，封装对各个 DAO 的调用，为上层（如控制器或UI）提供统一的数据接口。
 * 所有统计方法均直接委托给对应的 DAO 完成数据库查询，
 * 对于需要二次加工的数据（如会员分布统计、月度统计），在本层进行聚合处理。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class ReportService {

    /** 消费记录数据访问对象，用于消费相关的统计查询 */
    private ConsumeRecordDao consumeRecordDao;

    /** 充值记录数据访问对象，用于充值相关的统计查询 */
    private RechargeRecordDao rechargeRecordDao;

    /** 上机记录数据访问对象，用于上机次数等统计查询 */
    private OnlineRecordDao onlineRecordDao;

    /** 用户数据访问对象，用于用户数量和状态相关的统计查询 */
    private UserDao userDao;

    /**
     * 无参构造方法
     * 初始化所有 DAO 实例，为后续的统计方法调用做好准备。
     */
    public ReportService() {
        this.consumeRecordDao = new ConsumeRecordDao();
        this.rechargeRecordDao = new RechargeRecordDao();
        this.onlineRecordDao = new OnlineRecordDao();
        this.userDao = new UserDao();
    }

    /**
     * 获取今日营收总额
     * 统计当天所有消费记录的金额总和，反映当日网吧的营业收入情况。
     * 该方法委托 ConsumeRecordDao.getTodayTotal() 完成数据库层面的日期筛选与金额聚合。
     *
     * @return 今日消费总额（BigDecimal），如果今天没有任何消费记录则返回 BigDecimal.ZERO
     */
    public BigDecimal getTodayRevenue() {
        return consumeRecordDao.getTodayTotal();
    }

    /**
     * 获取本月营收总额
     * 统计当前月份（从当月1日到今天）所有消费记录的金额总和。
     * 该方法委托 ConsumeRecordDao.getMonthTotal() 完成数据库层面的月份筛选与金额聚合。
     *
     * @return 本月消费总额（BigDecimal），如果本月没有任何消费记录则返回 BigDecimal.ZERO
     */
    public BigDecimal getMonthRevenue() {
        return consumeRecordDao.getMonthTotal();
    }

    /**
     * 获取今日充值总额
     * 统计当天所有充值记录的金额总和，反映当日网吧的充值收入情况。
     * 该方法委托 RechargeRecordDao.getTodayTotal() 完成数据库层面的日期筛选与金额聚合。
     *
     * @return 今日充值总额（BigDecimal），如果今天没有任何充值记录则返回 BigDecimal.ZERO
     */
    public BigDecimal getTodayRecharge() {
        return rechargeRecordDao.getTodayTotal();
    }

    /**
     * 获取本月充值总额
     * 统计当前月份所有充值记录的金额总和。
     * 该方法委托 RechargeRecordDao.getMonthTotal() 完成数据库层面的月份筛选与金额聚合。
     *
     * @return 本月充值总额（BigDecimal），如果本月没有任何充值记录则返回 BigDecimal.ZERO
     */
    public BigDecimal getMonthRecharge() {
        return rechargeRecordDao.getMonthTotal();
    }

    /**
     * 获取今日上机人次
     * 统计当天所有上机记录的总条数，反映当日网吧的上机客流量。
     * 该方法委托 OnlineRecordDao.getTodayOnlineCount() 完成数据库层面的统计。
     *
     * @return 今日上机总次数（int），如果今天没有任何上机记录则返回 0
     */
    public int getTodayOnlineCount() {
        return onlineRecordDao.getTodayOnlineCount();
    }

    /**
     * 获取系统用户总数
     * 统计 user 表中所有用户的总数量（包括正常状态和禁用状态的用户）。
     * 该方法通过调用 UserDao.getTotalCount(null) 来获取全部用户的总数。
     *
     * @return 系统中所有用户的总数（int），查询失败时返回 0
     */
    public int getTotalUsers() {
        return userDao.getTotalCount(null);
    }

    /**
     * 获取活跃用户数量
     * 统计 user 表中状态为"正常"（status = 1）的用户数量。
     * 由于 UserDao 没有直接按状态统计的方法，此处先调用 findAll() 获取所有用户，
     * 然后在内存中遍历过滤出 status 等于 1 的用户进行计数。
     * 注意：当用户数量非常大时，此方式可能存在性能问题，建议后续在 UserDao 中增加按状态计数的方法。
     *
     * @return 状态为正常（status = 1）的活跃用户数量（int），查询失败时返回 0
     */
    public int getActiveUsers() {
        List<User> allUsers = userDao.findAll();
        if (allUsers == null || allUsers.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (User user : allUsers) {
            if (user.getStatus() != null && user.getStatus() == 1) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取用户消费排行榜
     * 按消费总额降序排列，展示每个用户的消费总金额和消费次数。
     * 该方法委托 ConsumeRecordDao.getUserConsumeRanking() 完成数据库层面的聚合查询，
     * 返回的结果集中每个 Map 包含以下键：
     *   - "username"      : 用户名（String）
     *   - "totalAmount"   : 消费总金额（BigDecimal）
     *   - "consumeCount"  : 消费次数（Long）
     *
     * @return 包含排行榜数据的 List&lt;Map&lt;String, Object&gt;&gt;，按消费总额从高到低排列
     */
    public List<Map<String, Object>> getUserConsumeRanking() {
        return consumeRecordDao.getUserConsumeRanking();
    }

    /**
     * 获取会员等级分布统计
     * 统计各个会员等级下的用户数量，用于分析网吧会员结构。
     * 通过查询所有用户，然后按 vipLevelName（会员等级名称）进行分组计数。
     * 使用 LinkedHashMap 保持插入顺序，确保结果有序。
     * 每个结果 Map 包含以下键：
     *   - "levelName" : 会员等级名称（String），例如 "普通会员"、"黄金会员" 等
     *   - "userCount" : 该等级下的用户数量（Integer）
     *
     * @return 包含各等级分布数据的 List&lt;Map&lt;String, Object&gt;&gt;
     */
    public List<Map<String, Object>> getVipDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();

        List<User> allUsers = userDao.findAll();
        if (allUsers == null || allUsers.isEmpty()) {
            return result;
        }

        Map<String, Integer> distributionMap = new LinkedHashMap<>();

        for (User user : allUsers) {
            String levelName = user.getVipLevelName();
            if (levelName == null || levelName.isEmpty()) {
                levelName = "未知等级";
            }
            distributionMap.put(levelName, distributionMap.getOrDefault(levelName, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : distributionMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("levelName", entry.getKey());
            map.put("userCount", entry.getValue());
            result.add(map);
        }

        return result;
    }

    /**
     * 获取指定年份的月度营收统计
     * 按月份统计指定年份中每个月的消费总额，用于生成年度营收趋势图或报表。
     * 该方法逐月调用 ConsumeRecordDao.getTotalByDateRange() 查询每个月的消费总额，
     * 将结果封装为包含月份编号和金额的 Map 列表。
     * 每个结果 Map 包含以下键：
     *   - "month"  : 月份编号（Integer），取值范围为 1 到 12
     *   - "amount" : 该月的消费总额（BigDecimal）
     *
     * @param year 要统计的年份，例如 2025
     * @return 包含1月至12月营收数据的 List&lt;Map&lt;String, Object&gt;&gt;，顺序从1月到12月
     */
    public List<Map<String, Object>> getMonthlyStats(int year) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            String startDate = String.format("%d-%02d-01", year, month);

            int lastDay;
            switch (month) {
                case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                    lastDay = 31;
                    break;
                case 4: case 6: case 9: case 11:
                    lastDay = 30;
                    break;
                case 2:
                    boolean isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
                    lastDay = isLeapYear ? 29 : 28;
                    break;
                default:
                    lastDay = 30;
                    break;
            }

            String endDate = String.format("%d-%02d-%02d", year, month, lastDay);

            BigDecimal monthlyAmount = consumeRecordDao.getTotalByDateRange(startDate, endDate);

            Map<String, Object> map = new HashMap<>();
            map.put("month", month);
            map.put("amount", monthlyAmount);
            result.add(map);
        }

        return result;
    }
}