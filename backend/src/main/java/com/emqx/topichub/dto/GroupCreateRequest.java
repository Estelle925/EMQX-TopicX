package com.emqx.topichub.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建分组请求对象
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class GroupCreateRequest {

    /**
     * 分组名称
     */
    @NotBlank(message = "分组名称不能为空")
    @Size(max = 100, message = "分组名称长度不能超过100个字符")
    private String name;

    /**
     * 分组描述
     */
    @Size(max = 500, message = "分组描述长度不能超过500个字符")
    private String description;
}