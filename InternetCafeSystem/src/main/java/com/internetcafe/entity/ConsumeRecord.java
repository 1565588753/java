package com.internetcafe.entity;

import java.math.BigDecimal;

/**
 * 消费记录实体类
 * 对应数据库 consume_record 表
 */
public class ConsumeRecord {
    private Integer id;              // 记录ID
    private Integer userId;          // 用户ID
    private String username;         // 用户名（用于展示）
    private BigDecimal amount;       // 消费金额
    private String consumeType;      // 消费类型
    private String createTime;       // 消费时间

    public ConsumeRecord() {}

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

    public String getConsumeType() {
        return consumeType;
    }

    public void setConsumeType(String consumeType) {
        this.consumeType = consumeType;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ConsumeRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", amount=" + amount +
                '}';
    }
}