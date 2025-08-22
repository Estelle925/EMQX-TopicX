package com.emqx.topichub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emqx.topichub.dto.*;
import com.emqx.topichub.entity.Group;
import com.emqx.topichub.entity.Tag;
import com.emqx.topichub.entity.Topic;
import com.emqx.topichub.entity.TopicTag;
import com.emqx.topichub.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService extends ServiceImpl<TopicMapper, Topic> {

    private final GroupService groupService;
    private final TagService tagService;
    private final TopicTagService topicTagService;

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

        // 分组筛选
        if (request.getGroupId() != null) {
            queryWrapper.eq("group_id", request.getGroupId());
        }

        // 系统筛选
        if (request.getSystemId() != null) {
            queryWrapper.eq("system_id", request.getSystemId());
        }

        // 排序
        if ("asc".equalsIgnoreCase(request.getSortDir())) {
            queryWrapper.orderByAsc(request.getSortBy());
        } else {
            queryWrapper.orderByDesc(request.getSortBy());
        }

        // 分页查询
        Page<Topic> page = new Page<>(request.getPage(), request.getSize());
        IPage<Topic> topicPage = this.page(page, queryWrapper);

        // 转换为DTO
        List<TopicDTO> topicDTOs = topicPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 如果有标签筛选，需要进一步过滤
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            topicDTOs = topicDTOs.stream()
                    .filter(topic -> topic.getTags().stream()
                            .anyMatch(tag -> request.getTagIds().contains(tag.getId())))
                    .collect(Collectors.toList());
        }

        // 构建返回结果
        Page<TopicDTO> resultPage = new Page<>(request.getPage(), request.getSize());
        resultPage.setRecords(topicDTOs);
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
    public TopicDTO getTopicDTOById(Long id) {
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
            }
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
            // 更新标签关联
            updateTopicTags(id, request.getTagIds());
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
            // 删除标签关联
            QueryWrapper<TopicTag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.eq("topic_id", id).eq("deleted", false);
            List<TopicTag> topicTags = topicTagService.list(tagQueryWrapper);
            topicTags.forEach(topicTag -> {
                topicTag.setDeleted(1);
            });
            topicTagService.updateBatchById(topicTags);
        } else {
            throw new RuntimeException("删除Topic失败");
        }
    }

    /**
     * 批量操作Topic
     *
     * @param request 批量操作请求
     */
    public void batchOperation(TopicBatchRequest request) {
        List<Topic> topics = this.listByIds(request.getTopicIds());
        if (topics.size() != request.getTopicIds().size()) {
            throw new RuntimeException("部分Topic不存在");
        }

        switch (request.getAction()) {
            case "assignGroup" -> batchAssignGroup(request.getTopicIds(), request.getGroupId());
            case "addTags" -> batchAddTags(request.getTopicIds(), request.getTagIds());
            case "removeTags" -> batchRemoveTags(request.getTopicIds(), request.getTagIds());
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
                .collect(Collectors.toList());

        // 过滤出新标签
        List<Long> newTagIds = tagIds.stream()
                .filter(tagId -> !existingTagIds.contains(tagId))
                .collect(Collectors.toList());

        if (!newTagIds.isEmpty()) {
            saveTopicTags(id, newTagIds);
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
    }

    // ========== 私有辅助方法 ==========

    /**
     * 转换Topic实体为DTO
     */
    private TopicDTO convertToDTO(Topic topic) {
        TopicDTO dto = new TopicDTO();
        BeanUtils.copyProperties(topic, dto);

        // 设置分组名称
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
     * 批量分配分组
     */
    private void batchAssignGroup(List<Long> topicIds, Long groupId) {
        List<Topic> topics = this.listByIds(topicIds);
        topics.forEach(topic -> {
            topic.setGroupId(groupId);
            topic.setUpdatedAt(LocalDateTime.now());
        });
        this.updateBatchById(topics);
    }

    /**
     * 批量添加标签
     */
    private void batchAddTags(List<Long> topicIds, List<Long> tagIds) {
        for (Long topicId : topicIds) {
            // 获取现有标签
            List<Long> existingTagIds = getTagsByTopicId(topicId).stream()
                    .map(TagDTO::getId)
                    .collect(Collectors.toList());

            // 过滤出新标签
            List<Long> newTagIds = tagIds.stream()
                    .filter(tagId -> !existingTagIds.contains(tagId))
                    .collect(Collectors.toList());

            if (!newTagIds.isEmpty()) {
                saveTopicTags(topicId, newTagIds);
            }
        }
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
    }
}