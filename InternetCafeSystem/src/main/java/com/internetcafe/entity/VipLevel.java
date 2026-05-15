package com.internetcafe.entity;

import java.math.BigDecimal;

/**
 * 会员等级实体类
 * 对应数据库 vip_level 表
 */
public class VipLevel {
    private Integer id;              // 等级ID
    private String levelName;        // 等级名称
    private BigDecimal discountRate; // 折扣率
    private String description;      // 等级描述

    public VipLevel() {}

    public VipLevel(Integer id, String levelName, BigDecimal discountRate, String description) {
        this.id = id;
        this.levelName = levelName;
        this.discountRate = discountRate;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "VipLevel{" +
                "id=" + id +
                ", levelName='" + levelName + '\'' +
                ", discountRate=" + discountRate +
                '}';
    }
}