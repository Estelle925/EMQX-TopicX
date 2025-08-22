-- EMQX Topic Hub 数据库表结构
-- 创建数据库
CREATE DATABASE IF NOT EXISTS emqx_topic_hub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE emqx_topic_hub;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `email` VARCHAR(100) COMMENT '邮箱',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '用户状态：active-活跃，inactive-非活跃，locked-锁定',
    `role` VARCHAR(20) DEFAULT 'user' COMMENT '角色：admin-管理员，user-普通用户',
    `last_login` DATETIME COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    INDEX `idx_username` (`username`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- EMQX系统表
CREATE TABLE IF NOT EXISTS `emqx_system` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '系统名称',
    `url` VARCHAR(255) NOT NULL COMMENT 'EMQX API地址',
    `username` VARCHAR(50) NOT NULL COMMENT '认证用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '认证密码（加密存储）',
    `description` TEXT COMMENT '系统描述',
    `status` VARCHAR(20) DEFAULT 'offline' COMMENT '连接状态：online-在线，offline-离线',
    `last_check` DATETIME COMMENT '最后检查时间',
    `connections` INT DEFAULT 0 COMMENT '连接数（缓存字段）',
    `messages` BIGINT DEFAULT 0 COMMENT '消息数（缓存字段）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    INDEX `idx_name` (`name`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='EMQX系统表';

-- 分组表
CREATE TABLE IF NOT EXISTS `topic_group` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分组名称',
    `description` TEXT COMMENT '分组描述',
    `topic_count` INT DEFAULT 0 COMMENT 'Topic数量（缓存字段）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    INDEX `idx_name` (`name`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Topic分组表';

-- 标签表
CREATE TABLE IF NOT EXISTS `tag` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(7) NOT NULL DEFAULT '#409EFF' COMMENT '标签颜色（十六进制色值）',
    `usage_count` INT DEFAULT 0 COMMENT '使用次数（缓存字段）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    UNIQUE KEY `uk_name` (`name`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- Topic表
CREATE TABLE IF NOT EXISTS `topic` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `name` VARCHAR(255) NOT NULL COMMENT 'Topic名称',
    `path` VARCHAR(255) NOT NULL COMMENT 'Topic路径',
    `system_id` BIGINT NOT NULL COMMENT '所属EMQX系统ID',
    `group_id` BIGINT COMMENT '所属分组ID',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT 'Topic状态：active-活跃，inactive-非活跃',
    `client_count` INT DEFAULT 0 COMMENT '客户端连接数（缓存字段）',
    `message_count` BIGINT DEFAULT 0 COMMENT '消息数量（缓存字段）',
    `last_activity` DATETIME COMMENT '最后活动时间',
    `payload_doc` TEXT COMMENT 'Payload说明文档',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    INDEX `idx_name` (`name`),
    INDEX `idx_path` (`path`),
    INDEX `idx_system_id` (`system_id`),
    INDEX `idx_group_id` (`group_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`),
    FOREIGN KEY (`system_id`) REFERENCES `emqx_system`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`group_id`) REFERENCES `topic_group`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Topic表';

-- Topic标签关联表
CREATE TABLE IF NOT EXISTS `topic_tag` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `topic_id` BIGINT NOT NULL COMMENT 'Topic ID',
    `tag_id` BIGINT NOT NULL COMMENT 'Tag ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    UNIQUE KEY `uk_topic_tag` (`topic_id`, `tag_id`),
    INDEX `idx_topic_id` (`topic_id`),
    INDEX `idx_tag_id` (`tag_id`),
    INDEX `idx_created_at` (`created_at`),
    FOREIGN KEY (`topic_id`) REFERENCES `topic`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`tag_id`) REFERENCES `tag`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Topic标签关联表';

-- 插入默认管理员用户
INSERT INTO `user` (`username`, `password`, `email`, `real_name`, `role`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'admin@example.com', '系统管理员', 'admin')
ON DUPLICATE KEY UPDATE `username` = `username`;

-- 插入默认分组
INSERT INTO `topic_group` (`name`, `description`) VALUES 
('设备遥测', '设备遥测数据相关的Topic'),
('传感器数据', '各类传感器数据Topic'),
('系统告警', '系统告警和通知Topic')
ON DUPLICATE KEY UPDATE `name` = `name`;

-- 插入默认标签
INSERT INTO `tag` (`name`, `color`) VALUES 
('重要', '#f56c6c'),
('实时', '#67c23a'),
('温度', '#409eff'),
('告警', '#e6a23c'),
('监控', '#ed8936')
ON DUPLICATE KEY UPDATE `name` = `name`;