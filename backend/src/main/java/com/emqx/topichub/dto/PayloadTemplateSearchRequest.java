package com.emqx.topichub.dto;

import lombok.Data;

/**
 * Payload模板搜索请求DTO
 * 用于接收搜索Payload模板的请求参数
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class PayloadTemplateSearchRequest {

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 页码（兼容前端page字段）
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 搜索关键词（模板名称或描述）
     */
    private String keyword;

    /**
     * 业务分组ID
     */
    private Long groupId;

    /**
     * 模板类型
     */
    private String type;

    /**
     * 是否只显示收藏的模板
     */
    private Boolean onlyFavorites;

    /**
     * 排序字段（name, usageCount, lastUsed, createdAt）
     */
    private String sortBy = "createdAt";

    /**
     * 排序方向（asc, desc）
     */
    private String sortOrder = "desc";
}