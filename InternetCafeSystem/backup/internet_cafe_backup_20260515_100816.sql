-- ==========================================
-- 网吧计费管理系统 - 数据备份
-- 备份时间: 2026-05-15 10:08:16
-- ==========================================

-- 表结构: admin
DROP TABLE IF EXISTS admin;
CREATE TABLE `admin` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'admin' COMMENT '角色：admin-超级管理员, operator-操作员',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 表数据: admin
INSERT INTO admin VALUES ('1', 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'admin', '2026-05-15 06:01:42');

-- 表结构: consume_record
DROP TABLE IF EXISTS consume_record;
CREATE TABLE `consume_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '消费金额',
  `consume_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '上机消费' COMMENT '消费类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消费时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_consume_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费记录表';

-- 表数据: consume_record

-- 表结构: online_record
DROP TABLE IF EXISTS online_record;
CREATE TABLE `online_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `login_time` datetime NOT NULL COMMENT '上机时间',
  `logout_time` datetime DEFAULT NULL COMMENT '下机时间',
  `duration` bigint NOT NULL DEFAULT '0' COMMENT '上机时长（分钟）',
  `cost` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '消费金额',
  `machine_no` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '机器编号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1-上机中, 2-已下机, 3-异常下机',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_login_time` (`login_time`),
  CONSTRAINT `fk_online_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上机记录表';

-- 表数据: online_record

-- 表结构: recharge_record
DROP TABLE IF EXISTS recharge_record;
CREATE TABLE `recharge_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '充值金额',
  `recharge_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
  `operator_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作员',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_recharge_time` (`recharge_time`),
  CONSTRAINT `fk_recharge_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值记录表';

-- 表数据: recharge_record

-- 表结构: system_log
DROP TABLE IF EXISTS system_log;
CREATE TABLE `system_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operator_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人',
  `operation_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型：LOGIN-登录, DELETE-删除, UPDATE-修改, ERROR-异常, OTHER-其他',
  `operation_content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作详情',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator` (`operator_name`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- 表数据: system_log
INSERT INTO system_log VALUES ('1', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-15 06:07:48');

-- 表结构: user
DROP TABLE IF EXISTS user;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `id_card` varchar(18) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证号',
  `phone` varchar(11) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
  `vip_level` int NOT NULL DEFAULT '1' COMMENT '会员等级ID，关联vip_level表',
  `points` int NOT NULL DEFAULT '0' COMMENT '积分',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '用户状态：0-禁用, 1-正常',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `fk_user_vip_level` (`vip_level`),
  CONSTRAINT `fk_user_vip_level` FOREIGN KEY (`vip_level`) REFERENCES `vip_level` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 表数据: user
INSERT INTO user VALUES ('1', 'test001', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '张三', '110101199001011234', '13800138001', '100.00', '1', '100', '1', '2026-05-15 06:01:42');
INSERT INTO user VALUES ('2', 'test002', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '李四', '110101199002021235', '13800138002', '200.00', '2', '500', '1', '2026-05-15 06:01:42');
INSERT INTO user VALUES ('3', 'test003', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '王五', '110101199003031236', '13800138003', '50.00', '1', '50', '1', '2026-05-15 06:01:42');

-- 表结构: vip_level
DROP TABLE IF EXISTS vip_level;
CREATE TABLE `vip_level` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '等级ID',
  `level_name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '等级名称',
  `discount_rate` decimal(3,2) NOT NULL COMMENT '折扣率（如0.80表示8折）',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '等级描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `level_name` (`level_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员等级表';

-- 表数据: vip_level
INSERT INTO vip_level VALUES ('1', '普通会员', '1.00', '普通会员，无折扣');
INSERT INTO vip_level VALUES ('2', 'VIP会员', '0.80', 'VIP会员，享受8折优惠');
INSERT INTO vip_level VALUES ('3', 'SVIP会员', '0.60', 'SVIP会员，享受6折优惠');

-- 表结构: sys_config
DROP TABLE IF EXISTS sys_config;
