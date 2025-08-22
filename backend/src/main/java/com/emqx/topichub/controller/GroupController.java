package com.emqx.topichub.controller;

import com.emqx.topichub.common.Result;
import com.emqx.topichub.dto.GroupCreateRequest;
import com.emqx.topichub.dto.GroupDTO;
import com.emqx.topichub.dto.GroupUpdateRequest;
import com.emqx.topichub.entity.Group;
import com.emqx.topichub.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分组管理控制器
 * 提供分组管理相关的API接口
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 获取所有分组列表
     *
     * @return 分组列表
     */
    @GetMapping
    public Result<List<GroupDTO>> getAllGroups() {
        List<Group> groups = groupService.list();
        List<GroupDTO> groupDTOs = groups.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return Result.success(groupDTOs);
    }

    /**
     * 根据ID获取分组详情
     *
     * @param id 分组ID
     * @return 分组详情
     */
    @GetMapping("/{id}")
    public Result<GroupDTO> getGroupById(@PathVariable Long id) {
        Group group = groupService.getById(id);
        if (group == null) {
            return Result.error("分组不存在");
        }
        return Result.success(convertToDTO(group));
    }

    /**
     * 创建新分组
     *
     * @param request 创建分组请求
     * @return 创建的分组信息
     */
    @PostMapping
    public Result<GroupDTO> createGroup(@Valid @RequestBody GroupCreateRequest request) {
        Group group = new Group();
        BeanUtils.copyProperties(request, group);
        group.setTopicCount(0); // 初始化Topic数量为0
        
        boolean saved = groupService.save(group);
        if (saved) {
            return Result.success(convertToDTO(group));
        } else {
            return Result.error("创建分组失败");
        }
    }

    /**
     * 更新分组信息
     *
     * @param id 分组ID
     * @param request 更新分组请求
     * @return 更新后的分组信息
     */
    @PutMapping("/{id}")
    public Result<GroupDTO> updateGroup(@PathVariable Long id,
                                       @Valid @RequestBody GroupUpdateRequest request) {
        Group existingGroup = groupService.getById(id);
        if (existingGroup == null) {
            return Result.error("分组不存在");
        }
        
        BeanUtils.copyProperties(request, existingGroup);
        boolean updated = groupService.updateById(existingGroup);
        if (updated) {
            return Result.success(convertToDTO(existingGroup));
        } else {
            return Result.error("更新分组失败");
        }
    }

    /**
     * 删除分组
     *
     * @param id 分组ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        Group existingGroup = groupService.getById(id);
        if (existingGroup == null) {
            return Result.error("分组不存在");
        }
        
        boolean deleted = groupService.removeById(id);
        if (deleted) {
            return Result.success(null);
        } else {
            return Result.error("删除分组失败");
        }
    }

    /**
     * 根据关键词搜索分组
     *
     * @param keyword 搜索关键词
     * @return 分组列表
     */
    @GetMapping("/search")
    public Result<List<GroupDTO>> searchGroups(@RequestParam(required = false) String keyword) {
        List<Group> groups;
        if (keyword == null || keyword.trim().isEmpty()) {
            groups = groupService.list();
        } else {
            groups = groupService.lambdaQuery()
                    .like(Group::getName, keyword)
                    .or()
                    .like(Group::getDescription, keyword)
                    .list();
        }
        
        List<GroupDTO> groupDTOs = groups.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return Result.success(groupDTOs);
    }

    /**
     * 将Group实体转换为GroupDTO
     *
     * @param group Group实体
     * @return GroupDTO
     */
    private GroupDTO convertToDTO(Group group) {
        GroupDTO dto = new GroupDTO();
        BeanUtils.copyProperties(group, dto);
        return dto;
    }
}