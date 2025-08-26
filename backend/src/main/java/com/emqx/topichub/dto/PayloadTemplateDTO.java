package com.emqx.topichub.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Payload模板响应DTO
 * 用于返回Payload模板信息
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class PayloadTemplateDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 所属业务ID
     */
    private Long groupId;

    /**
     * 业务分组名称
     */
    private String groupName;

    /**
     * 模板类型 (JSON, XML, Text, Binary)
     */
    private String type;

    /**
     * 模板描述
     */
    private String description;

    /**
     * Payload内容
     */
    private String payload;

    /**
     * 使用次数
     */
    private Integer usageCount;

    /**
     * 是否收藏
     */
    private Boolean isFavorite;

    /**
     * 最近使用时间
     */
    private LocalDateTime lastUsed;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}