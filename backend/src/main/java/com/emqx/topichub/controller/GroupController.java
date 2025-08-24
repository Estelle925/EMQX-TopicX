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
 * 业务管理控制器
 * 提供业务管理相关的API接口
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
     * 获取所有业务列表
     *
     * @return 业务列表
     */
    @GetMapping
    public Result<List<GroupDTO>> getAllGroups() {
        List<Group> groups = groupService.list();
        List<GroupDTO> groupDtoList = groups.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return Result.success(groupDtoList);
    }

    /**
     * 根据ID获取业务详情
     *
     * @param id 业务ID
     * @return 业务详情
     */
    @GetMapping("/{id}")
    public Result<GroupDTO> getGroupById(@PathVariable("id") Long id) {
        Group group = groupService.getById(id);
        if (group == null) {
            return Result.error("业务不存在");
        }
        return Result.success(convertToDTO(group));
    }

    /**
     * 创建新业务
     *
     * @param request 创建业务请求
     * @return 创建的业务信息
     */
    @PostMapping
    public Result<GroupDTO> createGroup(@Valid @RequestBody GroupCreateRequest request) {
        Group group = new Group();
        BeanUtils.copyProperties(request, group);
        // 初始化Topic数量为0
        group.setTopicCount(0);
        
        boolean saved = groupService.save(group);
        if (saved) {
            return Result.success(convertToDTO(group));
        } else {
            return Result.error("创建业务失败");
        }
    }

    /**
     * 更新业务信息
     *
     * @param id 业务ID
     * @param request 更新业务请求
     * @return 更新后的业务信息
     */
    @PutMapping("/{id}")
    public Result<GroupDTO> updateGroup(@PathVariable("id") Long id,
                                       @Valid @RequestBody GroupUpdateRequest request) {
        Group existingGroup = groupService.getById(id);
        if (existingGroup == null) {
            return Result.error("业务不存在");
        }
        
        BeanUtils.copyProperties(request, existingGroup);
        boolean updated = groupService.updateById(existingGroup);
        if (updated) {
            return Result.success(convertToDTO(existingGroup));
        } else {
            return Result.error("更新业务失败");
        }
    }

    /**
     * 删除业务
     *
     * @param id 业务ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteGroup(@PathVariable("id") Long id) {
        Group existingGroup = groupService.getById(id);
        if (existingGroup == null) {
            return Result.error("业务不存在");
        }
        
        boolean deleted = groupService.removeById(id);
        if (deleted) {
            return Result.success(null);
        } else {
            return Result.error("删除业务失败");
        }
    }

    /**
     * 根据关键词搜索业务
     *
     * @param keyword 搜索关键词
     * @return 业务列表
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
        
        List<GroupDTO> groupDtoList = groups.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return Result.success(groupDtoList);
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