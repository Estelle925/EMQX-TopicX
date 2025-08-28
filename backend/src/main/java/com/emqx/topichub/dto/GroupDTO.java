package com.emqx.topichub.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务数据传输对象
 * 用于业务管理页面的数据传输
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class GroupDTO {

    /**
     * 业务ID
     */
    private Long id;

    /**
     * 业务名称
     */
    private String name;

    /**
     * 业务描述
     */
    private String description;

    /**
     * Topic数量
     */
    private Integer topicCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}