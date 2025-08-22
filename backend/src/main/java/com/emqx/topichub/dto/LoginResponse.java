package com.emqx.topichub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应结果
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT访问令牌
     */
    private String token;

    /**
     * 用户名
     */
    private String username;

    /**
     * 令牌过期时间（毫秒时间戳）
     */
    private Long expiresAt;

}