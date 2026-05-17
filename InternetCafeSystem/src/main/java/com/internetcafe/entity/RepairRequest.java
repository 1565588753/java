package com.internetcafe.entity;

import java.math.BigDecimal;

public class RepairRequest {
    private Integer id;
    private Integer userId;
    private String username;
    private String machineNo;
    private String description;
    private Integer status;
    private String createTime;
    private String handleTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMachineNo() { return machineNo; }
    public void setMachineNo(String machineNo) { this.machineNo = machineNo; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getHandleTime() { return handleTime; }
    public void setHandleTime(String handleTime) { this.handleTime = handleTime; }
}
