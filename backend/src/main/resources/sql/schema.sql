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
-- 分组表
-- ----------------------------
DROP TABLE IF EXISTS `topic_group`;
CREATE TABLE `topic_group`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        varchar(100) NOT NULL COMMENT '分组名称',
    `description` text                  DEFAULT NULL COMMENT '分组描述',
    `topic_count` int(11) NOT NULL DEFAULT 0 COMMENT 'Topic数量（缓存字段）',
    `created_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     int(11) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY           `idx_topic_count` (`topic_count`),
    KEY           `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Topic分组表';

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
    `group_id`      bigint(20) DEFAULT NULL COMMENT '所属分组ID',
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

-- 插入默认管理员用户
INSERT INTO `user` (`username`, `password`, `email`, `real_name`, `status`, `role`)
VALUES ('admin', '21232f297a57a5a743894a0e4a801fc3', 'admin@emqx.io', '系统管理员', 'active', 'admin');
-- 密码为: admin (MD5加密)

-- 插入默认分组
INSERT INTO `topic_group` (`name`, `description`)
VALUES ('默认分组', '系统默认分组'),
       ('设备数据', '设备上报的数据Topic'),
       ('系统消息', '系统内部消息Topic'),
       ('用户消息', '用户相关消息Topic');

-- 插入默认标签
INSERT INTO `tag` (`name`, `color`)
VALUES ('重要', '#ff4d4f'),
       ('数据', '#1890ff'),
       ('告警', '#faad14'),
       ('系统', '#52c41a'),
       ('用户', '#722ed1');

SET
FOREIGN_KEY_CHECKS = 1;