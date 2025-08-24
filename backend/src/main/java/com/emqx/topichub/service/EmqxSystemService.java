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

    // ========== 常量定义 ==========
    
    /** 系统状态常量 */
    private static final String STATUS_ONLINE = "online";
    private static final String STATUS_OFFLINE = "offline";
    
    /** EMQX API 路径常量 */
    private static final String API_STATUS_PATH = "/api/v5/status";
    private static final String API_STATS_PATH = "/api/v5/stats";
    
    /** HTTP 头常量 */
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String AUTH_PREFIX = "Basic ";
    
    /** JSON 字段名常量 */
    private static final String FIELD_VERSION = "version";
    private static final String FIELD_NODE = "node";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_DATA = "data";
    private static final String FIELD_TOPICS = "topics";
    private static final String FIELD_CONNECTIONS = "connections";
    private static final String FIELD_COUNT = "count";
    
    /** 默认值常量 */
    private static final String DEFAULT_UNKNOWN = "Unknown";
    private static final int DEFAULT_COUNT = 0;

    @Resource
    private RestTemplate restTemplate;

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
            // 解密密码
            String password = new String(Base64.getDecoder().decode(system.getPassword()));

            // 创建认证头
            HttpHeaders headers = new HttpHeaders();
            String auth = system.getUsername() + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set(HEADER_AUTHORIZATION, AUTH_PREFIX + encodedAuth);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 调用EMQX API测试连接
            String apiUrl = system.getUrl() + API_STATUS_PATH;
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.GET, entity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            boolean isOnline = response.getStatusCode().is2xxSuccessful();

            // 更新系统状态
            system.setStatus(isOnline ? STATUS_ONLINE : STATUS_OFFLINE);
            system.setLastCheck(LocalDateTime.now());
            this.updateById(system);

            if (isOnline) {
                // 解析响应获取版本和节点信息
                String responseBody = response.getBody();
                String version = DEFAULT_UNKNOWN;
                String nodeInfo = DEFAULT_UNKNOWN;

                // 解析EMQX API响应获取版本和节点信息
                version = parseVersionFromResponse(responseBody);
                nodeInfo = parseNodeInfoFromResponse(responseBody);

                return ConnectionTestResult.success(id, system.getName(), responseTime, version, nodeInfo);
            } else {
                return ConnectionTestResult.failure(id, system.getName(), "HTTP状态码: " + response.getStatusCode());
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
     * 解析EMQX API响应中的版本信息
     *
     * @param response API响应内容
     * @return 版本信息
     */
    private String parseVersionFromResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            // EMQX API通常在根节点或status节点中包含版本信息
            if (rootNode.has(FIELD_VERSION)) {
                return rootNode.get(FIELD_VERSION).asText();
            }

            // 检查是否在status对象中
            if (rootNode.has(FIELD_STATUS) && rootNode.get(FIELD_STATUS).has(FIELD_VERSION)) {
                return rootNode.get(FIELD_STATUS).get(FIELD_VERSION).asText();
            }

            // 检查是否在data对象中
            if (rootNode.has(FIELD_DATA) && rootNode.get(FIELD_DATA).has(FIELD_VERSION)) {
                return rootNode.get(FIELD_DATA).get(FIELD_VERSION).asText();
            }

            // 如果是节点列表响应，尝试从第一个节点获取版本
            if (rootNode.isArray() && !rootNode.isEmpty()) {
                JsonNode firstNode = rootNode.get(0);
                if (firstNode.has(FIELD_VERSION)) {
                    return firstNode.get(FIELD_VERSION).asText();
                }
            }

            log.warn("无法从EMQX API响应中解析版本信息: {}", response);
            return DEFAULT_UNKNOWN;
        } catch (Exception e) {
            log.error("解析EMQX版本信息时发生错误", e);
            return DEFAULT_UNKNOWN;
        }
    }

    /**
     * 解析EMQX API响应中的节点信息
     *
     * @param response API响应内容
     * @return 节点信息
     */
    private String parseNodeInfoFromResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            // 检查是否有节点信息
            if (rootNode.has(FIELD_NODE)) {
                return rootNode.get(FIELD_NODE).asText();
            }

            if (rootNode.has(FIELD_DATA) && rootNode.get(FIELD_DATA).has(FIELD_NODE)) {
                return rootNode.get(FIELD_DATA).get(FIELD_NODE).asText();
            }

            return DEFAULT_UNKNOWN;
        } catch (Exception e) {
            log.error("解析EMQX节点信息时发生错误", e);
            return DEFAULT_UNKNOWN;
        }
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
                    // 解密密码
                    String password = new String(Base64.getDecoder().decode(system.getPassword()));

                    // 创建认证头
                    HttpHeaders headers = new HttpHeaders();
                    String auth = system.getUsername() + ":" + password;
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                    headers.set(HEADER_AUTHORIZATION, AUTH_PREFIX + encodedAuth);

                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    // 从每个在线系统获取主题数量
                    String apiUrl = system.getUrl() + API_STATS_PATH;
                    ResponseEntity<String> response = restTemplate.exchange(
                            apiUrl, HttpMethod.GET, entity, String.class);

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        long topicCount = parseTopicCountFromStatsResponse(response.getBody());
                        totalTopics += topicCount;
                    }
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
     * 从统计响应中解析主题数量
     *
     * @param response API响应内容
     * @return 主题数量
     */
    private long parseTopicCountFromStatsResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            // 检查不同可能的路径
            if (rootNode.has(FIELD_TOPICS) && rootNode.get(FIELD_TOPICS).has(FIELD_COUNT)) {
                return rootNode.get(FIELD_TOPICS).get(FIELD_COUNT).asLong();
            }

            if (rootNode.has(FIELD_DATA) && rootNode.get(FIELD_DATA).has(FIELD_TOPICS)) {
                JsonNode topicsNode = rootNode.get(FIELD_DATA).get(FIELD_TOPICS);
                if (topicsNode.has(FIELD_COUNT)) {
                    return topicsNode.get(FIELD_COUNT).asLong();
                }
            }

            // 如果找不到主题数量，返回0
            return DEFAULT_COUNT;
        } catch (Exception e) {
            log.error("解析主题数量时发生错误", e);
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
                    // 解密密码
                    String password = new String(Base64.getDecoder().decode(system.getPassword()));

                    // 创建认证头
                    HttpHeaders headers = new HttpHeaders();
                    String auth = system.getUsername() + ":" + password;
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                    headers.set(HEADER_AUTHORIZATION, AUTH_PREFIX + encodedAuth);

                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    // 获取统计信息
                    String apiUrl = system.getUrl() + API_STATS_PATH;
                    ResponseEntity<String> response = restTemplate.exchange(
                            apiUrl, HttpMethod.GET, entity, String.class);

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        // 解析响应获取主题数
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode = mapper.readTree(response.getBody());

                        // 解析主题数
                        long topicCount = parseCountFromNode(rootNode, FIELD_TOPICS);
                        totalTopics += topicCount;
                    }
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
     * 从JSON节点中解析计数信息
     *
     * @param rootNode  根节点
     * @param fieldName 字段名称
     * @return 计数值
     */
    private long parseCountFromNode(JsonNode rootNode, String fieldName) {
        try {
            // 检查不同可能的路径
            if (rootNode.has(fieldName) && rootNode.get(fieldName).has(FIELD_COUNT)) {
                return rootNode.get(fieldName).get(FIELD_COUNT).asLong();
            }

            if (rootNode.has(FIELD_DATA) && rootNode.get(FIELD_DATA).has(fieldName)) {
                JsonNode fieldNode = rootNode.get(FIELD_DATA).get(fieldName);
                if (fieldNode.has(FIELD_COUNT)) {
                    return fieldNode.get(FIELD_COUNT).asLong();
                }
            }

            // 如果找不到计数，返回0
            return DEFAULT_COUNT;
        } catch (Exception e) {
            log.error("解析{}计数时发生错误", fieldName, e);
            return DEFAULT_COUNT;
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
                // 解密密码
                String password = new String(Base64.getDecoder().decode(system.getPassword()));

                // 创建认证头
                HttpHeaders headers = new HttpHeaders();
                String auth = system.getUsername() + ":" + password;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                headers.set(HEADER_AUTHORIZATION, AUTH_PREFIX + encodedAuth);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                // 获取统计信息
                String apiUrl = system.getUrl() + API_STATS_PATH;
                ResponseEntity<String> response = restTemplate.exchange(
                        apiUrl, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(response.getBody());

                    // 解析主题数和连接数
                    long topicCount = parseCountFromNode(rootNode, FIELD_TOPICS);
                    long connectionCount = parseCountFromNode(rootNode, FIELD_CONNECTIONS);

                    dto.setTopicCount((int) topicCount);
                    dto.setConnectionCount((int) connectionCount);
                } else {
                    dto.setTopicCount(DEFAULT_COUNT);
                    dto.setConnectionCount(DEFAULT_COUNT);
                }
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
