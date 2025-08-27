package com.emqx.topichub.controller;

import com.emqx.topichub.common.Result;
import com.emqx.topichub.dto.*;
import com.emqx.topichub.service.EmqxSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统管理控制器
 * 提供EMQX系统管理相关的API接口
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/systems")
@RequiredArgsConstructor
public class SystemController {

    private final EmqxSystemService emqxSystemService;

    /**
     * 获取所有系统列表
     *
     * @return 系统列表
     */
    @GetMapping
    public Result<List<SystemManagementDTO>> getAllSystems() {
        List<SystemManagementDTO> systems = emqxSystemService.getAllSystems();
        return Result.success(systems);
    }

    /**
     * 根据关键词搜索系统
     *
     * @param keyword 搜索关键词
     * @return 系统列表
     */
    @GetMapping("/search")
    public Result<List<SystemManagementDTO>> searchSystems(@RequestParam(required = false) String keyword) {
        List<SystemManagementDTO> systems = emqxSystemService.searchSystems(keyword);
        return Result.success(systems);
    }

    /**
     * 根据ID获取系统详情
     *
     * @param id 系统ID
     * @return 系统详情
     */
    @GetMapping("/{id}")
    public Result<SystemManagementDTO> getSystemById(@PathVariable("id") Long id) {
        List<SystemManagementDTO> systems = emqxSystemService.getAllSystems();
        SystemManagementDTO system = systems.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (system == null) {
            return Result.error("系统不存在");
        }

        return Result.success(system);
    }

    /**
     * 创建新系统
     *
     * @param request 创建请求
     * @return 创建的系统信息
     */
    @PostMapping
    public Result<SystemManagementDTO> createSystem(@Valid @RequestBody SystemCreateRequest request) {
        try {
            SystemManagementDTO system = emqxSystemService.createSystem(request);
            return Result.success("系统创建成功", system);
        } catch (Exception e) {
            return Result.error("创建系统失败: " + e.getMessage());
        }
    }

    /**
     * 更新系统信息
     *
     * @param id      系统ID
     * @param request 更新请求
     * @return 更新后的系统信息
     */
    @PutMapping("/{id}")
    public Result<SystemManagementDTO> updateSystem(@PathVariable("id") Long id,
                                                    @Valid @RequestBody SystemUpdateRequest request) {
        try {
            SystemManagementDTO system = emqxSystemService.updateSystem(id, request);
            return Result.success("系统更新成功", system);
        } catch (Exception e) {
            return Result.error("更新系统失败: " + e.getMessage());
        }
    }

    /**
     * 删除系统
     *
     * @param id 系统ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSystem(@PathVariable("id") Long id) {
        try {
            emqxSystemService.deleteSystem(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除系统失败: " + e.getMessage());
        }
    }

    /**
     * 测试系统连接
     *
     * @param id 系统ID
     * @return 连接测试结果
     */
    @PostMapping("/{id}/test-connection")
    public Result<ConnectionTestResult> testConnection(@PathVariable("id") Long id) {
        try {
            ConnectionTestResult result = emqxSystemService.testConnectionDetailed(id);
            String message = result.getSuccess() ? "连接测试成功" : "连接测试失败";
            return Result.success(message, result);
        } catch (Exception e) {
            return Result.error("连接测试异常: " + e.getMessage());
        }
    }

    /**
     * 刷新所有系统状态
     *
     * @return 更新后的系统列表
     */
    @PostMapping("/refresh-status")
    public Result<List<SystemManagementDTO>> refreshSystemStatus() {
        try {
            List<SystemManagementDTO> systems = emqxSystemService.refreshAllSystemStatus();
            return Result.success("系统状态刷新成功", systems);
        } catch (Exception e) {
            return Result.error("刷新系统状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/stats")
    public Result<SystemStatsDTO> getSystemStats() {
        try {
            SystemStatsDTO stats = emqxSystemService.getSystemStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }
}