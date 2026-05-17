# 数据库表结构设计

## 数据库概述

- **数据库名称**：`internet_cafe_db`
- **字符集**：`utf8mb4`（支持完整中文和特殊字符）
- **排序规则**：`utf8mb4_unicode_ci`
- **存储引擎**：所有表均使用 `InnoDB`（支持事务和外键约束）

---

## 表关系总览

```
admin（管理员）          — 独立，无外键
vip_level（会员等级）    — 被 user 引用
user（用户）             — 引用 vip_level，被多个表引用
online_record（上机记录）— 引用 user
recharge_record（充值记录）— 引用 user
consume_record（消费记录）— 引用 user
system_config（系统配置） — 独立，无外键
system_log（系统日志）    — 独立，无外键
snack_product（零食商品） — 独立，被 snack_order 引用
snack_order（零食订单）   — 引用 user 和 snack_product
repair_request（报修表）  — 引用 user
```

---

## 表结构详情

### 1. 管理员表（admin）— D1

存储系统管理员账号信息。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 管理员ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| password | VARCHAR(100) | NOT NULL | 密码（SHA-256加密存储） |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'admin' | 角色：admin-超级管理员, operator-操作员 |
| create_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 2. 会员等级表（vip_level）— D3

定义不同会员等级的折扣规则。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 等级ID |
| level_name | VARCHAR(20) | NOT NULL, UNIQUE | 等级名称（普通会员/VIP会员/SVIP会员） |
| discount_rate | DECIMAL(3,2) | NOT NULL | 折扣率（如0.80表示8折） |
| description | VARCHAR(200) | DEFAULT '' | 等级描述 |

**初始数据**：

| id | level_name | discount_rate | description |
|----|-----------|--------------|-------------|
| 1 | 普通会员 | 1.00 | 普通会员，无折扣 |
| 2 | VIP会员 | 0.80 | VIP会员，享受8折优惠 |
| 3 | SVIP会员 | 0.60 | SVIP会员，享受6折优惠 |

### 3. 用户表（user）— D2

存储上网顾客的用户信息。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| password | VARCHAR(100) | NOT NULL | 密码（SHA-256加密存储） |
| real_name | VARCHAR(50) | NOT NULL | 真实姓名 |
| id_card | VARCHAR(18) | NOT NULL | 身份证号 |
| phone | VARCHAR(11) | NOT NULL | 手机号 |
| balance | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 账户余额 |
| vip_level | INT | NOT NULL, DEFAULT 1, FK → vip_level(id) | 会员等级ID |
| points | INT | NOT NULL, DEFAULT 0 | 积分 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：0-禁用, 1-正常 |
| create_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**：idx_username, idx_phone, idx_status

### 4. 上机记录表（online_record）— D4

记录用户上机、下机和计费信息。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 记录ID |
| user_id | INT | NOT NULL, FK → user(id) | 用户ID |
| login_time | DATETIME | NOT NULL | 上机时间 |
| logout_time | DATETIME | DEFAULT NULL | 下机时间 |
| duration | BIGINT | NOT NULL, DEFAULT 0 | 上机时长（分钟） |
| cost | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | 消费金额 |
| machine_no | VARCHAR(10) | NOT NULL | 机器编号 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：1-上机中, 2-已下机, 3-异常下机 |

**索引**：idx_user_id, idx_status, idx_login_time

### 5. 充值记录表（recharge_record）— D5

记录用户充值操作详情。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 记录ID |
| user_id | INT | NOT NULL, FK → user(id) | 用户ID |
| amount | DECIMAL(10,2) | NOT NULL | 充值金额 |
| recharge_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 充值时间 |
| operator_name | VARCHAR(50) | NOT NULL | 操作员 |

**索引**：idx_user_id, idx_recharge_time

### 6. 消费记录表（consume_record）— D6

记录用户的所有消费明细。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 记录ID |
| user_id | INT | NOT NULL, FK → user(id) | 用户ID |
| amount | DECIMAL(10,2) | NOT NULL | 消费金额 |
| consume_type | VARCHAR(20) | NOT NULL, DEFAULT '上机消费' | 消费类型 |
| create_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 消费时间 |

**索引**：idx_user_id, idx_create_time

### 7. 系统配置表（system_config）— D11

存储系统的运行时配置参数（键值对形式）。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 配置ID |
| config_key | VARCHAR(50) | NOT NULL, UNIQUE | 配置键名 |
| config_value | VARCHAR(200) | NOT NULL | 配置值 |
| description | VARCHAR(200) | DEFAULT '' | 配置描述 |
| update_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**初始配置数据**：

