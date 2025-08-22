package com.emqx.topichub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class LoginRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 选择的EMQX系统ID
     */
    private Long emqxSystemId;

}