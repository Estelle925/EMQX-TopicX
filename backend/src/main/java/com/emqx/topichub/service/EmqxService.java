package com.emqx.topichub.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.emqx.topichub.entity.EmqxSystem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * EMQX系统API交互服务
 * 负责与EMQX系统进行API通信，包括登录认证和数据获取
 *
 * @author EMQX Topic Hub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmqxService {

    @Resource
    private RestTemplate restTemplate;



    /**
     * 从EMQX系统获取Topic列表
     *
     * @param emqxSystem EMQX系统信息
     * @return Topic路径集合
     */
    public Set<String> fetchTopicsFromEmqx(EmqxSystem emqxSystem) {
        Set<String> topicPaths = new HashSet<>();
        
        try {
            // 1. 先登录获取Bearer token
            String bearerToken = loginToEmqx(emqxSystem);
            
            // 2. 使用token获取Topic列表
            topicPaths = fetchTopicsWithToken(emqxSystem, bearerToken);
            
            log.info("从EMQX系统 {} 获取到 {} 个Topic", emqxSystem.getName(), topicPaths.size());

        } catch (Exception e) {
            log.error("调用EMQX API失败，系统: {}", emqxSystem.getName(), e);
            throw new RuntimeException("调用EMQX API失败: " + e.getMessage());
        }

        return topicPaths;
    }

    /**
     * 登录EMQX系统获取Bearer token
     *
     * @param emqxSystem EMQX系统信息
     * @return Bearer token
     */
    private String loginToEmqx(EmqxSystem emqxSystem) {
        try {
            // 解密密码
            String password = new String(Base64.getDecoder().decode(emqxSystem.getPassword()));

            // 创建登录请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Referer", emqxSystem.getUrl() + "/");
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");

            // 创建登录请求体
            String loginBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", 
                    emqxSystem.getUsername(), password);
            
            HttpEntity<String> loginEntity = new HttpEntity<>(loginBody, headers);

            // 调用登录API
            String loginUrl = emqxSystem.getUrl() + "/api/v5/login";
            ResponseEntity<String> loginResponse = restTemplate.exchange(
                    loginUrl, HttpMethod.POST, loginEntity, String.class);

            if (loginResponse.getStatusCode().is2xxSuccessful() && loginResponse.getBody() != null) {
                // 解析响应获取token
                return parseTokenFromLoginResponse(loginResponse.getBody());
            } else {
                throw new RuntimeException("登录EMQX系统失败，状态码: " + loginResponse.getStatusCode());
            }

        } catch (Exception e) {
            log.error("登录EMQX系统失败，系统: {}", emqxSystem.getName(), e);
            throw new RuntimeException("登录EMQX系统失败: " + e.getMessage());
        }
    }

    /**
     * 使用Bearer token获取Topic列表
     *
     * @param emqxSystem EMQX系统信息
     * @param bearerToken Bearer token
     * @return Topic路径集合
     */
    private Set<String> fetchTopicsWithToken(EmqxSystem emqxSystem, String bearerToken) {
        Set<String> allTopics = new HashSet<>();
        int page = 1;
        int limit = 1000;
        boolean hasMoreData = true;
        
        try {
            // 创建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + bearerToken);
            headers.set("Referer", emqxSystem.getUrl() + "/");
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 分页获取所有Topic数据
            while (hasMoreData) {
                String apiUrl = emqxSystem.getUrl() + "/api/v5/topics?limit=" + limit + "&page=" + page;
                log.debug("正在获取第{}页Topic数据，URL: {}", page, apiUrl);
                
                ResponseEntity<String> response = restTemplate.exchange(
                        apiUrl, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    // 解析当前页的Topic列表
                    Set<String> currentPageTopics = parseTopicsFromResponse(response.getBody());
                    
                    if (currentPageTopics.isEmpty()) {
                        // 当前页没有数据，停止分页
                        hasMoreData = false;
                        log.debug("第{}页没有Topic数据，停止分页获取", page);
                    } else {
                        // 添加到总集合中
                        allTopics.addAll(currentPageTopics);
                        log.debug("第{}页获取到{}个Topic", page, currentPageTopics.size());
                        
                        // 如果当前页数据少于limit，说明已经是最后一页
                        if (currentPageTopics.size() < limit) {
                            hasMoreData = false;
                            log.debug("第{}页数据量({})小于限制({}), 已是最后一页", page, currentPageTopics.size(), limit);
                        } else {
                            // 继续下一页
                            page++;
                        }
                    }
                } else {
                    log.warn("获取EMQX系统 {} 第{}页Topic列表失败，状态码: {}", emqxSystem.getName(), page, response.getStatusCode());
                    hasMoreData = false;
                }
                
                // 防止无限循环，最多获取100页
                if (page > 100) {
                    log.warn("已达到最大页数限制(100页)，停止获取");
                    hasMoreData = false;
                }
            }
            
            log.info("共获取{}页数据，总计{}个Topic", page - 1, allTopics.size());
            return allTopics;

        } catch (Exception e) {
            log.error("获取Topic列表失败，系统: {}, 当前页: {}", emqxSystem.getName(), page, e);
            throw new RuntimeException("获取Topic列表失败: " + e.getMessage());
        }
    }

    /**
     * 从登录响应中解析Bearer token
     *
     * @param responseBody 登录响应体
     * @return Bearer token
     */
    private String parseTokenFromLoginResponse(String responseBody) {
        try {
            JSONObject jsonObject = JSON.parseObject(responseBody);
            
            // 直接检查是否有token字段
            String token = jsonObject.getString("token");
            if (token != null && !token.trim().isEmpty()) {
                return token;
            }
            
            // 如果有code字段，检查是否成功
            Integer code = jsonObject.getInteger("code");
            if (code != null) {
                if (code == 0 && jsonObject.containsKey("token")) {
                    return jsonObject.getString("token");
                }
                throw new RuntimeException("登录失败，错误码: " + code);
            }
            
            throw new RuntimeException("登录响应中未找到有效的token");
            
        } catch (Exception e) {
            log.error("解析登录响应失败: {}", responseBody, e);
            throw new RuntimeException("解析登录响应失败: " + e.getMessage());
        }
    }

    /**
     * 从API响应中解析Topic列表
     *
     * @param responseBody API响应体
     * @return Topic路径集合
     */
    private Set<String> parseTopicsFromResponse(String responseBody) {
        Set<String> topicPaths = new HashSet<>();
        
        try {
            JSONObject jsonObject = JSON.parseObject(responseBody);
            
            // 检查响应是否成功
            Integer code = jsonObject.getInteger("code");
            if (code != null && code == 0) {
                // 获取data数组
                JSONArray dataArray = jsonObject.getJSONArray("data");
                if (dataArray != null) {
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONObject topicNode = dataArray.getJSONObject(i);
                        String topicPath = topicNode.getString("topic");
                        if (topicPath != null && !topicPath.trim().isEmpty()) {
                            topicPaths.add(topicPath.trim());
                        }
                    }
                }
            } else {
                JSONArray dataJson = jsonObject.getJSONArray("data");
                if (dataJson != null) {
                    for (int i = 0; i < dataJson.size(); i++) {
                        JSONObject topicNode = dataJson.getJSONObject(i);
                        String topicPath = topicNode.getString("topic");
                        if (topicPath != null && !topicPath.trim().isEmpty()) {
                            topicPaths.add(topicPath.trim());
                        }
                    }
                }
                log.warn("EMQX API返回错误响应: {}", responseBody);
            }
            
        } catch (Exception e) {
            log.error("解析Topic响应失败: {}", responseBody, e);
            throw new RuntimeException("解析Topic响应失败: ");
        }
        
        return topicPaths;
    }

    /**
     * 测试EMQX系统连接状态
     *
     * @param emqxSystem EMQX系统信息
     * @return 是否连接成功
     */
    public boolean testConnection(EmqxSystem emqxSystem) {
        try {
            // 先登录获取Bearer token
            String bearerToken = loginToEmqx(emqxSystem);
            
            // 创建认证头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + bearerToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 测试连接
            String apiUrl = emqxSystem.getUrl() + "/api/v5/status";
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.GET, entity, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("测试EMQX系统连接失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取EMQX系统状态信息
     *
     * @param emqxSystem EMQX系统信息
     * @return 状态响应内容
     */
    public String getSystemStatus(EmqxSystem emqxSystem) {
        try {
            // 先登录获取Bearer token
            String bearerToken = loginToEmqx(emqxSystem);
            
            // 创建认证头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + bearerToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 获取状态信息
            String apiUrl = emqxSystem.getUrl() + "/api/v5/status";
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new RuntimeException("HTTP状态码: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("获取EMQX系统状态失败: {}", e.getMessage());
            throw new RuntimeException("获取系统状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取EMQX系统统计信息
     *
     * @param emqxSystem EMQX系统信息
     * @return 统计响应内容
     */
    public String getSystemStats(EmqxSystem emqxSystem) {
        try {
            // 先登录获取Bearer token
            String bearerToken = loginToEmqx(emqxSystem);
            
            // 创建认证头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + bearerToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 获取统计信息
            String apiUrl = emqxSystem.getUrl() + "/api/v5/stats";
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new RuntimeException("HTTP状态码: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("获取EMQX系统统计信息失败: {}", e.getMessage());
            throw new RuntimeException("获取系统统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 解析EMQX API响应中的版本信息
     *
     * @param response API响应内容
     * @return 版本信息
     */
    public String parseVersionFromResponse(String response) {
        try {
            JSONObject jsonObject = JSON.parseObject(response);

            // EMQX API通常在根节点或status节点中包含版本信息
            String version = jsonObject.getString("version");
            if (version != null) {
                return version;
            }

            // 检查是否在status对象中
            JSONObject statusObj = jsonObject.getJSONObject("status");
            if (statusObj != null) {
                version = statusObj.getString("version");
                if (version != null) {
                    return version;
                }
            }

            // 检查是否在data对象中
            JSONObject dataObj = jsonObject.getJSONObject("data");
            if (dataObj != null) {
                version = dataObj.getString("version");
                if (version != null) {
                    return version;
                }
            }

            // 如果是节点列表响应，尝试从第一个节点获取版本
            JSONArray jsonArray = JSON.parseArray(response);
            if (jsonArray != null && !jsonArray.isEmpty()) {
                JSONObject firstNode = jsonArray.getJSONObject(0);
                version = firstNode.getString("version");
                if (version != null) {
                    return version;
                }
            }

            log.warn("无法从EMQX API响应中解析版本信息: {}", response);
            return "Unknown";
        } catch (Exception e) {
            log.error("解析EMQX版本信息时发生错误", e);
            return "Unknown";
        }
    }

    /**
     * 解析EMQX API响应中的节点信息
     *
     * @param response API响应内容
     * @return 节点信息
     */
    public String parseNodeInfoFromResponse(String response) {
        try {
            // 首先尝试解析为JSON
            JSONObject jsonObject = JSON.parseObject(response);

            // 检查是否有节点信息
            String node = jsonObject.getString("node");
            if (node != null) {
                return node;
            }

            JSONObject dataObj = jsonObject.getJSONObject("data");
            if (dataObj != null) {
                node = dataObj.getString("node");
                if (node != null) {
                    return node;
                }
            }

            return "Unknown";
        } catch (Exception e) {
            // 如果JSON解析失败，尝试从纯文本中提取节点信息
            try {
                if (response != null && response.contains("Node ")) {
                    // 提取"Node xxx"格式的节点信息
                    String[] lines = response.split("\\n");
                    for (String line : lines) {
                        line = line.trim();
                        if (line.startsWith("Node ")) {
                            // 提取节点名称，格式通常是"Node emqx@172.17.0.14 is started"
                            String[] parts = line.split(" ");
                            if (parts.length >= 2) {
                                // 返回节点名称部分
                                return parts[1];
                            }
                        }
                    }
                }
                
                // 如果无法提取节点信息，返回响应的第一行作为节点信息
                if (response != null && !response.trim().isEmpty()) {
                    String firstLine = response.split("\\n")[0].trim();
                    if (!firstLine.isEmpty()) {
                        return firstLine;
                    }
                }
            } catch (Exception textParseException) {
                log.warn("解析纯文本节点信息时发生错误: {}", textParseException.getMessage());
            }
            
            log.debug("无法解析节点信息，响应内容: {}", response);
            return "Unknown";
        }
    }

    /**
     * 从JSON节点中解析计数信息
     * @return 计数值
     */
    public long parseCountFromNode(String response, String fieldName) {
        try {
            JSONArray jsonArray = JSON.parseArray(response);
            if (!CollectionUtils.isEmpty(jsonArray)) {
                JSONObject dataJson = jsonArray.getJSONObject(0);
                Long count = dataJson.getLong(fieldName);
                return count != null ? count : 0;
            }
        } catch (Exception e) {
            log.error("解析JSON节点时发生错误: {}", e.getMessage());
        }
        return 0;
    }
}