package com.emqx.topichub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统管理DTO
 * 用于系统管理页面的数据传输
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemManagementDTO {

    /**
     * 系统ID
     */
    private Long id;

    /**
     * 系统名称
     */
    private String name;

    /**
     * EMQX API地址
     */
    private String url;

    /**
     * 认证用户名
     */
    private String username;

    /**
     * 系统描述
     */
    private String description;

    /**
     * 连接状态：online-在线，offline-离线
     */
    private String status;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheck;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * Topic总数（从EMQX API获取）
     */
    private Integer topicCount;

    /**
     * 连接数（从EMQX API获取）
     */
    private Integer connectionCount;

    /**
     * 是否正在测试连接
     */
    private Boolean testing;
}