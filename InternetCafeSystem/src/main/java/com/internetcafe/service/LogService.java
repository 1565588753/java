package com.internetcafe.service;

import com.internetcafe.dao.SystemLogDao;
import com.internetcafe.entity.SystemLog;
import com.internetcafe.util.DateUtil;

import java.util.List;

/**
 * 系统日志服务类
 * 负责系统操作日志的记录与查询管理，为系统提供统一的操作审计功能。
 * 本服务类封装了对 SystemLogDao 的调用，提供了便捷的日志记录方法（如登录日志、删除日志等），
 * 以及多种维度的日志查询方法（按操作人、按操作类型、按日期范围等）。
 * 日志的创建时间由本服务类通过 DateUtil.getCurrentDateTime() 自动生成，
 * 确保时间格式的一致性和准确性。
 *
 * @author InternetCafeSystem
 * @version 1.0
 */
public class LogService {

    /** 系统日志数据访问对象，负责日志的数据库持久化操作 */
    private SystemLogDao systemLogDao;

    /**
     * 无参构造方法
     * 初始化 SystemLogDao 实例，为后续的日志记录和查询操作做好准备。
     */
    public LogService() {
        this.systemLogDao = new SystemLogDao();
    }

    /**
     * 添加一条系统操作日志（通用方法）
     * 创建一个 SystemLog 实体对象，设置操作人、操作类型、操作内容和当前时间，
     * 然后调用 SystemLogDao.insert() 将日志持久化到数据库。
     * 该方法为其他便捷日志方法（addLoginLog、addDeleteLog 等）提供底层支持。
     *
     * @param operatorName     操作人名称，通常是当前登录的管理员或操作员用户名
     * @param operationType    操作类型，例如 "登录"、"删除"、"修改"、"错误" 等
     * @param operationContent 操作详细内容，描述具体执行了什么操作
     */
    public void addLog(String operatorName, String operationType, String operationContent) {
        try {
            SystemLog log = new SystemLog();
            log.setOperatorName(operatorName);
            log.setOperationType(operationType);
            log.setOperationContent(operationContent);
            log.setCreateTime(DateUtil.getCurrentDateTime());

            systemLogDao.insert(log);
        } catch (Exception e) {
            System.err.println("记录日志失败: " + e.getMessage());
        }
    }

    public void addOperationLog(String operator, String type, String content) {
        addLog(operator, type, content);
    }

    /**
     * 添加登录日志（便捷方法）
     * 记录管理员或操作员登录系统的操作，操作类型固定为 "登录"。
     * 内部调用 addLog() 通用方法完成日志记录。
     *
     * @param operatorName 登录的操作人名称
     */
    public void addLoginLog(String operatorName) {
        addLog(operatorName, "登录", operatorName + " 登录了系统");
    }

    /**
     * 添加删除操作日志（便捷方法）
     * 记录管理员执行的删除操作，操作类型固定为 "删除"。
     * 内部调用 addLog() 通用方法完成日志记录。
     *
     * @param operatorName 执行删除操作的操作人名称
     * @param content      被删除的内容描述，例如 "删除了用户：张三"
     */
    public void addDeleteLog(String operatorName, String content) {
        addLog(operatorName, "删除", content);
    }

    /**
     * 添加修改操作日志（便捷方法）
     * 记录管理员执行的修改/更新操作，操作类型固定为 "修改"。
     * 内部调用 addLog() 通用方法完成日志记录。
     *
     * @param operatorName 执行修改操作的操作人名称
     * @param content      修改的内容描述，例如 "修改了用户张三的会员等级"
     */
    public void addUpdateLog(String operatorName, String content) {
        addLog(operatorName, "修改", content);
    }

    /**
     * 添加错误日志（便捷方法）
     * 记录系统运行过程中发生的错误或异常，操作类型固定为 "错误"。
     * 用于问题排查和系统审计，帮助管理员追踪系统异常情况。
     * 内部调用 addLog() 通用方法完成日志记录。
     *
     * @param operatorName 触发或关联该错误的操作人名称，系统级错误可传 "SYSTEM"
     * @param content      错误详细描述，例如 "用户充值失败：数据库连接超时"
     */
    public void addErrorLog(String operatorName, String content) {
        addLog(operatorName, "错误", content);
    }

    /**
     * 获取所有系统日志
     * 查询系统中全部操作日志记录，按创建时间降序排列，最新的日志在前。
     * 该方法委托 SystemLogDao.findAll() 完成数据库查询。
     *
     * @return 包含所有系统日志的 List 集合，如果没有日志记录则返回空列表
     */
    public List<SystemLog> getAllLogs() {
        return systemLogDao.findAll();
    }

    /**
     * 根据操作人查询系统日志
     * 查询指定操作人员产生的所有操作日志记录，按时间降序排列。
     * 用于追踪某个管理员或操作员的所有操作历史。
     * 该方法委托 SystemLogDao.findByOperator() 完成数据库查询。
     *
     * @param operatorName 操作人名称
     * @return 该操作人的所有日志记录列表，如果没有相关记录则返回空列表
     */
    public List<SystemLog> getLogsByOperator(String operatorName) {
        return systemLogDao.findByOperator(operatorName);
    }

    /**
     * 根据操作类型查询系统日志
     * 查询指定操作类型的所有日志记录，按时间降序排列。
     * 用于分类查看操作历史，例如查看所有"登录"记录、所有"删除"记录等。
     * 该方法委托 SystemLogDao.findByType() 完成数据库查询。
     *
     * @param operationType 操作类型字符串，例如 "登录"、"删除"、"修改"、"错误"
     * @return 该操作类型的所有日志记录列表，如果没有相关记录则返回空列表
     */
    public List<SystemLog> getLogsByType(String operationType) {
        return systemLogDao.findByType(operationType);
    }

    /**
     * 根据日期范围查询系统日志
     * 查询指定开始日期和结束日期之间产生的所有操作日志，按时间降序排列。
     * 用于按时间段检索操作记录，方便进行周期性的操作审计和问题排查。
     * 日期范围为闭区间，包含开始日期和结束日期当天的所有日志。
     * 该方法委托 SystemLogDao.findByDateRange() 完成数据库查询。
     *
     * @param startDate 开始日期字符串，格式为 yyyy-MM-dd，例如 "2025-01-01"
     * @param endDate   结束日期字符串，格式为 yyyy-MM-dd，例如 "2025-01-31"
     * @return 日期范围内的所有日志记录列表，如果没有相关记录则返回空列表
     */
    public List<SystemLog> getLogsByDateRange(String startDate, String endDate) {
        return systemLogDao.findByDateRange(startDate, endDate);
    }
}