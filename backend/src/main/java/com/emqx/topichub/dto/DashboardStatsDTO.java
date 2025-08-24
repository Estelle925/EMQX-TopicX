package com.emqx.topichub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表板统计数据DTO
 * 用于封装仪表板页面所需的统计信息
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    /**
     * EMQX系统数量
     */
    private Integer systemCount;

    /**
     * Topic总数
     */
    private Integer topicCount;

    /**
     * 业务数量
     */
    private Integer groupCount;

    /**
     * 标签数量
     */
    private Integer tagCount;
}