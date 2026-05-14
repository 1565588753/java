package com.internetcafe.entity;

import java.math.BigDecimal;

/**
 * 上机记录实体类
 * 对应数据库 online_record 表
 */
public class OnlineRecord {
    private Integer id;              // 记录ID
    private Integer userId;          // 用户ID
    private String username;         // 用户名（用于展示）
    private String loginTime;        // 上机时间
    private String logoutTime;       // 下机时间
    private Long duration;           // 上机时长（分钟）
    private BigDecimal cost;         // 消费金额
    private String machineNo;        // 机器编号
    private Integer status;          // 状态：1-上机中, 2-已下机, 3-异常下机

    public OnlineRecord() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(String logoutTime) {
        this.logoutTime = logoutTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getMachineNo() {
        return machineNo;
    }

    public void setMachineNo(String machineNo) {
        this.machineNo = machineNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OnlineRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", machineNo='" + machineNo + '\'' +
                ", status=" + status +
                '}';
    }
}