package com.emqx.topichub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emqx.topichub.dto.*;
import com.emqx.topichub.entity.EmqxSystem;
import com.emqx.topichub.mapper.EmqxSystemMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;


/**
 * EMQX系统服务类
 * 提供EMQX系统的管理功能
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class EmqxSystemService extends ServiceImpl<EmqxSystemMapper, EmqxSystem> {

    // ========== 常量定义 ==========
    
    /** 系统状态常量 */
    private static final String STATUS_ONLINE = "online";
    private static final String STATUS_OFFLINE = "offline";
    
    /** 默认值常量 */
    private static final String DEFAULT_UNKNOWN = "Unknown";
    private static final int DEFAULT_COUNT = 0;
    private static final String FIELD_TOPICS = "topics.count";
    private static final String FIELD_CONNECTIONS = "connections.count";

    @Resource
    private EmqxService emqxService;

    /**
     * 获取所有系统列表
     *
     * @return 系统管理DTO列表
     */
    public List<SystemManagementDTO> getAllSystems() {
        List<EmqxSystem> systems = this.list();
        return systems.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 根据关键词搜索系统
     *
     * @param keyword 搜索关键词
     * @return 系统管理DTO列表
     */
    public List<SystemManagementDTO> searchSystems(String keyword) {
        LambdaQueryWrapper<EmqxSystem> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(EmqxSystem::getName, keyword)
                    .or()
                    .like(EmqxSystem::getUrl, keyword);
        }
        List<EmqxSystem> systems = this.list(wrapper);
        return systems.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 创建新系统
     *
     * @param request 创建请求
     * @return 创建的系统DTO
     */
    public SystemManagementDTO createSystem(SystemCreateRequest request) {
        EmqxSystem system = new EmqxSystem();
        BeanUtils.copyProperties(request, system);

        // 密码加密存储（这里简单使用Base64，实际项目中应使用更安全的加密方式）
        system.setPassword(Base64.getEncoder().encodeToString(request.getPassword().getBytes()));
        // 默认离线状态
        system.setStatus(STATUS_OFFLINE);

        this.save(system);
        return convertToDTO(system);
    }

    /**
     * 更新系统信息
     *
     * @param id      系统ID
     * @param request 更新请求
     * @return 更新后的系统DTO
     */
    public SystemManagementDTO updateSystem(Long id, SystemUpdateRequest request) {
        EmqxSystem system = this.getById(id);
        if (system == null) {
            throw new RuntimeException("系统不存在");
        }

        BeanUtils.copyProperties(request, system);

        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(request.getPassword())) {
            system.setPassword(Base64.getEncoder().encodeToString(request.getPassword().getBytes()));
        }

        this.updateById(system);
        return convertToDTO(system);
    }

    /**
     * 删除系统
     *
     * @param id 系统ID
     */
    public void deleteSystem(Long id) {
        EmqxSystem system = this.getById(id);
        if (system == null) {
            throw new RuntimeException("系统不存在");
        }
        this.removeById(id);
    }

    /**
     * 测试系统连接
     *
     * @param id 系统ID
     * @return 连接测试结果
     */
    public boolean testConnection(Long id) {
        ConnectionTestResult result = testConnectionDetailed(id);
        return result.getSuccess();
    }

    /**
     * 测试系统连接（详细结果）
     *
     * @param id 系统ID
     * @return 详细的连接测试结果
     */
    public ConnectionTestResult testConnectionDetailed(Long id) {
        EmqxSystem system = this.getById(id);
        if (system == null) {
            throw new RuntimeException("系统不存在");
        }

        long startTime = System.currentTimeMillis();

        try {
            // 使用EmqxService测试连接
            boolean isOnline = emqxService.testConnection(system);
            long responseTime = System.currentTimeMillis() - startTime;

            // 更新系统状态
            system.setStatus(isOnline ? STATUS_ONLINE : STATUS_OFFLINE);
            system.setLastCheck(LocalDateTime.now());
            this.updateById(system);

            if (isOnline) {
                // 获取系统状态信息
                String responseBody = emqxService.getSystemStatus(system);
                String version = DEFAULT_UNKNOWN;
                String nodeInfo = DEFAULT_UNKNOWN;

                // 解析EMQX API响应获取版本和节点信息
                version = emqxService.parseVersionFromResponse(responseBody);
                nodeInfo = emqxService.parseNodeInfoFromResponse(responseBody);

                return ConnectionTestResult.success(id, system.getName(), responseTime, version, nodeInfo);
            } else {
                return ConnectionTestResult.failure(id, system.getName(), "连接失败");
            }

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("测试系统连接失败: {}", e.getMessage());

            // 更新为离线状态
            system.setStatus(STATUS_OFFLINE);
            system.setLastCheck(LocalDateTime.now());
            this.updateById(system);

            return ConnectionTestResult.failure(id, system.getName(), e.getMessage());
        }
    }

    /**
     * 刷新所有系统状态
     *
     * @return 更新后的系统列表
     */
    public List<SystemManagementDTO> refreshAllSystemStatus() {
        List<EmqxSystem> systems = this.list();

        for (EmqxSystem system : systems) {
            testConnection(system.getId());
        }

        return getAllSystems();
    }

    /**
     * 获取系统统计信息
     *
     * @return 统计信息
     */
    public SystemStatsDTO getSystemStats() {
        List<EmqxSystem> systems = this.list();

        long onlineCount = systems.stream().filter(s -> STATUS_ONLINE.equals(s.getStatus())).count();
        long offlineCount = systems.stream().filter(s -> STATUS_OFFLINE.equals(s.getStatus())).count();
        long totalSystems = systems.size();

        // 计算总Topic数，从各个在线系统获取
        long totalTopics = calculateTotalTopicsFromOnlineSystems();

        SystemStatsDTO stats = new SystemStatsDTO(onlineCount, offlineCount, totalSystems, totalTopics);

        // 获取实时主题和连接数统计
        fetchRealTimeTopicAndConnectionCounts(stats);

        return stats;
    }





    /**
     * 计算在线系统的总主题数
     *
     * @return 总主题数
     */
    private long calculateTotalTopicsFromOnlineSystems() {
        try {
            // 获取所有在线的EMQX系统
            LambdaQueryWrapper<EmqxSystem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EmqxSystem::getStatus, STATUS_ONLINE);
            List<EmqxSystem> onlineSystems = this.list(wrapper);

            long totalTopics = 0L;

            for (EmqxSystem system : onlineSystems) {
                try {
                    // 使用EmqxService获取统计信息
                    String responseBody = emqxService.getSystemStats(system);
                    long topicCount = emqxService.parseCountFromNode(responseBody,FIELD_TOPICS);
                    totalTopics += topicCount;
                } catch (Exception e) {
                    log.warn("获取系统 {} 的主题数量失败: {}", system.getName(), e.getMessage());
                }
            }

            return totalTopics;
        } catch (Exception e) {
            log.error("计算在线系统总主题数时发生错误", e);
            return DEFAULT_COUNT;
        }
    }



    /**
     * 获取实时主题和连接数统计
     *
     * @param stats 统计信息DTO
     */
    private void fetchRealTimeTopicAndConnectionCounts(SystemStatsDTO stats) {
        try {
            // 获取所有在线的EMQX系统
            LambdaQueryWrapper<EmqxSystem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EmqxSystem::getStatus, STATUS_ONLINE);
            List<EmqxSystem> onlineSystems = this.list(wrapper);

            long totalTopics = 0L;

            for (EmqxSystem system : onlineSystems) {
                try {
                    // 使用EmqxService获取统计信息
                    String responseBody = emqxService.getSystemStats(system);
                    
                    // 解析主题数
                    long topicCount = emqxService.parseCountFromNode(responseBody,FIELD_TOPICS);
                    totalTopics += topicCount;
                } catch (Exception e) {
                    log.warn("获取系统 {} 的实时统计信息失败: {}", system.getName(), e.getMessage());
                }
            }

            // 更新总主题数
            stats.setTotalTopics(totalTopics);

        } catch (Exception e) {
            log.error("获取实时主题和连接数统计时发生错误", e);
        }
    }



    /**
     * 转换实体为DTO
     *
     * @param system 系统实体
     * @return 系统DTO
     */
    private SystemManagementDTO convertToDTO(EmqxSystem system) {
        SystemManagementDTO dto = new SystemManagementDTO();
        BeanUtils.copyProperties(system, dto);

        // 不返回密码信息
        dto.setTesting(false);

        // 从EMQX API获取实时的Topic数和连接数
        try {
            if (STATUS_ONLINE.equals(system.getStatus())) {
                // 使用EmqxService获取统计信息
                String responseBody = emqxService.getSystemStats(system);

                // 解析主题数和连接数
                long topicCount = emqxService.parseCountFromNode(responseBody, FIELD_TOPICS);
                long connectionCount = emqxService.parseCountFromNode(responseBody, FIELD_CONNECTIONS);

                dto.setTopicCount((int) topicCount);
                dto.setConnectionCount((int) connectionCount);
            } else {
                // 离线系统设置为0
                dto.setTopicCount(DEFAULT_COUNT);
                dto.setConnectionCount(DEFAULT_COUNT);
            }
        } catch (Exception e) {
            log.warn("获取系统 {} 的实时统计信息失败: {}", system.getName(), e.getMessage());
            dto.setTopicCount(DEFAULT_COUNT);
            dto.setConnectionCount(DEFAULT_COUNT);
        }

        return dto;
    }
}
