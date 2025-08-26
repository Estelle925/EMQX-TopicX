package com.emqx.topichub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Payload模板实体类
 * 用于管理MQTT Payload模板信息
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("payload_template")
public class PayloadTemplate {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    @TableField("name")
    private String name;

    /**
     * 所属业务ID
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 模板类型 (JSON, XML, Text, Binary)
     */
    @TableField("type")
    private String type;

    /**
     * 模板描述
     */
    @TableField("description")
    private String description;

    /**
     * Payload内容
     */
    @TableField("content")
    private String content;

    /**
     * 使用次数
     */
    @TableField("usage_count")
    private Integer usageCount;

    /**
     * 是否收藏
     */
    @TableField("is_favorite")
    private Boolean isFavorite;

    /**
     * 最近使用时间
     */
    @TableField("last_used_at")
    private LocalDateTime lastUsed;

    /**
     * 创建时间
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}