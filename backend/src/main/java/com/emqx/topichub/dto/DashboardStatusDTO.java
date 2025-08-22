package com.emqx.topichub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统状态DTO
 * 用于封装EMQX系统的状态信息
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatusDTO {

    /**
     * 系统ID
     */
    private Long id;

    /**
     * 系统名称
     */
    private String name;

    /**
     * 系统URL
     */
    private String url;

    /**
     * 系统状态：online-在线，offline-离线
     */
    private String status;

    /**
     * 连接数
     */
    private Integer topicCount;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheck;
}