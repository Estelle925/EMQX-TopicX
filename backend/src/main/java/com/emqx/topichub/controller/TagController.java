package com.emqx.topichub.controller;

import com.emqx.topichub.common.Result;
import com.emqx.topichub.dto.TagCreateRequest;
import com.emqx.topichub.dto.TagDTO;
import com.emqx.topichub.dto.TagUpdateRequest;
import com.emqx.topichub.entity.Tag;
import com.emqx.topichub.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签管理控制器
 * 提供标签管理相关的API接口
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 获取所有标签列表
     *
     * @return 标签列表
     */
    @GetMapping
    public Result<List<TagDTO>> getAllTags() {
        List<Tag> tags = tagService.list();
        List<TagDTO> tagDTOs = tags.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return Result.success(tagDTOs);
    }

    /**
     * 根据ID获取标签详情
     *
     * @param id 标签ID
     * @return 标签详情
     */
    @GetMapping("/{id}")
    public Result<TagDTO> getTagById(@PathVariable Long id) {
        Tag tag = tagService.getById(id);
        if (tag == null) {
            return Result.error("标签不存在");
        }
        return Result.success(convertToDTO(tag));
    }

    /**
     * 创建新标签
     *
     * @param request 创建标签请求
     * @return 创建的标签信息
     */
    @PostMapping
    public Result<TagDTO> createTag(@Valid @RequestBody TagCreateRequest request) {
        // 检查标签名称是否已存在
        Tag existingTag = tagService.lambdaQuery()
                .eq(Tag::getName, request.getName())
                .one();
        if (existingTag != null) {
            return Result.error("标签名称已存在");
        }
        
        Tag tag = new Tag();
        BeanUtils.copyProperties(request, tag);
        tag.setUsageCount(0); // 初始化使用次数为0
        
        boolean saved = tagService.save(tag);
        if (saved) {
            return Result.success(convertToDTO(tag));
        } else {
            return Result.error("创建标签失败");
        }
    }

    /**
     * 更新标签信息
     *
     * @param id 标签ID
     * @param request 更新标签请求
     * @return 更新后的标签信息
     */
    @PutMapping("/{id}")
    public Result<TagDTO> updateTag(@PathVariable Long id,
                                   @Valid @RequestBody TagUpdateRequest request) {
        Tag existingTag = tagService.getById(id);
        if (existingTag == null) {
            return Result.error("标签不存在");
        }
        
        // 检查标签名称是否已被其他标签使用
        Tag duplicateTag = tagService.lambdaQuery()
                .eq(Tag::getName, request.getName())
                .ne(Tag::getId, id)
                .one();
        if (duplicateTag != null) {
            return Result.error("标签名称已存在");
        }
        
        BeanUtils.copyProperties(request, existingTag);
        boolean updated = tagService.updateById(existingTag);
        if (updated) {
            return Result.success(convertToDTO(existingTag));
        } else {
            return Result.error("更新标签失败");
        }
    }

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        Tag existingTag = tagService.getById(id);
        if (existingTag == null) {
            return Result.error("标签不存在");
        }
        
        // TODO: 检查标签是否被Topic使用，如果被使用则不允许删除
        // 这里可以添加业务逻辑检查
        
        boolean deleted = tagService.removeById(id);
        if (deleted) {
            return Result.success(null);
        } else {
            return Result.error("删除标签失败");
        }
    }

    /**
     * 根据关键词搜索标签
     *
     * @param keyword 搜索关键词
     * @return 标签列表
     */
    @GetMapping("/search")
    public Result<List<TagDTO>> searchTags(@RequestParam(required = false) String keyword) {
        List<Tag> tags;
        if (keyword == null || keyword.trim().isEmpty()) {
            tags = tagService.list();
        } else {
            tags = tagService.lambdaQuery()
                    .like(Tag::getName, keyword)
                    .list();
        }
        
        List<TagDTO> tagDTOs = tags.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return Result.success(tagDTOs);
    }

    /**
     * 将Tag实体转换为TagDTO
     *
     * @param tag Tag实体
     * @return TagDTO
     */
    private TagDTO convertToDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        BeanUtils.copyProperties(tag, dto);
        return dto;
    }
}