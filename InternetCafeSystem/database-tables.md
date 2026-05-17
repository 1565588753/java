# 数据库表结构

数据库名称：`internet_cafe_db`

---

## 1. 管理员表（admin）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 管理员ID（主键） |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(100) | 密码（加密存储） |
| role | VARCHAR(20) | 角色 |
| create_time | DATETIME | 创建时间 |

## 2. 会员等级表（vip_level）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 等级ID（主键） |
| level_name | VARCHAR(20) | 等级名称 |
| discount_rate | DECIMAL(3,2) | 折扣率 |
| description | VARCHAR(200) | 等级描述 |

## 3. 用户表（user）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 用户ID（主键） |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(100) | 密码（加密存储） |
| real_name | VARCHAR(50) | 真实姓名 |
| id_card | VARCHAR(18) | 身份证号 |
| phone | VARCHAR(11) | 手机号 |
| balance | DECIMAL(10,2) | 账户余额 |
| vip_level | INT | 会员等级ID |
| points | INT | 积分 |
| status | TINYINT | 状态 |
| create_time | DATETIME | 创建时间 |

## 4. 上机记录表（online_record）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 记录ID（主键） |
| user_id | INT | 用户ID |
| login_time | DATETIME | 上机时间 |
| logout_time | DATETIME | 下机时间 |
| duration | BIGINT | 上机时长（分钟） |
| cost | DECIMAL(10,2) | 消费金额 |
| machine_no | VARCHAR(10) | 机器编号 |
| status | TINYINT | 状态 |

## 5. 充值记录表（recharge_record）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 记录ID（主键） |
| user_id | INT | 用户ID |
| amount | DECIMAL(10,2) | 充值金额 |
| recharge_time | DATETIME | 充值时间 |
| operator_name | VARCHAR(50) | 操作员 |

## 6. 消费记录表（consume_record）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 记录ID（主键） |
| user_id | INT | 用户ID |
| amount | DECIMAL(10,2) | 消费金额 |
| consume_type | VARCHAR(20) | 消费类型 |
| create_time | DATETIME | 消费时间 |

## 7. 系统配置表（system_config）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 配置ID（主键） |
| config_key | VARCHAR(50) | 配置键名 |
| config_value | VARCHAR(200) | 配置值 |
| description | VARCHAR(200) | 配置描述 |
| update_time | DATETIME | 更新时间 |

## 8. 系统日志表（system_log）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 日志ID（主键） |
| operator_name | VARCHAR(50) | 操作人 |
| operation_type | VARCHAR(20) | 操作类型 |
| operation_content | VARCHAR(500) | 操作详情 |
| create_time | DATETIME | 操作时间 |

## 9. 零食商品表（snack_product）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 商品ID（主键） |
| name | VARCHAR(100) | 商品名称 |
| price | DECIMAL(10,2) | 商品单价 |
| image | VARCHAR(500) | 商品图片 |
| stock | INT | 库存数量 |
| status | TINYINT | 状态 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

## 10. 零食订单表（snack_order）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 订单ID（主键） |
| user_id | INT | 用户ID |
| product_id | INT | 商品ID |
| product_name | VARCHAR(100) | 商品名称 |
| quantity | INT | 购买数量 |
| unit_price | DECIMAL(10,2) | 单价 |
| total_price | DECIMAL(10,2) | 总价 |
| status | TINYINT | 状态 |
| order_time | DATETIME | 下单时间 |
| confirm_time | DATETIME | 确认时间 |

## 11. 报修表（repair_request）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 报修ID（主键） |
| user_id | INT | 报修用户ID |
| username | VARCHAR(50) | 报修用户名 |
| machine_no | VARCHAR(10) | 报修机器号 |
| description | VARCHAR(500) | 故障描述 |
| status | TINYINT | 状态 |
| create_time | DATETIME | 报修时间 |
| handle_time | DATETIME | 处理时间 |