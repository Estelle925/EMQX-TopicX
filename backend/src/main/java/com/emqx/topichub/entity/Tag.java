package com.emqx.topichub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 标签实体类
 * 用于Topic标签管理
 * 
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tag")
public class Tag {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    @TableField("name")
    private String name;

    /**
     * 标签颜色（十六进制色值）
     */
    @TableField("color")
    private String color;

    /**
     * 使用次数（缓存字段）
     */
    @TableField("usage_count")
    private Integer usageCount;

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