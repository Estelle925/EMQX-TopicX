package com.emqx.topichub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 分组实体类
 * 用于Topic分组管理
 * 
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("topic_group")
public class Group {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分组名称
     */
    @TableField("name")
    private String name;

    /**
     * 分组描述
     */
    @TableField("description")
    private String description;

    /**
     * Topic数量（缓存字段）
     */
    @TableField("topic_count")
    private Integer topicCount;

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