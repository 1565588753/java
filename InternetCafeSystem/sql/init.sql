-- ==========================================
-- 网吧计费管理系统 - 数据库初始化脚本
-- 数据库名称: internet_cafe_db
-- 编码: utf8mb4
-- ==========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS internet_cafe_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE internet_cafe_db;

-- ==========================================
-- 1. 管理员表 admin
-- ==========================================
DROP TABLE IF EXISTS admin;
CREATE TABLE admin (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    role VARCHAR(20) NOT NULL DEFAULT 'admin' COMMENT '角色：admin-超级管理员, operator-操作员',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ==========================================
-- 2. 会员等级表 vip_level
-- ==========================================
DROP TABLE IF EXISTS vip_level;
CREATE TABLE vip_level (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '等级ID',
    level_name VARCHAR(20) NOT NULL UNIQUE COMMENT '等级名称',
    discount_rate DECIMAL(3,2) NOT NULL COMMENT '折扣率（如0.80表示8折）',
    description VARCHAR(200) DEFAULT '' COMMENT '等级描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级表';

-- ==========================================
-- 3. 用户表 user
-- ==========================================
DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    id_card VARCHAR(18) NOT NULL COMMENT '身份证号',
    phone VARCHAR(11) NOT NULL COMMENT '手机号',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    vip_level INT NOT NULL DEFAULT 1 COMMENT '会员等级ID，关联vip_level表',
    points INT NOT NULL DEFAULT 0 COMMENT '积分',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态：0-禁用, 1-正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_status (status),
    CONSTRAINT fk_user_vip_level FOREIGN KEY (vip_level) REFERENCES vip_level(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ==========================================
-- 4. 上机记录表 online_record
-- ==========================================
DROP TABLE IF EXISTS online_record;
CREATE TABLE online_record (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id INT NOT NULL COMMENT '用户ID',
    login_time DATETIME NOT NULL COMMENT '上机时间',
    logout_time DATETIME DEFAULT NULL COMMENT '下机时间',
    duration BIGINT NOT NULL DEFAULT 0 COMMENT '上机时长（分钟）',
    cost DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '消费金额',
    machine_no VARCHAR(10) NOT NULL COMMENT '机器编号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-上机中, 2-已下机, 3-异常下机',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_login_time (login_time),
    CONSTRAINT fk_online_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上机记录表';

-- ==========================================
-- 5. 充值记录表 recharge_record
-- ==========================================
DROP TABLE IF EXISTS recharge_record;
CREATE TABLE recharge_record (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id INT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '充值金额',
    recharge_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
    operator_name VARCHAR(50) NOT NULL COMMENT '操作员',
    INDEX idx_user_id (user_id),
    INDEX idx_recharge_time (recharge_time),
    CONSTRAINT fk_recharge_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值记录表';

-- ==========================================
-- 6. 消费记录表 consume_record
-- ==========================================
DROP TABLE IF EXISTS consume_record;
CREATE TABLE consume_record (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id INT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '消费金额',
    consume_type VARCHAR(20) NOT NULL DEFAULT '上机消费' COMMENT '消费类型',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消费时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    CONSTRAINT fk_consume_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费记录表';

-- ==========================================
-- 7. 系统配置表 system_config
-- ==========================================
DROP TABLE IF EXISTS system_config;
CREATE TABLE system_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(50) NOT NULL UNIQUE,
    config_value VARCHAR(200) NOT NULL,
    description VARCHAR(200) DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO system_config (config_key, config_value, description) VALUES
('base_price', '5.0', '基础上网单价（元/小时）'),
('peak_price', '8.0', '高峰时段单价（元/小时）'),
('night_price', '3.0', '深夜时段单价（元/小时）');

-- ==========================================
-- 8. 系统日志表 system_log
-- ==========================================
DROP TABLE IF EXISTS system_log;
CREATE TABLE system_log (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    operator_name VARCHAR(50) NOT NULL COMMENT '操作人',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：LOGIN-登录, DELETE-删除, UPDATE-修改, ERROR-异常, OTHER-其他',
    operation_content VARCHAR(500) NOT NULL COMMENT '操作详情',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_operator (operator_name),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- ==========================================
-- 初始化数据
-- ==========================================

-- 插入会员等级数据
INSERT INTO vip_level (level_name, discount_rate, description) VALUES
('普通会员', 1.00, '普通会员，无折扣'),
('VIP会员', 0.80, 'VIP会员，享受8折优惠'),
('SVIP会员', 0.60, 'SVIP会员，享受6折优惠');

-- 插入默认管理员账号（密码为加密后的 admin123）
-- 使用SHA-256加密: admin123 -> 对应的密文
INSERT INTO admin (username, password, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'admin');

-- 插入测试用户数据
INSERT INTO user (username, password, real_name, id_card, phone, balance, vip_level, points, status) VALUES
('test001', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '张三', '110101199001011234', '13800138001', 100.00, 1, 100, 1),
('test002', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '李四', '110101199002021235', '13800138002', 200.00, 2, 500, 1),
('test003', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '王五', '110101199003031236', '13800138003', 50.00, 1, 50, 1);

-- ==========================================
-- 9. 零食商品表 snack_product
-- ==========================================
DROP TABLE IF EXISTS snack_product;
CREATE TABLE snack_product (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    image VARCHAR(500) DEFAULT '' COMMENT '商品图片（base64或URL）',
    stock INT NOT NULL DEFAULT 999 COMMENT '库存数量',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架, 1-上架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- 10. 零食订单表 snack_order
-- ==========================================
DROP TABLE IF EXISTS snack_order;
CREATE TABLE snack_order (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL COMMENT '用户ID',
    product_id INT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称（冗余）',
    quantity INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    total_price DECIMAL(10,2) NOT NULL COMMENT '总价',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待确认, 1-已完成, 2-已取消',
    order_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirm_time DATETIME DEFAULT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    CONSTRAINT fk_snack_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_snack_product FOREIGN KEY (product_id) REFERENCES snack_product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化示例商品
INSERT INTO snack_product (name, price, stock, status) VALUES
('康师傅红烧牛肉面', 5.00, 100, 1),
('统一冰红茶', 4.00, 200, 1),
('恰恰瓜子', 6.00, 150, 1),
('乐事薯片', 8.00, 80, 1),
('可口可乐', 4.00, 200, 1),
('农夫山泉', 2.00, 300, 1),
('红牛', 6.00, 120, 1),
('火腿肠', 3.00, 200, 1);
