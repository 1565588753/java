-- ==========================================
-- 网吧计费管理系统 - 数据备份
-- 备份时间: 2026-05-14 16:07:01
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
INSERT INTO admin VALUES ('1', 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'admin', '2026-05-14 11:22:45');

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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费记录表';

-- 表数据: consume_record
INSERT INTO consume_record VALUES ('1', '1', '0.08', '上机消费', '2026-05-14 12:22:51');
INSERT INTO consume_record VALUES ('2', '1', '0.08', '上机消费', '2026-05-14 13:30:54');
INSERT INTO consume_record VALUES ('3', '2', '0.07', '上机消费', '2026-05-14 13:33:31');
INSERT INTO consume_record VALUES ('4', '2', '0.07', '上机消费', '2026-05-14 13:33:31');
INSERT INTO consume_record VALUES ('5', '3', '0.07', '上机消费', '2026-05-14 13:38:00');

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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上机记录表';

-- 表数据: online_record
INSERT INTO online_record VALUES ('1', '1', '2026-05-14 12:22:50', '2026-05-14 12:22:51', '1', '0.08', 'A01', '2');
INSERT INTO online_record VALUES ('2', '1', '2026-05-14 13:30:54', '2026-05-14 13:30:54', '1', '0.08', 'B01', '2');
INSERT INTO online_record VALUES ('3', '2', '2026-05-14 13:33:31', '2026-05-14 13:33:31', '1', '0.07', 'C01', '2');
INSERT INTO online_record VALUES ('4', '3', '2026-05-14 13:38:00', '2026-05-14 13:38:00', '1', '0.07', 'E01', '2');

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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值记录表';

