package com.emqx.topichub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * 系统统计信息数据传输对象
 * 用于封装系统运行状态的统计信息
 *
 * @author EMQX Topic Hub Team
 */
@Data
@AllArgsConstructor
public class SystemStatsDTO {
    /**
     * 在线系统数量
     * 记录当前处于在线状态的系统实例数量
     */
    private Long onlineCount;

    /**
     * 离线系统数量
     * 记录当前处于离线状态的系统实例数量
     */
    private Long offlineCount;

    /**
     * 系统总数量
     * 记录所有已注册系统的总数量，包括在线和离线状态
     */
    private Long totalSystems;

    /**
     * 主题总数量
     * 记录系统中所有消息主题的总数量
     */
    private Long totalTopics;

}
