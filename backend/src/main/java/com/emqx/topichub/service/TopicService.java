package com.emqx.topichub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emqx.topichub.dto.*;
import com.emqx.topichub.entity.EmqxSystem;
import com.emqx.topichub.service.EmqxSystemService;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Set;
import com.emqx.topichub.entity.Group;
import com.emqx.topichub.entity.Tag;
import com.emqx.topichub.entity.Topic;
import com.emqx.topichub.entity.TopicTag;
import com.emqx.topichub.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author EMQX Topic Hub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicService extends ServiceImpl<TopicMapper, Topic> {

    private final GroupService groupService;
    private final TagService tagService;
    private final TopicTagService topicTagService;
    private final EmqxSystemService emqxSystemService;
    private final EmqxService emqxService;
    private final PayloadTemplateService payloadTemplateService;

    /**
     * 分页搜索Topic列表
     *
     * @param request 搜索请求参数
     * @return 分页Topic列表
     */
    public IPage<TopicDTO> searchTopics(TopicSearchRequest request) {
        // 构建查询条件
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);

        // 关键字搜索（Topic名称或路径）
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("name", request.getKeyword())
                    .or()
                    .like("path", request.getKeyword())
            );
        }

        // 业务筛选
        if (request.getGroupId() != null) {
            queryWrapper.eq("group_id", request.getGroupId());
        }

        // 系统筛选
        if (request.getSystemId() != null) {
            queryWrapper.eq("system_id", request.getSystemId());
        }

        // 排序
        if (StringUtils.hasText(request.getSortBy())) {
            String sortBy = request.getSortBy();
            //驼峰转下划线
            String asc = "asc";
            sortBy = sortBy.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
            if (asc.equalsIgnoreCase(request.getSortDir())) {
                queryWrapper.orderByAsc(sortBy);
            } else {
                queryWrapper.orderByDesc(sortBy);
            }
        }

        // 分页查询
        Page<Topic> page = new Page<>(request.getPage(), request.getSize());
        IPage<Topic> topicPage = this.page(page, queryWrapper);

        // 转换为DTO
        List<TopicDTO> topicDtoList = topicPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 如果有标签筛选，需要进一步过滤
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            topicDtoList = topicDtoList.stream()
                    .filter(topic -> topic.getTags().stream()
                            .anyMatch(tag -> request.getTagIds().contains(tag.getId())))
                    .collect(Collectors.toList());
        }

        // 构建返回结果
        Page<TopicDTO> resultPage = new Page<>(request.getPage(), request.getSize());
        resultPage.setRecords(topicDtoList);
        resultPage.setTotal(topicPage.getTotal());
        resultPage.setPages(topicPage.getPages());

        return resultPage;
    }

    /**
     * 根据ID获取Topic详情
     *
     * @param id Topic ID
     * @return Topic详情
     */
    public TopicDTO getTopicDtoById(Long id) {
        Topic topic = this.getById(id);
        if (topic == null || topic.getDeleted() != 0) {
            throw new RuntimeException("Topic不存在");
        }
        return convertToDTO(topic);
    }

    /**
     * 创建新Topic
     *
     * @param request 创建请求
     * @return 创建的Topic
     */
    public TopicDTO createTopic(TopicCreateRequest request) {
        // 检查Topic路径是否已存在
        QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("path", request.getPath())
                .eq("system_id", request.getSystemId())
                .eq("deleted", false);
        if (this.count(queryWrapper) > 0) {
            throw new RuntimeException("该系统中已存在相同路径的Topic");
        }

        // 创建Topic
        Topic topic = new Topic();
        BeanUtils.copyProperties(request, topic);
        topic.setCreatedAt(LocalDateTime.now());
        topic.setUpdatedAt(LocalDateTime.now());
        topic.setDeleted(0);

        if (this.save(topic)) {
            // 处理标签关联
            if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
                saveTopicTags(topic.getId(), request.getTagIds());
                // 更新标签使用次数统计
                updateTagsUsageCount(request.getTagIds());
            }
            
            // 更新分组Topic数量统计
            updateGroupTopicCount(topic.getGroupId());
            
            return convertToDTO(topic);
        } else {
            throw new RuntimeException("创建Topic失败");
        }
    }

    /**
     * 更新Topic
     *
     * @param id      Topic ID
     * @param request 更新请求
     * @return 更新后的Topic
     */
    public TopicDTO updateTopic(Long id, TopicUpdateRequest request) {
        Topic existingTopic = this.getById(id);
        if (existingTopic == null || existingTopic.getDeleted() != 0) {
            throw new RuntimeException("Topic不存在");
        }

        // 检查路径是否与其他Topic冲突
        if (!existingTopic.getPath().equals(request.getPath())) {
            QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("path", request.getPath())
                    .eq("system_id", existingTopic.getSystemId())
                    .ne("id", id)
                    .eq("deleted", false);
            if (this.count(queryWrapper) > 0) {
                throw new RuntimeException("该系统中已存在相同路径的Topic");
            }
        }

        // 更新Topic
        BeanUtils.copyProperties(request, existingTopic);
        existingTopic.setUpdatedAt(LocalDateTime.now());

        if (this.updateById(existingTopic)) {
            // 获取原有标签ID列表
            List<Long> oldTagIds = getTagsByTopicId(id).stream()
                    .map(TagDTO::getId)
                    .collect(Collectors.toList());
            
            // 获取原有分组ID
            Long oldGroupId = existingTopic.getGroupId();
            
            // 更新标签关联
            updateTopicTags(id, request.getTagIds());
            
            // 更新标签使用次数统计（包括新旧标签）
            updateTagsUsageCount(oldTagIds);
            if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
                updateTagsUsageCount(request.getTagIds());
            }
            
            // 如果分组发生变更，更新相关分组的Topic数量统计
            if (!oldGroupId.equals(request.getGroupId())) {
                updateGroupTopicCount(oldGroupId); // 更新原分组
                updateGroupTopicCount(request.getGroupId()); // 更新新分组
            }
            
            return convertToDTO(existingTopic);
        } else {
            throw new RuntimeException("更新Topic失败");
        }
    }

    /**
     * 删除Topic
     *
     * @param id Topic ID
     */
    public void deleteTopic(Long id) {
        Topic topic = this.getById(id);
        if (topic == null || topic.getDeleted() != 0) {
            throw new RuntimeException("Topic不存在");
        }

        // 软删除Topic
        topic.setDeleted(1);
        topic.setUpdatedAt(LocalDateTime.now());

        if (this.updateById(topic)) {
            // 获取要删除的标签ID列表
            List<Long> tagIds = getTagsByTopicId(id).stream()
                    .map(TagDTO::getId)
                    .collect(Collectors.toList());
            
            // 删除标签关联
            QueryWrapper<TopicTag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("topic_id", id).eq("deleted", false);
            List<TopicTag> topicTags = topicTagService.list(tagQueryWrapper);
            topicTags.forEach(topicTag -> {
                topicTag.setDeleted(1);
            });
            topicTagService.updateBatchById(topicTags);
            
            // 更新标签使用次数统计
            updateTagsUsageCount(tagIds);
            
            // 更新分组Topic数量统计
            updateGroupTopicCount(topic.getGroupId());
        } else {
            throw new RuntimeException("删除Topic失败");
        }
    }

    /**
     * 批量操作Topic
     *
     * @param request 批量操作请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchOperation(TopicBatchRequest request) {
        List<Topic> topics = this.listByIds(request.getTopicIds());
        if (topics.size() != request.getTopicIds().size()) {
            throw new RuntimeException("部分Topic不存在");
        }

        switch (request.getAction()) {
            case "assignGroup" -> batchAssignGroup(request.getTopicIds(), request.getGroupId());
            case "addTags" -> batchAddTags(request.getTopicIds(), request.getTagIds());
            case "removeTags" -> batchRemoveTags(request.getTopicIds(), request.getTagIds());
            case "updatePayload" -> batchUpdatePayload(request.getTopicIds(), request.getTemplateId(), request.getPayloadDoc());
            default -> throw new RuntimeException("不支持的操作类型");
        }
    }

    /**
     * 获取Topic的标签列表
     *
     * @param id Topic ID
     * @return 标签列表
     */
    public List<TagDTO> getTopicTags(Long id) {
        Topic topic = this.getById(id);
        if (topic == null || topic.getDeleted() != 0) {
            throw new RuntimeException("Topic不存在");
        }

        return getTagsByTopicId(id);
    }

    /**
     * 为Topic添加标签
     *
     * @param id     Topic ID
     * @param tagIds 标签ID列表
     */
    public void addTopicTags(Long id, List<Long> tagIds) {
        Topic topic = this.getById(id);
        if (topic == null || topic.getDeleted() != 0) {
            throw new RuntimeException("Topic不存在");
        }

        // 获取现有标签
        List<Long> existingTagIds = getTagsByTopicId(id).stream()
                .map(TagDTO::getId)
                .toList();

        // 过滤出新标签
        List<Long> newTagIds = tagIds.stream()
                .filter(tagId -> !existingTagIds.contains(tagId))
                .collect(Collectors.toList());

        if (!newTagIds.isEmpty()) {
            saveTopicTags(id, newTagIds);
            // 更新标签使用次数统计
            updateTagsUsageCount(newTagIds);
        }
    }

    /**
     * 移除Topic的标签
     *
     * @param id     Topic ID
     * @param tagIds 标签ID列表
     */
    public void removeTopicTags(Long id, List<Long> tagIds) {
        Topic topic = this.getById(id);
        if (topic == null || topic.getDeleted() != 0) {
            throw new RuntimeException("Topic不存在");
        }

        QueryWrapper<TopicTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("topic_id", id)
                .in("tag_id", tagIds)
                .eq("deleted", false);

        List<TopicTag> topicTags = topicTagService.list(queryWrapper);
        topicTags.forEach(topicTag -> {
            topicTag.setDeleted(1);
        });

        topicTagService.updateBatchById(topicTags);
        
        // 更新标签使用次数统计
        updateTagsUsageCount(tagIds);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 转换Topic实体为DTO
     */
    private TopicDTO convertToDTO(Topic topic) {
        TopicDTO dto = new TopicDTO();
        BeanUtils.copyProperties(topic, dto);

        // 设置业务名称
        if (topic.getGroupId() != null) {
            Group group = groupService.getById(topic.getGroupId());
            if (group != null && group.getDeleted() == 0) {
                dto.setGroupName(group.getName());
            }
        }

        // 设置标签列表
        dto.setTags(getTagsByTopicId(topic.getId()));

        // 设置状态
        dto.setStatus(topic.getDeleted() != 0 ? "disabled" : "enabled");

        return dto;
    }

    /**
     * 根据Topic ID获取标签列表
     */
    private List<TagDTO> getTagsByTopicId(Long topicId) {
        QueryWrapper<TopicTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("topic_id", topicId).eq("deleted", false);
        List<TopicTag> topicTags = topicTagService.list(queryWrapper);

        if (topicTags.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> tagIds = topicTags.stream()
                .map(TopicTag::getTagId)
                .collect(Collectors.toList());

        List<Tag> tags = tagService.listByIds(tagIds);
        return tags.stream()
                .filter(tag -> tag.getDeleted() == 0)
                .map(this::convertTagToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换Tag实体为DTO
     */
    private TagDTO convertTagToDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        BeanUtils.copyProperties(tag, dto);
        return dto;
    }

    /**
     * 保存Topic标签关联
     */
    private void saveTopicTags(Long topicId, List<Long> tagIds) {
        List<TopicTag> topicTags = tagIds.stream()
                .map(tagId -> {
                    TopicTag topicTag = new TopicTag();
                    topicTag.setTopicId(topicId);
                    topicTag.setTagId(tagId);
                    topicTag.setCreatedAt(LocalDateTime.now());
                    topicTag.setDeleted(0);
                    return topicTag;
                })
                .collect(Collectors.toList());
        topicTagService.saveBatch(topicTags);
    }

    /**
     * 更新Topic标签关联
     */
    private void updateTopicTags(Long topicId, List<Long> newTagIds) {
        // 删除现有关联
        QueryWrapper<TopicTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("topic_id", topicId).eq("deleted", false);
        List<TopicTag> existingTopicTags = topicTagService.list(queryWrapper);
        existingTopicTags.forEach(topicTag -> {
            topicTag.setDeleted(1);
        });
        topicTagService.updateBatchById(existingTopicTags);

        // 添加新关联
        if (newTagIds != null && !newTagIds.isEmpty()) {
            saveTopicTags(topicId, newTagIds);
        }
    }

    /**
     * 批量分配业务
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchAssignGroup(List<Long> topicIds, Long groupId) {
        List<Topic> topics = this.listByIds(topicIds);
        
        // 收集所有涉及的分组ID（包括原分组和新分组）
        Set<Long> affectedGroupIds = topics.stream()
                .map(Topic::getGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        affectedGroupIds.add(groupId);
        
        topics.forEach(topic -> {
            topic.setGroupId(groupId);
            topic.setUpdatedAt(LocalDateTime.now());
        });
        this.updateBatchById(topics);
        
        // 更新所有涉及分组的Topic数量统计
        for (Long affectedGroupId : affectedGroupIds) {
            updateGroupTopicCount(affectedGroupId);
        }
    }

    /**
     * 批量添加标签
     */
    private void batchAddTags(List<Long> topicIds, List<Long> tagIds) {
        for (Long topicId : topicIds) {
            // 获取现有标签
            List<Long> existingTagIds = getTagsByTopicId(topicId).stream()
                    .map(TagDTO::getId)
                    .toList();

            // 过滤出新标签
            List<Long> newTagIds = tagIds.stream()
                    .filter(tagId -> !existingTagIds.contains(tagId))
                    .collect(Collectors.toList());

            if (!newTagIds.isEmpty()) {
                saveTopicTags(topicId, newTagIds);
            }
        }
        
        // 更新标签使用次数统计
        updateTagsUsageCount(tagIds);
    }

    /**
     * 批量移除标签
     */
    private void batchRemoveTags(List<Long> topicIds, List<Long> tagIds) {
        for (Long topicId : topicIds) {
            QueryWrapper<TopicTag> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("topic_id", topicId)
                    .in("tag_id", tagIds)
                    .eq("deleted", false);

            List<TopicTag> topicTags = topicTagService.list(queryWrapper);
            topicTags.forEach(topicTag -> {
                topicTag.setDeleted(1);
            });
            topicTagService.updateBatchById(topicTags);
        }
        
        // 更新标签使用次数统计
        updateTagsUsageCount(tagIds);
    }

    /**
     * 批量更新Payload
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdatePayload(List<Long> topicIds, Long templateId, String payloadDoc) {
        List<Topic> topics = this.listByIds(topicIds);
        
        String finalPayloadDoc = payloadDoc;
        
        // 如果提供了模板ID，则从模板获取内容
        if (templateId != null) {
            try {
                PayloadTemplateDTO template = payloadTemplateService.getTemplateDtoById(templateId);
                finalPayloadDoc = template.getPayload();
                
                // 增加模板使用次数
                payloadTemplateService.useTemplate(templateId);
            } catch (Exception e) {
                throw new RuntimeException("获取Payload模板失败: " + e.getMessage());
            }
        }
        
        // 批量更新Topic的payloadDoc字段
        for (Topic topic : topics) {
            topic.setPayloadDoc(finalPayloadDoc);
            topic.setUpdatedAt(LocalDateTime.now());
        }
        
        this.updateBatchById(topics);
    }

    /**
     * 从EMQX系统同步Topic数据
     *
     * @param systemId EMQX系统ID
     * @return 同步结果
     */
    @Transactional(rollbackFor = Exception.class)
    public TopicSyncResult syncTopicsFromEmqx(Long systemId) {
        try {
            // 1. 获取EMQX系统信息
            EmqxSystem emqxSystem = emqxSystemService.getById(systemId);
            if (emqxSystem == null) {
                throw new RuntimeException("EMQX系统不存在，ID: " + systemId);
            }

            if (!"online".equals(emqxSystem.getStatus())) {
                throw new RuntimeException("EMQX系统离线，无法同步数据: " + emqxSystem.getName());
            }

            // 2. 调用EMQX API获取订阅信息
            Set<String> topicPaths = emqxService.fetchTopicsFromEmqx(emqxSystem);
            
            if (topicPaths.isEmpty()) {
                return TopicSyncResult.success(0, 0);
            }

            // 3. 同步到数据库
            return syncTopicsToDatabase(topicPaths, systemId);

        } catch (Exception e) {
            log.error("同步EMQX系统Topic失败，systemId: {}", systemId, e);
            throw new RuntimeException("同步失败: " + e.getMessage());
        }
    }



    /**
     * 将Topic数据同步到数据库
     *
     * @param topicPaths Topic路径集合
     * @param systemId   系统ID
     * @return 同步结果
     */
    private TopicSyncResult syncTopicsToDatabase(Set<String> topicPaths, Long systemId) {
        int syncedCount = 0;
        int updatedCount = 0;

        for (String topicPath : topicPaths) {
            try {
                // 检查Topic是否已存在
                QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("path", topicPath)
                        .eq("system_id", systemId)
                        .eq("deleted", false);
                
                Topic existingTopic = this.getOne(queryWrapper);
                
                if (existingTopic != null) {
                    // 更新现有Topic
                    existingTopic.setLastActivity(LocalDateTime.now());
                    existingTopic.setUpdatedAt(LocalDateTime.now());
                    this.updateById(existingTopic);
                    updatedCount++;
                } else {
                    // 创建新Topic
                    Topic newTopic = new Topic();
                    newTopic.setName(generateTopicName(topicPath));
                    newTopic.setPath(topicPath);
                    newTopic.setSystemId(systemId);
                    newTopic.setLastActivity(LocalDateTime.now());
                    newTopic.setCreatedAt(LocalDateTime.now());
                    newTopic.setUpdatedAt(LocalDateTime.now());
                    newTopic.setDeleted(0);
                    
                    this.save(newTopic);
                    syncedCount++;
                }
                
            } catch (Exception e) {
                log.error("同步Topic到数据库失败，path: {}", topicPath, e);
            }
        }

        log.info("Topic同步完成，新增: {}，更新: {}", syncedCount, updatedCount);
        
        return TopicSyncResult.success(syncedCount, updatedCount);
    }

    /**
     * 根据Topic路径生成Topic名称
     *
     * @param topicPath Topic路径
     * @return Topic名称
     */
    private String generateTopicName(String topicPath) {
        // 取路径的最后一段作为名称
        String[] parts = topicPath.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return topicPath;
    }

    /**
     * 更新分组的Topic数量统计
     *
     * @param groupId 分组ID
     */
    private void updateGroupTopicCount(Long groupId) {
        if (groupId == null) {
            return;
        }
        
        try {
            QueryWrapper<Topic> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("group_id", groupId).eq("deleted", false);
            long topicCount = this.count(queryWrapper);
            
            Group group = groupService.getById(groupId);
            if (group != null) {
                group.setTopicCount((int) topicCount);
                groupService.updateById(group);
            }
        } catch (Exception e) {
            log.warn("更新分组Topic数量统计失败，groupId: {}", groupId, e);
        }
    }

    /**
     * 更新标签的使用次数统计
     *
     * @param tagId 标签ID
     */
    private void updateTagUsageCount(Long tagId) {
        if (tagId == null) {
            return;
        }
        
        try {
            QueryWrapper<TopicTag> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("tag_id", tagId).eq("deleted", false);
            long usageCount = topicTagService.count(queryWrapper);
            
            Tag tag = tagService.getById(tagId);
            if (tag != null) {
                tag.setUsageCount((int) usageCount);
                tagService.updateById(tag);
            }
        } catch (Exception e) {
            log.warn("更新标签使用次数统计失败，tagId: {}", tagId, e);
        }
    }

    /**
     * 批量更新标签的使用次数统计
     *
     * @param tagIds 标签ID列表
     */
    private void updateTagsUsageCount(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        
        for (Long tagId : tagIds) {
            updateTagUsageCount(tagId);
        }
    }
}