-- 表数据: recharge_record
INSERT INTO recharge_record VALUES ('1', '1', '50.00', '2026-05-14 13:30:54', 'admin');
INSERT INTO recharge_record VALUES ('2', '2', '100.00', '2026-05-14 13:38:00', 'admin');

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
) ENGINE=InnoDB AUTO_INCREMENT=63 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- 表数据: system_log
INSERT INTO system_log VALUES ('1', 'admin', '登录', '管理员 [admin] 登录系统，角色：admin', '2026-05-14 11:35:07');
INSERT INTO system_log VALUES ('2', 'admin', '登出', '管理员 [admin] 退出系统', '2026-05-14 11:35:07');
INSERT INTO system_log VALUES ('3', '测试员', '测试', '这是一条测试日志', '2026-05-14 11:35:07');
INSERT INTO system_log VALUES ('4', '测试员', '登录', '测试员 登录了系统', '2026-05-14 11:35:07');
INSERT INTO system_log VALUES ('5', '测试员', '错误', '模拟错误日志', '2026-05-14 11:35:07');
INSERT INTO system_log VALUES ('6', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 12:19:13');
INSERT INTO system_log VALUES ('7', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 12:19:35');
INSERT INTO system_log VALUES ('8', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 12:22:49');
INSERT INTO system_log VALUES ('9', 'admin', '上机', '管理员 [admin] 为用户 [test001] 开启上机，机器号：A01', '2026-05-14 12:22:50');
INSERT INTO system_log VALUES ('10', 'admin', '下机', '管理员 [admin] 执行下机结算，记录ID=1，消费金额=0.08元', '2026-05-14 12:22:51');
INSERT INTO system_log VALUES ('11', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:30:27');
INSERT INTO system_log VALUES ('12', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:30:28');
INSERT INTO system_log VALUES ('13', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:30:53');
INSERT INTO system_log VALUES ('14', 'admin', '新增用户', '管理员 [admin] 新增了用户 [debug001]', '2026-05-14 13:30:53');
INSERT INTO system_log VALUES ('15', 'admin', '上机', '管理员 [admin] 为用户 [test001] 开启上机，机器号：B01', '2026-05-14 13:30:54');
INSERT INTO system_log VALUES ('16', 'admin', '下机', '管理员 [admin] 执行下机结算，记录ID=2，消费金额=0.08元', '2026-05-14 13:30:54');
INSERT INTO system_log VALUES ('17', 'admin', '修改会员等级', '管理员 [admin] 将会员等级 [VIP会员] 的折扣率修改为 0.75', '2026-05-14 13:30:54');
INSERT INTO system_log VALUES ('18', 'admin', '修改会员等级', '管理员 [admin] 将会员等级 [VIP会员] 的折扣率修改为 0.8', '2026-05-14 13:30:54');
INSERT INTO system_log VALUES ('19', 'admin', '会员升级', '管理员 [admin] 将用户ID=3 升级为 [VIP会员]', '2026-05-14 13:30:54');
INSERT INTO system_log VALUES ('20', 'admin', '充值', '管理员 [admin] 为用户ID=1 充值 50.0 元', '2026-05-14 13:30:54');
INSERT INTO system_log VALUES ('21', 'admin', '清理日志', '管理员 [admin] 清理了 2020-01-01 之前的系统日志，共删除 0 条记录', '2026-05-14 13:30:54');
INSERT INTO system_log VALUES ('22', 'admin', '删除用户', '管理员 [admin] 删除了用户 [debug001]，用户ID=4', '2026-05-14 13:30:54');
INSERT INTO system_log VALUES ('23', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:32:51');
INSERT INTO system_log VALUES ('24', 'admin', '新增用户', '管理员 [admin] 新增了用户 [debug002]', '2026-05-14 13:32:52');
INSERT INTO system_log VALUES ('25', 'admin', '修改用户', '管理员 [admin] 修改了用户ID=5 的信息', '2026-05-14 13:32:52');
INSERT INTO system_log VALUES ('26', 'admin', '删除用户', '管理员 [admin] 删除了用户 [debug002]，用户ID=5', '2026-05-14 13:32:52');
INSERT INTO system_log VALUES ('27', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:33:30');
INSERT INTO system_log VALUES ('28', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:33:30');
INSERT INTO system_log VALUES ('29', 'admin', '上机', '管理员 [admin] 为用户 [test002] 开启上机，机器号：C01', '2026-05-14 13:33:31');
INSERT INTO system_log VALUES ('30', 'admin', '下机', '管理员 [admin] 执行下机结算，记录ID=3，消费金额=0.07元', '2026-05-14 13:33:31');
INSERT INTO system_log VALUES ('31', 'admin', '下机', '管理员 [admin] 执行下机结算，记录ID=3，消费金额=0.07元', '2026-05-14 13:33:31');
INSERT INTO system_log VALUES ('32', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:37:59');
INSERT INTO system_log VALUES ('33', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:37:59');
INSERT INTO system_log VALUES ('34', 'admin', '上机', '管理员 [admin] 为用户 [test003] 开启上机，机器号：E01', '2026-05-14 13:38:00');
INSERT INTO system_log VALUES ('35', 'admin', '下机', '管理员 [admin] 执行下机结算，记录ID=4，消费金额=0.07元', '2026-05-14 13:38:00');
INSERT INTO system_log VALUES ('36', 'admin', '充值', '管理员 [admin] 为用户ID=2 充值 100.0 元', '2026-05-14 13:38:00');
INSERT INTO system_log VALUES ('37', 'admin', '修改会员等级', '管理员 [admin] 将会员等级 [VIP会员] 的折扣率修改为 0.8', '2026-05-14 13:38:00');
INSERT INTO system_log VALUES ('38', 'admin', '会员升级', '管理员 [admin] 将用户ID=3 升级为 [普通会员]', '2026-05-14 13:38:00');
INSERT INTO system_log VALUES ('39', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:40:05');
INSERT INTO system_log VALUES ('40', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:40:51');
INSERT INTO system_log VALUES ('41', 'admin', '新增用户', '管理员 [admin] 新增了用户 [11]', '2026-05-14 13:41:50');
INSERT INTO system_log VALUES ('42', 'admin', '登出', '管理员 [admin] 通过Web端退出系统', '2026-05-14 13:42:06');
INSERT INTO system_log VALUES ('43', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:42:59');
INSERT INTO system_log VALUES ('44', 'admin', '登出', '管理员 [admin] 通过Web端退出系统', '2026-05-14 13:43:21');
INSERT INTO system_log VALUES ('45', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:54:04');
INSERT INTO system_log VALUES ('46', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:54:13');
INSERT INTO system_log VALUES ('47', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:54:24');
INSERT INTO system_log VALUES ('48', 'admin', '登出', '管理员 [admin] 通过Web端退出系统', '2026-05-14 13:54:24');
INSERT INTO system_log VALUES ('49', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 13:57:48');
INSERT INTO system_log VALUES ('50', 'admin', '修改用户', '管理员 [admin] 修改了用户ID=6 的信息', '2026-05-14 13:57:55');
INSERT INTO system_log VALUES ('51', 'admin', '修改用户', '管理员 [admin] 修改了用户ID=6 的信息', '2026-05-14 13:58:02');
INSERT INTO system_log VALUES ('52', 'admin', '登出', '管理员 [admin] 通过Web端退出系统', '2026-05-14 13:58:03');
INSERT INTO system_log VALUES ('53', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 14:00:09');
INSERT INTO system_log VALUES ('54', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 14:02:41');
INSERT INTO system_log VALUES ('55', 'admin', '错误', '账户 [admin] 因连续5次登录失败已被锁定15分钟', '2026-05-14 14:33:09');
INSERT INTO system_log VALUES ('56', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 14:33:31');
INSERT INTO system_log VALUES ('57', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 14:38:26');
INSERT INTO system_log VALUES ('58', 'admin', '删除用户', '管理员 [admin] 删除了用户 [11]，用户ID=6', '2026-05-14 14:38:32');
INSERT INTO system_log VALUES ('59', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 14:42:31');
INSERT INTO system_log VALUES ('60', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 14:42:48');
INSERT INTO system_log VALUES ('61', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 15:03:34');
INSERT INTO system_log VALUES ('62', 'admin', '登录', '管理员 [admin] 通过Web端登录系统，角色：admin', '2026-05-14 15:06:35');

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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 表数据: user
INSERT INTO user VALUES ('1', 'test001', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '张三', '110101199001011234', '13800138001', '149.84', '1', '100', '1', '2026-05-14 11:22:45');
INSERT INTO user VALUES ('2', 'test002', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '李四', '110101199002021235', '13800138002', '299.86', '2', '500', '1', '2026-05-14 11:22:45');
INSERT INTO user VALUES ('3', 'test003', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '王五', '110101199003031236', '13800138003', '49.93', '1', '50', '1', '2026-05-14 11:22:45');

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
