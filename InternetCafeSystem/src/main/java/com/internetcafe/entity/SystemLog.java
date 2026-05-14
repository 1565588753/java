package com.internetcafe.entity;

/**
 * 系统日志实体类
 * 对应数据库 system_log 表
 */
public class SystemLog {
    private Integer id;              // 日志ID
    private String operatorName;     // 操作人
    private String operationType;    // 操作类型
    private String operationContent; // 操作详情
    private String createTime;       // 操作时间

    public SystemLog() {}

    public SystemLog(Integer id, String operatorName, String operationType,
                     String operationContent, String createTime) {
        this.id = id;
        this.operatorName = operatorName;
        this.operationType = operationType;
        this.operationContent = operationContent;
        this.createTime = createTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationContent() {
        return operationContent;
    }

    public void setOperationContent(String operationContent) {
        this.operationContent = operationContent;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "SystemLog{" +
                "id=" + id +
                ", operatorName='" + operatorName + '\'' +
                ", operationType='" + operationType + '\'' +
                '}';
    }
}