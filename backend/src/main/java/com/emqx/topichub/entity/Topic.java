package com.emqx.topichub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Topic实体类
 * 用于管理MQTT Topic信息
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("topic")
public class Topic {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Topic名称
     */
    @TableField("name")
    private String name;

    /**
     * Topic路径
     */
    @TableField("path")
    private String path;

    /**
     * 所属EMQX系统ID
     */
    @TableField("system_id")
    private Long systemId;

    /**
     * 所属业务ID
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 最后活动时间
     */
    @TableField("last_activity")
    private LocalDateTime lastActivity;

    /**
     * Payload说明文档
     */
    @TableField("payload_doc")
    private String payloadDoc;

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