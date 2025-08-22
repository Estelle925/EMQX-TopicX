package com.emqx.topichub.controller;

import com.emqx.topichub.common.Result;
import com.emqx.topichub.dto.DashboardStatsDTO;
import com.emqx.topichub.dto.SystemStatusDTO;
import com.emqx.topichub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表板控制器
 * 提供仪表板页面所需的统计数据和系统状态信息
 * 
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取仪表板统计数据
     * 
     * @return 统计数据
     */
    @GetMapping("/stats")
    public Result<DashboardStatsDTO> getStats() {
        DashboardStatsDTO stats = dashboardService.getStats();
        return Result.success(stats);
    }

    /**
     * 获取系统状态列表
     * 
     * @return 系统状态列表
     */
    @GetMapping("/systems/status")
    public Result<List<SystemStatusDTO>> getSystemStatus() {
        List<SystemStatusDTO> systemStatus = dashboardService.getSystemStatus();
        return Result.success(systemStatus);
    }

    /**
     * 刷新系统状态
     * 主动检查所有EMQX系统的连接状态
     * 
     * @return 更新后的系统状态列表
     */
    @GetMapping("/systems/refresh")
    public Result<List<SystemStatusDTO>> refreshSystemStatus() {
        List<SystemStatusDTO> systemStatus = dashboardService.refreshSystemStatus();
        return Result.success(systemStatus);
    }
}