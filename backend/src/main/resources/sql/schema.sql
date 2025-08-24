-- EMQX Topic Hub 数据库建表语句
-- 根据实体类字段生成
-- 创建时间: 2024-12-21

-- 设置字符集
SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`         bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`   varchar(50)  NOT NULL COMMENT '用户名',
    `password`   varchar(255) NOT NULL COMMENT '密码（加密存储）',
    `email`      varchar(100)          DEFAULT NULL COMMENT '邮箱',
    `real_name`  varchar(50)           DEFAULT NULL COMMENT '真实姓名',
    `status`     varchar(20)  NOT NULL DEFAULT 'active' COMMENT '用户状态：active-活跃，inactive-非活跃，locked-锁定',
    `role`       varchar(20)  NOT NULL DEFAULT 'user' COMMENT '角色：admin-管理员，user-普通用户',
    `last_login` datetime              DEFAULT NULL COMMENT '最后登录时间',
    `created_at` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`    int(11) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY          `idx_status` (`status`),
    KEY          `idx_role` (`role`),
    KEY          `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- EMQX系统表
-- ----------------------------
DROP TABLE IF EXISTS `emqx_system`;
CREATE TABLE `emqx_system`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        varchar(100) NOT NULL COMMENT '系统名称',
    `url`         varchar(255) NOT NULL COMMENT 'EMQX API地址',
    `username`    varchar(50)  NOT NULL COMMENT '认证用户名',
    `password`    varchar(255) NOT NULL COMMENT '认证密码（加密存储）',
    `description` text                  DEFAULT NULL COMMENT '系统描述',
    `status`      varchar(20)  NOT NULL DEFAULT 'offline' COMMENT '连接状态：online-在线，offline-离线',
    `last_check`  datetime              DEFAULT NULL COMMENT '最后检查时间',
    `created_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     int(11) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY           `idx_status` (`status`),
    KEY           `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EMQX系统表';

-- ----------------------------
-- 业务表
-- ----------------------------
DROP TABLE IF EXISTS `topic_group`;
CREATE TABLE `topic_group`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        varchar(100) NOT NULL COMMENT '业务名称',
    `description` text                  DEFAULT NULL COMMENT '业务描述',
    `topic_count` int(11) NOT NULL DEFAULT 0 COMMENT 'Topic数量（缓存字段）',
    `created_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     int(11) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY           `idx_topic_count` (`topic_count`),
    KEY           `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Topic业务表';

-- ----------------------------
-- 标签表
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        varchar(50) NOT NULL COMMENT '标签名称',
    `color`       varchar(7)           DEFAULT '#1890ff' COMMENT '标签颜色（十六进制色值）',
    `usage_count` int(11) NOT NULL DEFAULT 0 COMMENT '使用次数（缓存字段）',
    `created_at`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     int(11) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY           `idx_usage_count` (`usage_count`),
    KEY           `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ----------------------------
-- Topic表
-- ----------------------------
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`          varchar(255) NOT NULL COMMENT 'Topic名称',
    `path`          varchar(500) NOT NULL COMMENT 'Topic路径',
    `system_id`     bigint(20) NOT NULL COMMENT '所属EMQX系统ID',
    `group_id`      bigint(20) DEFAULT NULL COMMENT '所属业务ID',
    `last_activity` datetime              DEFAULT NULL COMMENT '最后活动时间',
    `payload_doc`   text                  DEFAULT NULL COMMENT 'Payload说明文档',
    `created_at`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       int(11) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_system_path` (`system_id`, `path`),
    KEY             `idx_system_id` (`system_id`),
    KEY             `idx_group_id` (`group_id`),
    KEY             `idx_last_activity` (`last_activity`),
    KEY             `idx_created_at` (`created_at`),
    CONSTRAINT `fk_topic_system` FOREIGN KEY (`system_id`) REFERENCES `emqx_system` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_topic_group` FOREIGN KEY (`group_id`) REFERENCES `topic_group` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Topic表';

