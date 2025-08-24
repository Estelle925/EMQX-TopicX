package com.emqx.topichub.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表响应DTO
 * 用于返回用户列表数据
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class UserListResponse {

    /**
     * 用户列表
     */
    private List<UserInfo> users;

    /**
     * 总数
     */
    private Long total;

    /**
     * 当前页
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 用户信息
     */
    @Data
    public static class UserInfo {
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
         * 最后登录时间
         */
        private LocalDateTime lastLoginAt;
    }
}