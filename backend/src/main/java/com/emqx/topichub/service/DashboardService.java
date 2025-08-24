package com.emqx.topichub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.emqx.topichub.dto.DashboardStatsDTO;
import com.emqx.topichub.dto.DashboardStatusDTO;
import com.emqx.topichub.entity.EmqxSystem;
import com.emqx.topichub.entity.Group;
import com.emqx.topichub.entity.Tag;
import com.emqx.topichub.entity.Topic;
import com.emqx.topichub.mapper.EmqxSystemMapper;
import com.emqx.topichub.mapper.GroupMapper;
import com.emqx.topichub.mapper.TagMapper;
import com.emqx.topichub.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 仪表板服务实现类
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmqxSystemMapper emqxSystemMapper;
    private final TopicMapper topicMapper;
    private final GroupMapper groupMapper;
    private final TagMapper tagMapper;
    private final RestTemplate restTemplate;

    /**
     * 获取仪表板统计数据
     *
     * @return 统计数据
     */
    public DashboardStatsDTO getStats() {
        // 统计EMQX系统数量
        Integer systemCount = Math.toIntExact(emqxSystemMapper.selectCount(new QueryWrapper<EmqxSystem>()));

        // 统计Topic数量
        Integer topicCount = Math.toIntExact(topicMapper.selectCount(new QueryWrapper<Topic>()));

        // 统计业务数量
        Integer groupCount = Math.toIntExact(groupMapper.selectCount(new QueryWrapper<Group>()));

        // 统计标签数量
        Integer tagCount = Math.toIntExact(tagMapper.selectCount(new QueryWrapper<Tag>()));

        return new DashboardStatsDTO(systemCount, topicCount, groupCount, tagCount);
    }

    /**
     * 获取系统状态列表
     *
     * @return 系统状态列表
     */
    public List<DashboardStatusDTO> getSystemStatus() {
        List<EmqxSystem> systems = emqxSystemMapper.selectList(new QueryWrapper<>());

        return systems.stream().map(system -> {
            DashboardStatusDTO statusDTO = new DashboardStatusDTO();
            statusDTO.setId(system.getId());
            statusDTO.setName(system.getName());
            statusDTO.setUrl(system.getUrl());
            statusDTO.setStatus(system.getStatus());
            statusDTO.setLastCheck(system.getLastCheck());
            statusDTO.setTopicCount(Math.toIntExact(topicMapper.selectCount(new QueryWrapper<Topic>().lambda().eq(Topic::getSystemId, system.getId()))));
            return statusDTO;
        }).collect(Collectors.toList());
    }


    /**
     * 刷新系统状态
     * 主动检查所有EMQX系统的连接状态
     *
     * @return 更新后的系统状态列表
     */
    public List<DashboardStatusDTO> refreshSystemStatus() {
        List<EmqxSystem> systems = emqxSystemMapper.selectList(new QueryWrapper<EmqxSystem>());

        // 检查每个系统的状态
        for (EmqxSystem system : systems) {
            try {
                // 实际调用EMQX API检查系统状态
                boolean isOnline = checkSystemHealth(system);

                system.setStatus(isOnline ? "online" : "offline");
                system.setLastCheck(LocalDateTime.now());

                emqxSystemMapper.updateById(system);

                log.info("系统 {} 状态检查完成，状态: {}", system.getName(), system.getStatus());
            } catch (Exception e) {
                log.error("检查系统 {} 状态时发生错误: {}", system.getName(), e.getMessage());
                system.setStatus("offline");
                system.setLastCheck(LocalDateTime.now());
                emqxSystemMapper.updateById(system);
            }
        }

        return getSystemStatus();
    }

    /**
     * 检查系统健康状态
     * 实现实际的EMQX API调用逻辑
     *
     * @param system EMQX系统
     * @return 是否在线
     */
    private boolean checkSystemHealth(EmqxSystem system) {
        try {
            if (system.getUrl() == null || system.getUrl().isEmpty()) {
                return false;
            }
            
            // 解密密码
            String password = new String(Base64.getDecoder().decode(system.getPassword()));
            
            // 创建认证头
            HttpHeaders headers = new HttpHeaders();
            String auth = system.getUsername() + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 调用EMQX API状态接口
            String apiUrl = system.getUrl() + "/api/v5/status";
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.GET, entity, String.class);
            
            // 检查响应状态码
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            log.warn("检查系统 {} 健康状态失败: {}", system.getName(), e.getMessage());
            return false;
        }
    }
}