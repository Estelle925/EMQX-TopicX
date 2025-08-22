package com.emqx.topichub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emqx.topichub.dto.ConnectionTestResult;
import com.emqx.topichub.dto.SystemCreateRequest;
import com.emqx.topichub.dto.SystemManagementDTO;
import com.emqx.topichub.dto.SystemUpdateRequest;
import com.emqx.topichub.entity.EmqxSystem;
import com.emqx.topichub.mapper.EmqxSystemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

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
    
    private final RestTemplate restTemplate = new RestTemplate();
    
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
        system.setStatus("offline");
        
        this.save(system);
        return convertToDTO(system);
    }
    
    /**
     * 更新系统信息
     * 
     * @param id 系统ID
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
            // 解密密码
            String password = new String(Base64.getDecoder().decode(system.getPassword()));
            
            // 创建认证头
            HttpHeaders headers = new HttpHeaders();
            String auth = system.getUsername() + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 调用EMQX API测试连接
            String apiUrl = system.getUrl() + "/api/v5/status";
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl, HttpMethod.GET, entity, String.class);
            
            long responseTime = System.currentTimeMillis() - startTime;
            boolean isOnline = response.getStatusCode().is2xxSuccessful();
            
            // 更新系统状态
            system.setStatus(isOnline ? "online" : "offline");
            system.setLastCheck(LocalDateTime.now());
            this.updateById(system);
            
            if (isOnline) {
                // 解析响应获取版本和节点信息
                String responseBody = response.getBody();
                String version = "Unknown";
                String nodeInfo = "Unknown";
                
                // TODO: 解析EMQX API响应获取版本和节点信息
                // 这里可以根据实际的EMQX API响应格式进行解析
                
                return ConnectionTestResult.success(id, system.getName(), responseTime, version, nodeInfo);
            } else {
                return ConnectionTestResult.failure(id, system.getName(), "HTTP状态码: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("测试系统连接失败: {}", e.getMessage());
            
            // 更新为离线状态
            system.setStatus("offline");
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
        
        long onlineCount = systems.stream().filter(s -> "online".equals(s.getStatus())).count();
        long offlineCount = systems.stream().filter(s -> "offline".equals(s.getStatus())).count();
        long totalSystems = systems.size();
        
        // TODO: 计算总Topic数，需要从各个在线系统获取
        long totalTopics = 0;
        
        return new SystemStatsDTO(onlineCount, offlineCount, totalSystems, totalTopics);
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
        
        // TODO: 从EMQX API获取实时的Topic数和连接数
        dto.setTopicCount(0);
        dto.setConnectionCount(0);
        
        return dto;
    }
    
    /**
     * 系统统计DTO
     */
    public static class SystemStatsDTO {
        private final long onlineCount;
        private final long offlineCount;
        private final long totalSystems;
        private final long totalTopics;
        
        public SystemStatsDTO(long onlineCount, long offlineCount, long totalSystems, long totalTopics) {
            this.onlineCount = onlineCount;
            this.offlineCount = offlineCount;
            this.totalSystems = totalSystems;
            this.totalTopics = totalTopics;
        }
        
        public long getOnlineCount() { return onlineCount; }
        public long getOfflineCount() { return offlineCount; }
        public long getTotalSystems() { return totalSystems; }
        public long getTotalTopics() { return totalTopics; }
    }
}
