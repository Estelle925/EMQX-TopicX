package com.emqx.topichub.controller;

import com.emqx.topichub.common.Result;
import com.emqx.topichub.dto.UserCreateRequest;
import com.emqx.topichub.dto.UserListResponse;
import com.emqx.topichub.dto.UserResponse;
import com.emqx.topichub.dto.UserUpdateRequest;
import com.emqx.topichub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 处理用户管理相关请求
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     *
     * @param page 页码，默认为1
     * @param size 每页大小，默认为10
     * @param keyword 搜索关键词
     * @return 用户列表响应
     */
    @GetMapping
    public Result<UserListResponse> getUserList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        UserListResponse response = userService.getUserList(page, size, keyword);
        return Result.success(response);
    }

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public Result<UserResponse> getUserById(@PathVariable("id") Long id) {
        UserResponse response = userService.getUserById(id);
        return Result.success(response);
    }

    /**
     * 创建用户
     *
     * @param request 创建用户请求
     * @return 创建的用户信息
     */
    @PostMapping
    public Result<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return Result.success(response);
    }

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param request 更新用户请求
     * @return 更新后的用户信息
     */
    @PostMapping("/{id}/update")
    public Result<UserResponse> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return Result.success(response);
    }

    /**
     * 禁用用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PostMapping("/{id}/disable")
    public Result<Void> disableUser(@PathVariable("id") Long id) {
        userService.disableUser(id);
        return Result.success();
    }

    /**
     * 启用用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PostMapping("/{id}/enable")
    public Result<Void> enableUser(@PathVariable("id") Long id) {
        userService.enableUser(id);
        return Result.success();
    }

    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 操作结果
     */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(
            @PathVariable("id") Long id,
            @RequestParam("newPassword") String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success();
    }

    /**
     * 批量禁用用户
     *
     * @param ids 用户ID列表
     * @return 操作结果
     */
    @PostMapping("/batch/disable")
    public Result<Void> batchDisableUsers(@RequestBody Long[] ids) {
        for (Long id : ids) {
            userService.disableUser(id);
        }
        return Result.success();
    }

    /**
     * 批量启用用户
     *
     * @param ids 用户ID列表
     * @return 操作结果
     */
    @PostMapping("/batch/enable")
    public Result<Void> batchEnableUsers(@RequestBody Long[] ids) {
        for (Long id : ids) {
            userService.enableUser(id);
        }
        return Result.success();
    }

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @return 操作结果
     */
    @PostMapping("/batch/delete")
    public Result<Void> batchDeleteUsers(@RequestBody Long[] ids) {
        for (Long id : ids) {
            userService.deleteUser(id);
        }
        return Result.success();
    }
}