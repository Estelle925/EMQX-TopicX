package com.emqx.topichub.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的请求限流服务
 */
@Slf4j
@Service
public class RateLimitService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查是否允许执行操作（基于固定时间窗口限流）
     *
     * @param key 限流键
     * @param windowSeconds 时间窗口（秒）
     * @param maxRequests 最大请求次数
     * @return true表示允许执行，false表示被限流
     */
    public boolean isAllowed(String key, int windowSeconds, int maxRequests) {
        try {
            String redisKey = "rate_limit:" + key;
            
            // 获取当前计数
            Integer currentCount = (Integer) redisTemplate.opsForValue().get(redisKey);
            
            if (currentCount == null) {
                // 第一次请求，设置计数为1并设置过期时间
                redisTemplate.opsForValue().set(redisKey, 1, Duration.ofSeconds(windowSeconds));
                log.debug("首次请求，key: {}, 设置计数为1", redisKey);
                return true;
            } else if (currentCount < maxRequests) {
                // 未达到限制，增加计数
                redisTemplate.opsForValue().increment(redisKey);
                log.debug("请求通过，key: {}, 当前计数: {}", redisKey, currentCount + 1);
                return true;
            } else {
                // 达到限制，拒绝请求
                Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
                log.warn("请求被限流，key: {}, 当前计数: {}, 剩余时间: {}秒", redisKey, currentCount, ttl);
                return false;
            }
        } catch (Exception e) {
            log.error("限流检查异常，key: {}", key, e);
            // 异常情况下允许通过，避免影响正常业务
            return true;
        }
    }

    /**
     * 检查同步操作是否允许执行
     *
     * @param systemId EMQX系统ID
     * @return true表示允许执行，false表示被限流
     */
    public boolean isTopicSyncAllowed(Long systemId) {
        String key = "topic_sync:" + systemId;
        // 每个系统每5分钟最多允许同步1次
        return isAllowed(key, 300, 1);
    }

    /**
     * 获取限流剩余时间
     *
     * @param systemId EMQX系统ID
     * @return 剩余时间（秒），-1表示无限制
     */
    public Long getTopicSyncRemainingTime(Long systemId) {
        try {
            String redisKey = "rate_limit:topic_sync:" + systemId;
            return redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("获取限流剩余时间异常，systemId: {}", systemId, e);
            return -1L;
        }
    }

    /**
     * 清除指定系统的同步限流
     *
     * @param systemId EMQX系统ID
     */
    public void clearTopicSyncLimit(Long systemId) {
        try {
            String redisKey = "rate_limit:topic_sync:" + systemId;
            redisTemplate.delete(redisKey);
            log.info("清除同步限流，systemId: {}", systemId);
        } catch (Exception e) {
            log.error("清除同步限流异常，systemId: {}", systemId, e);
        }
    }
}