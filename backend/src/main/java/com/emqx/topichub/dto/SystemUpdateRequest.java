package com.emqx.topichub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新系统请求DTO
 * 用于接收更新EMQX系统的请求参数
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class SystemUpdateRequest {

    /**
     * 系统名称
     */
    @NotBlank(message = "系统名称不能为空")
    @Size(max = 100, message = "系统名称长度不能超过100个字符")
    private String name;

    /**
     * EMQX API地址
     */
    @NotBlank(message = "API地址不能为空")
    @Size(max = 255, message = "API地址长度不能超过255个字符")
    private String url;

    /**
     * 认证用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    /**
     * 认证密码（可选，如果不提供则不更新密码）
     */
    @Size(max = 100, message = "密码长度不能超过100个字符")
    private String password;

    /**
     * 系统描述
     */
    @Size(max = 500, message = "系统描述长度不能超过500个字符")
    private String description;
}