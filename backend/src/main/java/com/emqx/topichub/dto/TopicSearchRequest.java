package com.emqx.topichub.dto;

import lombok.Data;

import java.util.List;

/**
 * Topic搜索请求DTO
 * 用于接收Topic搜索和筛选的请求参数
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class TopicSearchRequest {

    /**
     * 搜索关键字（Topic名称或路径）
     */
    private String keyword;

    /**
     * 业务ID筛选
     */
    private Long groupId;

    /**
     * 标签ID列表筛选
     */
    private List<Long> tagIds;

    /**
     * 状态筛选（enabled/disabled）
     */
    private String status;

    /**
     * EMQX系统ID筛选
     */
    private Long systemId;

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 20;

    /**
     * 排序字段
     */
    private String sortBy = "createdAt";

    /**
     * 排序方向（asc/desc）
     */
    private String sortDir = "desc";
}