-- ----------------------------
-- Topic标签关联表
-- ----------------------------
DROP TABLE IF EXISTS `topic_tag`;
CREATE TABLE `topic_tag`
(
    `id`         bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `topic_id`   bigint(20) NOT NULL COMMENT 'Topic ID',
    `tag_id`     bigint(20) NOT NULL COMMENT 'Tag ID',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`    int(11) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_topic_tag` (`topic_id`, `tag_id`),
    KEY          `idx_topic_id` (`topic_id`),
    KEY          `idx_tag_id` (`tag_id`),
    KEY          `idx_created_at` (`created_at`),
    CONSTRAINT `fk_topic_tag_topic` FOREIGN KEY (`topic_id`) REFERENCES `topic` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_topic_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Topic标签关联表';

-- ----------------------------
-- 初始化数据
-- ----------------------------

-- 插入用户数据
INSERT INTO `user` (`username`, `password`, `email`, `real_name`, `status`, `role`)
VALUES ('admin', '21232f297a57a5a743894a0e4a801fc3', 'admin@emqx.io', '系统管理员', 'active', 'admin'),
       ('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', 'zhangsan@company.com', '张三', 'active', 'user'),
       ('lisi', 'e10adc3949ba59abbe56e057f20f883e', 'lisi@company.com', '李四', 'active', 'user'),
       ('wangwu', 'e10adc3949ba59abbe56e057f20f883e', 'wangwu@company.com', '王五', 'inactive', 'user'),
       ('zhaoliu', 'e10adc3949ba59abbe56e057f20f883e', 'zhaoliu@company.com', '赵六', 'active', 'user'),
       ('developer', 'e10adc3949ba59abbe56e057f20f883e', 'dev@company.com', '开发者', 'active', 'admin');
-- 密码为: admin (MD5加密) 或 123456 (MD5加密)

-- 插入EMQX系统数据
INSERT INTO `emqx_system` (`name`, `url`, `username`, `password`, `description`, `status`, `last_check`)
VALUES ('生产环境EMQX', 'http://emqx-prod.company.com:18083', 'admin', 'public', '生产环境的EMQX集群，处理核心业务数据', 'online', NOW()),
       ('测试环境EMQX', 'http://emqx-test.company.com:18083', 'admin', 'public', '测试环境的EMQX实例，用于功能测试', 'online', NOW()),
       ('开发环境EMQX', 'http://localhost:18083', 'admin', 'public', '本地开发环境的EMQX实例', 'offline', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
       ('IoT设备集群', 'http://iot-emqx.company.com:18083', 'iot_admin', 'iot_pass', '专门处理IoT设备连接的EMQX集群', 'online', NOW()),
       ('边缘计算节点', 'http://edge-emqx.company.com:18083', 'edge_admin', 'edge_pass', '边缘计算环境的EMQX节点', 'offline', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 插入Topic业务数据
INSERT INTO `topic_group` (`name`, `description`, `topic_count`)
VALUES ('默认业务', '系统默认业务', 0),
       ('设备数据', '设备上报的数据Topic', 15),
       ('系统消息', '系统内部消息Topic', 8),
       ('用户消息', '用户相关消息Topic', 5),
       ('传感器数据', '各类传感器上报的实时数据', 12),
       ('设备控制', '设备控制指令相关Topic', 6),
       ('告警通知', '系统告警和通知消息', 4),
       ('日志数据', '系统和设备日志数据', 7),
       ('统计分析', '数据统计和分析结果', 3),
       ('第三方集成', '与第三方系统集成的消息', 2);

-- 插入标签数据
INSERT INTO `tag` (`name`, `color`, `usage_count`)
VALUES ('重要', '#ff4d4f', 25),
       ('数据', '#1890ff', 35),
       ('告警', '#faad14', 15),
       ('系统', '#52c41a', 20),
       ('用户', '#722ed1', 12),
       ('实时', '#13c2c2', 28),
       ('温度', '#eb2f96', 18),
       ('湿度', '#2f54eb', 16),
       ('压力', '#f5222d', 14),
       ('位置', '#fa8c16', 22),
       ('状态', '#a0d911', 30),
       ('控制', '#fadb14', 19),
       ('监控', '#52c41a', 24),
       ('日志', '#722ed1', 13),
       ('错误', '#ff7875', 8);

-- 插入Topic数据
INSERT INTO `topic` (`name`, `path`, `system_id`, `group_id`, `last_activity`, `payload_doc`)
VALUES 
-- 生产环境Topics
('设备温度数据', 'device/+/temperature', 1, 2, NOW(), '{"temperature": 25.5, "unit": "celsius", "timestamp": 1640995200}'),
('设备湿度数据', 'device/+/humidity', 1, 2, DATE_SUB(NOW(), INTERVAL 5 MINUTE), '{"humidity": 65.2, "unit": "percent", "timestamp": 1640995200}'),
('设备状态上报', 'device/+/status', 1, 2, DATE_SUB(NOW(), INTERVAL 2 MINUTE), '{"status": "online", "battery": 85, "signal": -45}'),
('系统心跳', 'system/heartbeat', 1, 3, NOW(), '{"node_id": "emqx@127.0.0.1", "uptime": 86400, "memory_usage": 45.2}'),
('用户登录事件', 'user/+/login', 1, 4, DATE_SUB(NOW(), INTERVAL 10 MINUTE), '{"user_id": 123, "login_time": 1640995200, "ip": "192.168.1.100"}'),
('传感器压力数据', 'sensor/pressure/+', 1, 5, DATE_SUB(NOW(), INTERVAL 3 MINUTE), '{"pressure": 1013.25, "unit": "hPa", "location": "room_01"}'),
('设备控制指令', 'control/device/+/command', 1, 6, DATE_SUB(NOW(), INTERVAL 15 MINUTE), '{"command": "turn_on", "device_id": "dev_001", "params": {}}'),
('系统告警', 'alert/system/+', 1, 7, DATE_SUB(NOW(), INTERVAL 1 HOUR), '{"level": "warning", "message": "High CPU usage", "timestamp": 1640995200}'),

-- 测试环境Topics
('测试设备数据', 'test/device/+/data', 2, 2, DATE_SUB(NOW(), INTERVAL 30 MINUTE), '{"test_data": "sample", "device_id": "test_001"}'),
('测试用户消息', 'test/user/+/message', 2, 4, DATE_SUB(NOW(), INTERVAL 45 MINUTE), '{"message": "test message", "user_id": "test_user"}'),
('测试系统日志', 'test/system/log', 2, 8, DATE_SUB(NOW(), INTERVAL 20 MINUTE), '{"level": "info", "message": "Test log entry"}'),

-- IoT设备集群Topics
('IoT设备位置', 'iot/device/+/location', 4, 5, DATE_SUB(NOW(), INTERVAL 8 MINUTE), '{"latitude": 39.9042, "longitude": 116.4074, "accuracy": 5}'),
('IoT设备电量', 'iot/device/+/battery', 4, 2, DATE_SUB(NOW(), INTERVAL 12 MINUTE), '{"battery_level": 78, "charging": false, "voltage": 3.7}'),
('IoT环境监测', 'iot/environment/+', 4, 5, DATE_SUB(NOW(), INTERVAL 6 MINUTE), '{"temperature": 22.3, "humidity": 58.7, "air_quality": 85}'),
('IoT设备告警', 'iot/alert/+', 4, 7, DATE_SUB(NOW(), INTERVAL 25 MINUTE), '{"alert_type": "low_battery", "device_id": "iot_001", "threshold": 20}'),

-- 开发环境Topics
('开发调试数据', 'dev/debug/+', 3, 8, DATE_SUB(NOW(), INTERVAL 2 HOUR), '{"debug_info": "sample debug data", "module": "auth"}'),
('开发测试消息', 'dev/test/message', 3, 3, DATE_SUB(NOW(), INTERVAL 3 HOUR), '{"test_case": "login_test", "result": "passed"}'),

-- 统计分析Topics
('数据统计结果', 'analytics/stats/+', 1, 9, DATE_SUB(NOW(), INTERVAL 1 HOUR), '{"metric": "device_count", "value": 1250, "period": "hourly"}'),
('用户行为分析', 'analytics/user_behavior', 1, 9, DATE_SUB(NOW(), INTERVAL 2 HOUR), '{"action": "page_view", "count": 156, "page": "/dashboard"}'),

-- 第三方集成Topics
('第三方API数据', 'integration/api/+', 1, 10, DATE_SUB(NOW(), INTERVAL 4 HOUR), '{"api_name": "weather_api", "data": {"city": "Beijing", "temp": 15}}'),
('外部系统通知', 'integration/notification', 1, 10, DATE_SUB(NOW(), INTERVAL 6 HOUR), '{"source": "external_system", "type": "update", "content": "Data sync completed"}');

-- 插入Topic标签关联数据
INSERT INTO `topic_tag` (`topic_id`, `tag_id`)
VALUES 
-- 设备温度数据的标签
(1, 1), (1, 2), (1, 6), (1, 7),
-- 设备湿度数据的标签
(2, 2), (2, 6), (2, 8),
-- 设备状态上报的标签
(3, 1), (3, 2), (3, 11),
-- 系统心跳的标签
(4, 4), (4, 13),
-- 用户登录事件的标签
(5, 5), (5, 14),
-- 传感器压力数据的标签
(6, 2), (6, 6), (6, 9),
-- 设备控制指令的标签
(7, 1), (7, 12),
-- 系统告警的标签
(8, 1), (8, 3), (8, 15),
-- 测试设备数据的标签
(9, 2), (9, 4),
-- 测试用户消息的标签
(10, 5), (10, 4),
-- 测试系统日志的标签
(11, 4), (11, 14),
-- IoT设备位置的标签
(12, 2), (12, 10),
-- IoT设备电量的标签
(13, 2), (13, 11),
-- IoT环境监测的标签
(14, 1), (14, 2), (14, 6), (14, 13),
-- IoT设备告警的标签
(15, 1), (15, 3),
-- 开发调试数据的标签
(16, 4), (16, 14),
-- 开发测试消息的标签
(17, 4),
-- 数据统计结果的标签
(18, 2), (18, 13),
-- 用户行为分析的标签
(19, 5), (19, 2),
-- 第三方API数据的标签
(20, 2), (20, 4),
-- 外部系统通知的标签
(21, 4), (21, 13);

SET
FOREIGN_KEY_CHECKS = 1;