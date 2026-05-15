package com.internetcafe.entity;

import java.math.BigDecimal;

/**
 * 充值记录实体类
 * 对应数据库 recharge_record 表
 */
public class RechargeRecord {
    private Integer id;              // 记录ID
    private Integer userId;          // 用户ID
    private String username;         // 用户名（用于展示）
    private BigDecimal amount;       // 充值金额
    private String rechargeTime;     // 充值时间
    private String operatorName;     // 操作员

    public RechargeRecord() {}

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRechargeTime() {
        return rechargeTime;
    }

    public void setRechargeTime(String rechargeTime) {
        this.rechargeTime = rechargeTime;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    @Override
    public String toString() {
        return "RechargeRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", amount=" + amount +
                '}';
    }
}