| config_key | config_value | description |
|-----------|-------------|-------------|
| base_price | 5.0 | 基础上网单价（元/小时） |
| peak_price | 8.0 | 高峰时段单价（元/小时） |
| night_price | 3.0 | 深夜时段单价（元/小时） |
| peak_start_hour | 18 | 高峰时段开始小时 |
| peak_end_hour | 23 | 高峰时段结束小时 |
| night_start_hour | 0 | 深夜时段开始小时 |
| night_end_hour | 7 | 深夜时段结束小时 |

### 8. 系统日志表（system_log）— D7

记录所有管理员的操作日志。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 日志ID |
| operator_name | VARCHAR(50) | NOT NULL | 操作人 |
| operation_type | VARCHAR(20) | NOT NULL | 操作类型：LOGIN/DELETE/UPDATE/ERROR/OTHER |
| operation_content | VARCHAR(500) | NOT NULL | 操作详情 |
| create_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 操作时间 |

**索引**：idx_operator, idx_operation_type, idx_create_time

### 9. 零食商品表（snack_product）— D9

管理网吧销售的零食商品信息。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 商品ID |
| name | VARCHAR(100) | NOT NULL | 商品名称 |
| price | DECIMAL(10,2) | NOT NULL | 商品单价 |
| image | VARCHAR(500) | DEFAULT '' | 商品图片 |
| stock | INT | NOT NULL, DEFAULT 999 | 库存数量 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态：0-下架, 1-上架 |
| create_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

### 10. 零食订单表（snack_order）— D10

记录用户购买零食的订单信息。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 订单ID |
| user_id | INT | NOT NULL, FK → user(id) | 用户ID |
| product_id | INT | NOT NULL, FK → snack_product(id) | 商品ID |
| product_name | VARCHAR(100) | NOT NULL | 商品名称（冗余） |
| quantity | INT | NOT NULL, DEFAULT 1 | 购买数量 |
| unit_price | DECIMAL(10,2) | NOT NULL | 单价 |
| total_price | DECIMAL(10,2) | NOT NULL | 总价 |
| status | TINYINT | NOT NULL, DEFAULT 0 | 状态：0-待确认, 1-已完成, 2-已取消 |
| order_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 下单时间 |
| confirm_time | DATETIME | DEFAULT NULL | 确认时间 |

**索引**：idx_user_id, idx_status

### 11. 报修表（repair_request）— D8

记录用户提交的机器报修请求。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 报修ID |
| user_id | INT | NOT NULL, FK → user(id) | 报修用户ID |
| username | VARCHAR(50) | NOT NULL | 报修用户名 |
| machine_no | VARCHAR(10) | NOT NULL | 报修机器号 |
| description | VARCHAR(500) | NOT NULL | 故障描述 |
| status | TINYINT | NOT NULL, DEFAULT 0 | 状态：0-待处理, 1-已处理 |
| create_time | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 报修时间 |
| handle_time | DATETIME | DEFAULT NULL | 处理时间 |

**索引**：idx_user_id, idx_status, idx_create_time

---

## E-R图核心关系

```
admin (1)  ── 操作 ──> (N) online_record
admin (1)  ── 操作 ──> (N) recharge_record
admin (1)  ── 操作 ──> (N) system_log

user (1)  ── 拥有 ──> (N) online_record
user (1)  ── 拥有 ──> (N) recharge_record
user (1)  ── 拥有 ──> (N) consume_record
user (1)  ── 拥有 ──> (N) snack_order
user (1)  ── 拥有 ──> (N) repair_request
user (N)  ── 属于 ──> (1) vip_level

snack_product (1)  ── 包含于 ──> (N) snack_order
```

---

## 数据流图对应关系

| 数据存储编号 | 表名 | 说明 |
|-------------|------|------|
| D1 | admin | 管理员表 |
| D2 | user | 用户表 |
| D3 | vip_level | 会员等级表 |
| D4 | online_record | 上机记录表 |
| D5 | recharge_record | 充值记录表 |
| D6 | consume_record | 消费记录表 |
| D7 | system_log | 系统日志表 |
| D8 | repair_request | 报修表 |
| D9 | snack_product | 零食商品表 |
| D10 | snack_order | 零食订单表 |
| D11 | system_config | 系统配置表 |