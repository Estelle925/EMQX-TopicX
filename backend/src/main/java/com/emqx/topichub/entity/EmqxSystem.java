package com.emqx.topichub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * EMQX系统实体类
 * 用于管理多个EMQX服务器实例
 * 
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("emqx_system")
public class EmqxSystem {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 系统名称
     */
    @TableField("name")
    private String name;

    /**
     * EMQX API地址
     */
    @TableField("url")
    private String url;

    /**
     * 认证用户名
     */
    @TableField("username")
    private String username;

    /**
     * 认证密码（加密存储）
     */
    @TableField("password")
    private String password;

    /**
     * 系统描述
     */
    @TableField("description")
    private String description;

    /**
     * 连接状态：online-在线，offline-离线
     */
    @TableField("status")
    private String status;

    /**
     * 最后检查时间
     */
    @TableField("last_check")
    private LocalDateTime lastCheck;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}