package com.emqx.topichub.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签数据传输对象
 * 用于标签管理页面的数据传输
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class TagDTO {

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签颜色（十六进制色值）
     */
    private String color;

    /**
     * 使用次数
     */
    private Integer usageCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}