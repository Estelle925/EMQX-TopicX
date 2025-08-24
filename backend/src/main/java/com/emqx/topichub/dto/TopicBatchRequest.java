package com.emqx.topichub.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Topic批量操作请求DTO
 * 用于接收批量操作Topic的请求参数
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class TopicBatchRequest {

    /**
     * Topic ID列表
     */
    @NotEmpty(message = "Topic ID列表不能为空")
    private List<Long> topicIds;

    /**
     * 操作类型：assignGroup（分配业务）、addTags（添加标签）、removeTags（移除标签）
     */
    @NotNull(message = "操作类型不能为空")
    private String action;

    /**
     * 业务ID（当action为assignGroup时使用）
     */
    private Long groupId;

    /**
     * 标签ID列表（当action为addTags或removeTags时使用）
     */
    private List<Long> tagIds;
}