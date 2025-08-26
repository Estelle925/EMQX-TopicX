package com.emqx.topichub.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.emqx.topichub.common.Result;
import com.emqx.topichub.dto.*;
import com.emqx.topichub.dto.TopicSyncResult;
import com.emqx.topichub.service.TopicService;
import com.emqx.topichub.service.RateLimitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Topic管理控制器
 * 提供Topic管理相关的API接口
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;
    private final RateLimitService rateLimitService;

    /**
     * 分页搜索Topic列表
     *
     * @param request 搜索请求参数
     * @return 分页Topic列表
     */
    @PostMapping("/search")
    public Result<IPage<TopicDTO>> searchTopics(@Valid @RequestBody TopicSearchRequest request) {
        IPage<TopicDTO> result = topicService.searchTopics(request);
        return Result.success(result);
    }

    /**
     * 根据ID获取Topic详情
     *
     * @param id Topic ID
     * @return Topic详情
     */
    @GetMapping("/{id}")
    public Result<TopicDTO> getTopicById(@PathVariable("id") Long id) {
        try {
            TopicDTO result = topicService.getTopicDtoById(id);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建新Topic
     *
     * @param request 创建请求
     * @return 创建的Topic
     */
    @PostMapping
    public Result<TopicDTO> createTopic(@Valid @RequestBody TopicCreateRequest request) {
        try {
            TopicDTO result = topicService.createTopic(request);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新Topic
     *
     * @param id      Topic ID
     * @param request 更新请求
     * @return 更新后的Topic
     */
    @PutMapping("/{id}")
    public Result<TopicDTO> updateTopic(@PathVariable("id") Long id,
                                        @Valid @RequestBody TopicUpdateRequest request) {
        try {
            TopicDTO result = topicService.updateTopic(id, request);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除Topic
     *
     * @param id Topic ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTopic(@PathVariable("id") Long id) {
        try {
            topicService.deleteTopic(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量操作Topic
     *
     * @param request 批量操作请求
     * @return 操作结果
     */
    @PostMapping("/batch")
    public Result<Void> batchOperation(@Valid @RequestBody TopicBatchRequest request) {
        try {
            topicService.batchOperation(request);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取Topic的标签列表
     *
     * @param id Topic ID
     * @return 标签列表
     */
    @GetMapping("/{id}/tags")
    public Result<List<TagDTO>> getTopicTags(@PathVariable("id") Long id) {
        try {
            List<TagDTO> result = topicService.getTopicTags(id);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 为Topic添加标签
     *
     * @param id     Topic ID
     * @param tagIds 标签ID列表
     * @return 操作结果
     */
    @PostMapping("/{id}/tags")
    public Result<Void> addTopicTags(@PathVariable("id") Long id, @RequestBody List<Long> tagIds) {
        try {
            topicService.addTopicTags(id, tagIds);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 移除Topic的标签
     *
     * @param id     Topic ID
     * @param tagIds 标签ID列表
     * @return 操作结果
     */
    @DeleteMapping("/{id}/tags")
    public Result<Void> removeTopicTags(@PathVariable("id") Long id, @RequestBody List<Long> tagIds) {
        try {
            topicService.removeTopicTags(id, tagIds);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取Topic的公开文档（无需登录）
     *
     * @param id Topic ID
     * @return Topic公开文档信息
     */
    @GetMapping("/{id}/public-doc")
    public Result<TopicDTO> getTopicPublicDoc(@PathVariable("id") Long id) {
        try {
            TopicDTO result = topicService.getTopicDtoById(id);
            // 只返回必要的公开信息
            TopicDTO publicDoc = new TopicDTO();
            publicDoc.setId(result.getId());
            publicDoc.setName(result.getName());
            publicDoc.setPath(result.getPath());
            publicDoc.setPayloadDoc(result.getPayloadDoc());
            publicDoc.setUpdatedAt(result.getUpdatedAt());
            publicDoc.setCreatedAt(result.getCreatedAt());
            publicDoc.setGroupName(result.getGroupName());
            publicDoc.setTags(result.getTags());
            publicDoc.setLastActivity(result.getLastActivity());
            return Result.success(publicDoc);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 同步EMQX系统的Topic数据
     *
     * @param systemId EMQX系统ID
     * @return 同步结果
     */
    @PostMapping("/sync/{systemId}")
    public Result<TopicSyncResult> syncTopicsFromEmqx(@PathVariable("systemId") Long systemId) {
        try {
            // 检查限流
           if (!rateLimitService.isTopicSyncAllowed(systemId)) {
               Long remainingTime = rateLimitService.getTopicSyncRemainingTime(systemId);
               String message = String.format("同步操作过于频繁，请等待 %d 秒后再试", remainingTime);
               return Result.error(message);
           }
            
            TopicSyncResult result = topicService.syncTopicsFromEmqx(systemId);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

}