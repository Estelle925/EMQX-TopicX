package com.emqx.topichub.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户响应DTO
 * 用于返回单个用户的详细信息
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class UserResponse {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 用户状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;
}