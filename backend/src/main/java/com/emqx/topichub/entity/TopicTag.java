package com.emqx.topichub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Topic标签关联实体类
 * 用于管理Topic和Tag的多对多关系
 * 
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("topic_tag")
public class TopicTag {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Topic ID
     */
    @TableField("topic_id")
    private Long topicId;

    /**
     * Tag ID
     */
    @TableField("tag_id")
    private Long tagId;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}