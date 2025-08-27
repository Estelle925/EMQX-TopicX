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
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    // ================================ API路径常量 ================================
    
    /**
     * EMQX登录API路径
     * 用于获取访问令牌的认证接口
     */
    private static final String API_LOGIN_PATH = "/api/v5/login";
    
    /**
     * EMQX系统状态API路径
     * 用于检查系统运行状态和版本信息
     */
    private static final String API_STATUS_PATH = "/api/v5/status";
    
    /**
     * EMQX系统统计API路径
     * 用于获取系统运行统计数据
     */
    private static final String API_STATS_PATH = "/api/v5/stats";
    
    /**
     * EMQX主题列表API路径
     * 用于获取当前系统中的所有主题信息
     */
    private static final String API_TOPICS_PATH = "/api/v5/topics";
    
    // ================================ 缓存相关常量 ================================
    
    /**
     * Redis中EMQX令牌缓存的键前缀
     * 格式: emqx:token:{systemId}
     */
    private static final String CACHE_KEY_PREFIX = "emqx:token:";
    
    /**
     * 令牌在Redis中的缓存超时时间（分钟）
     * 设置为50分钟，略小于EMQX默认的60分钟令牌有效期，确保缓存不会过期
     */
    private static final long TOKEN_CACHE_TIMEOUT_MINUTES = 50L;
    
    // ================================ 分页相关常量 ================================
    
    /**
     * 默认分页大小
     * 用于主题列表等分页查询的默认每页记录数
     */
    private static final int DEFAULT_PAGE_SIZE = 1000;
    
    /**
     * 最大分页限制
     * 防止无限循环，限制最多获取的页数
     */
    private static final int MAX_PAGE_LIMIT = 100;
    
    /**
     * 初始页码
     * 分页查询的起始页码
     */
    private static final int INITIAL_PAGE = 1;
    
    // ================================ HTTP头常量 ================================
    
    /**
     * HTTP Authorization头名称
     * 用于传递认证信息
     */
    private static final String HEADER_AUTHORIZATION = "Authorization";
    
    /**
     * HTTP Content-Type头名称
     * 用于指定请求体的媒体类型
     */
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    
    /**
     * HTTP Referer头名称
     * 用于指定请求来源页面
     */
    private static final String HEADER_REFERER = "Referer";
    
    /**
     * HTTP User-Agent头名称
     * 用于标识客户端应用程序
     */
    private static final String HEADER_USER_AGENT = "User-Agent";
    
    /**
     * Bearer令牌前缀
     * 用于构造Authorization头的Bearer认证格式
     */
    private static final String BEARER_PREFIX = "Bearer ";
    
    /**
     * JSON内容类型
     * 用于指定请求体为JSON格式
     */
    private static final String CONTENT_TYPE_JSON = "application/json";
    
    /**
     * 浏览器用户代理字符串
     * 模拟Chrome浏览器的User-Agent，用于API请求
     */
    private static final String USER_AGENT_VALUE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36";
    
    // ================================ JSON字段常量 ================================
    
    /**
     * JSON响应中的令牌字段名
     * 用于从登录响应中提取访问令牌
     */
    private static final String JSON_FIELD_TOKEN = "token";
    
    /**
     * JSON响应中的状态码字段名
     * 用于判断API调用是否成功
     */
    private static final String JSON_FIELD_CODE = "code";
    
    /**
     * JSON响应中的数据字段名
     * 用于提取响应中的主要数据内容
     */
    private static final String JSON_FIELD_DATA = "data";
    
    /**
     * JSON响应中的版本字段名
     * 用于获取EMQX系统版本信息
     */
    private static final String JSON_FIELD_VERSION = "version";
    
    /**
     * JSON响应中的状态字段名
     * 用于获取系统运行状态信息
     */
    private static final String JSON_FIELD_STATUS = "status";
    
    /**
     * JSON响应中的主题字段名
     * 用于从主题列表响应中提取主题路径
     */
    private static final String JSON_FIELD_TOPIC = "topic";
    
    /**
     * API调用成功的状态码
     * EMQX API返回0表示操作成功
     */
    private static final int SUCCESS_CODE = 0;

    @Resource
    private RestTemplate restTemplate;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;



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
     * 支持Redis缓存，避免重复登录
     *
     * @param emqxSystem EMQX系统信息
     * @return Bearer token
     */
    private String loginToEmqx(EmqxSystem emqxSystem) {
        String cacheKey = CACHE_KEY_PREFIX + emqxSystem.getId();
        
        try {
            // 1. 先从Redis缓存获取token
            String cachedToken = stringRedisTemplate.opsForValue().get(cacheKey);
            
            // 2. 如果缓存中有token，验证其有效性
            if (cachedToken != null && !cachedToken.isEmpty()) {
                if (validateToken(emqxSystem, cachedToken)) {
                    log.debug("使用缓存的token，系统: {}", emqxSystem.getName());
                    return cachedToken;
                } else {
                    log.info("缓存的token已失效，重新登录，系统: {}", emqxSystem.getName());
                    // 删除失效的token
                    stringRedisTemplate.delete(cacheKey);
                }
            }
            
            // 3. 重新登录获取新token
            String newToken = performLogin(emqxSystem);
            
            // 4. 将新token缓存到Redis，设置过期时间为50分钟（EMQX默认token有效期1小时）
            stringRedisTemplate.opsForValue().set(cacheKey, newToken, TOKEN_CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            
            log.info("获取新token并缓存，系统: {}", emqxSystem.getName());
            return newToken;
            
        } catch (Exception e) {
            log.error("登录EMQX系统失败，系统: {}", emqxSystem.getName(), e);
            throw new RuntimeException("登录EMQX系统失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建带Bearer token的HTTP请求头
     *
     * @param token Bearer token
     * @return HTTP请求头
     */
    private HttpHeaders createBearerAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_AUTHORIZATION, BEARER_PREFIX + token);
        return headers;
    }
    
    /**
     * 创建标准的EMQX API请求头（包含Bearer token和其他标准头）
     *
     * @param emqxSystem EMQX系统信息
     * @param token Bearer token
     * @return HTTP请求头
     */
    private HttpHeaders createStandardApiHeaders(EmqxSystem emqxSystem, String token) {
        HttpHeaders headers = createBearerAuthHeaders(token);
        headers.set(HEADER_REFERER, emqxSystem.getUrl() + "/");
        headers.set(HEADER_USER_AGENT, USER_AGENT_VALUE);
        return headers;
    }
    
    /**
     * 创建登录请求头
     *
     * @param emqxSystem EMQX系统信息
     * @return HTTP请求头
     */
    private HttpHeaders createLoginHeaders(EmqxSystem emqxSystem) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
        headers.set(HEADER_REFERER, emqxSystem.getUrl() + "/");
        headers.set(HEADER_USER_AGENT, USER_AGENT_VALUE);
        return headers;
    }
    
    /**
     * 执行GET请求
     *
     * @param url 请求URL
     * @param headers 请求头
     * @return 响应结果
     */
    private ResponseEntity<String> executeGetRequest(String url, HttpHeaders headers) {
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }
    
    /**
     * 执行POST请求
     *
     * @param url 请求URL
     * @param headers 请求头
     * @param body 请求体
     * @return 响应结果
     */
    private ResponseEntity<String> executePostRequest(String url, HttpHeaders headers, String body) {
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    /**
     * 验证token是否有效
     * 通过调用EMQX状态接口来验证token
     *
     * @param emqxSystem EMQX系统信息
     * @param token 待验证的token
     * @return token是否有效
     */
    private boolean validateToken(EmqxSystem emqxSystem, String token) {
        try {
            HttpHeaders headers = createBearerAuthHeaders(token);
            String apiUrl = emqxSystem.getUrl() + API_STATUS_PATH;
            ResponseEntity<String> response = executeGetRequest(apiUrl, headers);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("Token验证失败，系统: {}, 错误: {}", emqxSystem.getName(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 执行实际的登录操作
     *
     * @param emqxSystem EMQX系统信息
     * @return Bearer token
     */
    private String performLogin(EmqxSystem emqxSystem) {
        // 解密密码
        String password = new String(Base64.getDecoder().decode(emqxSystem.getPassword()));

        // 创建登录请求头
        HttpHeaders headers = createLoginHeaders(emqxSystem);

        // 创建登录请求体
        String loginBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", 
                emqxSystem.getUsername(), password);
        
        // 调用登录API
        String loginUrl = emqxSystem.getUrl() + API_LOGIN_PATH;
        ResponseEntity<String> loginResponse = executePostRequest(loginUrl, headers, loginBody);

        if (loginResponse.getStatusCode().is2xxSuccessful() && loginResponse.getBody() != null) {
            // 解析响应获取token
            return parseTokenFromLoginResponse(loginResponse.getBody());
        } else {
            throw new RuntimeException("登录EMQX系统失败，状态码: " + loginResponse.getStatusCode());
        }
    }
    
    /**
     * 清除指定系统的token缓存
     * 当系统配置更新时调用此方法
     *
     * @param systemId 系统ID
     */
    public void clearTokenCache(Long systemId) {
        String cacheKey = CACHE_KEY_PREFIX + systemId;
        stringRedisTemplate.delete(cacheKey);
        log.info("已清除系统 {} 的token缓存", systemId);
    }
    
    /**
     * 清除所有系统的token缓存
     */
    public void clearAllTokenCache() {
        Set<String> keys = stringRedisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (!keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
            log.info("已清除所有系统的token缓存，共 {} 个", keys.size());
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
        int page = INITIAL_PAGE;
        int limit = DEFAULT_PAGE_SIZE;
        boolean hasMoreData = true;
        
        try {
            // 创建请求头
            HttpHeaders headers = createStandardApiHeaders(emqxSystem, bearerToken);

            // 分页获取所有Topic数据
            while (hasMoreData) {
                String apiUrl = emqxSystem.getUrl() + API_TOPICS_PATH + "?limit=" + limit + "&page=" + page;
                log.debug("正在获取第{}页Topic数据，URL: {}", page, apiUrl);
                
                ResponseEntity<String> response = executeGetRequest(apiUrl, headers);

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
                if (page > MAX_PAGE_LIMIT) {
                    log.warn("已达到最大页数限制({}页)，停止获取", MAX_PAGE_LIMIT);
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
            String token = jsonObject.getString(JSON_FIELD_TOKEN);
            if (token != null && !token.trim().isEmpty()) {
                return token;
            }
            
            // 如果有code字段，检查是否成功
            Integer code = jsonObject.getInteger(JSON_FIELD_CODE);
            if (code != null) {
                if (code == SUCCESS_CODE && jsonObject.containsKey(JSON_FIELD_TOKEN)) {
                    return jsonObject.getString(JSON_FIELD_TOKEN);
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
            Integer code = jsonObject.getInteger(JSON_FIELD_CODE);
            if (code != null && code == SUCCESS_CODE) {
                // 获取data数组
                JSONArray dataArray = jsonObject.getJSONArray(JSON_FIELD_DATA);
                if (dataArray != null) {
                    for (int i = 0; i < dataArray.size(); i++) {
                        JSONObject topicNode = dataArray.getJSONObject(i);
                        String topicPath = topicNode.getString(JSON_FIELD_TOPIC);
                        if (topicPath != null && !topicPath.trim().isEmpty()) {
                            topicPaths.add(topicPath.trim());
                        }
                    }
                }
            } else {
                JSONArray dataJson = jsonObject.getJSONArray(JSON_FIELD_DATA);
                if (dataJson != null) {
                    for (int i = 0; i < dataJson.size(); i++) {
                        JSONObject topicNode = dataJson.getJSONObject(i);
                        String topicPath = topicNode.getString(JSON_FIELD_TOPIC);
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
            HttpHeaders headers = createBearerAuthHeaders(bearerToken);

            // 测试连接
            String apiUrl = emqxSystem.getUrl() + API_STATUS_PATH;
            ResponseEntity<String> response = executeGetRequest(apiUrl, headers);

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
            HttpHeaders headers = createBearerAuthHeaders(bearerToken);

            // 获取状态信息
            String apiUrl = emqxSystem.getUrl() + "/api/v5/status";
            ResponseEntity<String> response = executeGetRequest(apiUrl, headers);

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
            HttpHeaders headers = createBearerAuthHeaders(bearerToken);

            // 获取统计信息
            String apiUrl = emqxSystem.getUrl() + API_STATS_PATH;
            ResponseEntity<String> response = executeGetRequest(apiUrl, headers);

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
     * 检查系统健康状态
     * 使用登录获取Bearer token的方式进行认证
     *
     * @param system EMQX系统
     * @return 是否在线
     */
    public boolean checkSystemHealth(EmqxSystem system) {
        try {
            if (system.getUrl() == null || system.getUrl().isEmpty()) {
                return false;
            }
            
            // 先登录获取Bearer token
            String bearerToken = loginToEmqx(system);
            
            // 创建认证头
            HttpHeaders headers = createBearerAuthHeaders(bearerToken);
            
            // 调用EMQX API状态接口
            String apiUrl = system.getUrl() + API_STATUS_PATH;
            ResponseEntity<String> response = executeGetRequest(apiUrl, headers);
            
            // 检查响应状态码
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            log.warn("检查系统 {} 健康状态失败: {}", system.getName(), e.getMessage());
            return false;
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
            String version = jsonObject.getString(JSON_FIELD_VERSION);
            if (version != null) {
                return version;
            }

            // 检查是否在status对象中
            JSONObject statusObj = jsonObject.getJSONObject(JSON_FIELD_STATUS);
            if (statusObj != null) {
                version = statusObj.getString(JSON_FIELD_VERSION);
                if (version != null) {
                    return version;
                }
            }

            // 检查是否在data对象中
            JSONObject dataObj = jsonObject.getJSONObject(JSON_FIELD_DATA);
            if (dataObj != null) {
                version = dataObj.getString(JSON_FIELD_VERSION);
                if (version != null) {
                    return version;
                }
            }

            // 如果是节点列表响应，尝试从第一个节点获取版本
            JSONArray jsonArray = JSON.parseArray(response);
            if (jsonArray != null && !jsonArray.isEmpty()) {
                JSONObject firstNode = jsonArray.getJSONObject(0);
                version = firstNode.getString(JSON_FIELD_VERSION);
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

            JSONObject dataObj = jsonObject.getJSONObject(JSON_FIELD_DATA);
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
                String node = "Node ";
                if (response != null && response.contains(node)) {
                    // 提取"Node xxx"格式的节点信息
                    String[] lines = response.split("\\n");
                    for (String line : lines) {
                        line = line.trim();
                        if (line.startsWith(node)) {
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