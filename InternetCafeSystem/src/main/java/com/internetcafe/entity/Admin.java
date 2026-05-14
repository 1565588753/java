package com.internetcafe.entity;

/**
 * 管理员实体类
 * 对应数据库 admin 表
 */
public class Admin {
    private Integer id;              // 管理员ID
    private String username;         // 用户名
    private String password;         // 密码（加密存储）
    private String role;             // 角色：admin-超级管理员, operator-操作员
    private String createTime;       // 创建时间

    public Admin() {}

    public Admin(Integer id, String username, String password, String role, String createTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.createTime = createTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}