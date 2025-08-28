package com.emqx.topichub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emqx.topichub.dto.UserCreateRequest;
import com.emqx.topichub.dto.UserListResponse;
import com.emqx.topichub.dto.UserResponse;
import com.emqx.topichub.dto.UserUpdateRequest;
import com.emqx.topichub.entity.User;
import com.emqx.topichub.enums.UserStatusEnum;
import com.emqx.topichub.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务类
 * 处理用户相关业务逻辑
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    public User findByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, false));
    }

    /**
     * 验证用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息，验证失败返回null
     */
    public User validateUser(String username, String password) {
        User user = findByUsername(username);
        if (user == null) {
            return null;
        }

        // 验证密码（这里使用MD5加密，实际项目建议使用BCrypt）
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!encryptedPassword.equals(user.getPassword())) {
            return null;
        }

        // 检查用户状态
        if (UserStatusEnum.LOCKED.getCode().equals(user.getStatus())) {
            throw new RuntimeException("用户已被禁用");
        }

        return user;
    }

    /**
     * 更新用户最后登录时间
     *
     * @param userId 用户ID
     */
    public void updateLastLoginTime(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setLastLogin(LocalDateTime.now());
        this.updateById(user);
    }

    /**
     * 分页查询用户列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @return 用户列表响应
     */
    public UserListResponse getUserList(int page, int size, String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getDeleted, false)
                .orderByDesc(User::getCreatedAt);

        // 添加搜索条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }

        IPage<User> userPage = this.page(pageParam, queryWrapper);

        UserListResponse response = new UserListResponse();
        response.setTotal(userPage.getTotal());
        response.setPage(page);
        response.setSize(size);

        List<UserListResponse.UserInfo> userInfos = userPage.getRecords().stream()
                .map(this::convertToUserInfo)
                .collect(Collectors.toList());
        response.setUsers(userInfos);

        return response;
    }

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    public UserResponse getUserById(Long id) {
        User user = this.getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }
        return convertToUserResponse(user);
    }

    /**
     * 创建用户
     *
     * @param request 创建用户请求
     * @return 用户详情
     */
    public UserResponse createUser(UserCreateRequest request) {
        // 检查用户名是否已存在
        User existingUser = findByUsername(request.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        User existingEmailUser = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail())
                .eq(User::getDeleted, false));
        if (existingEmailUser != null) {
            throw new RuntimeException("邮箱已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(request.getPassword().getBytes()));
        user.setEmail(request.getEmail());
        user.setRealName(request.getRealName());
        user.setStatus(UserStatusEnum.ACTIVE.getCode());

        this.save(user);
        return convertToUserResponse(user);
    }

    /**
     * 更新用户
     *
     * @param id 用户ID
     * @param request 更新用户请求
     * @return 用户详情
     */
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = this.getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }

        // 检查邮箱是否已被其他用户使用
        User existingEmailUser = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail())
                .ne(User::getId, id)
                .eq(User::getDeleted, false));
        if (existingEmailUser != null) {
            throw new RuntimeException("邮箱已被其他用户使用");
        }

        user.setEmail(request.getEmail());
        user.setRealName(request.getRealName());
        user.setStatus(request.getStatus());

        this.updateById(user);
        return convertToUserResponse(user);
    }

    /**
     * 禁用用户
     *
     * @param id 用户ID
     */
    public void disableUser(Long id) {
        User user = this.getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(UserStatusEnum.LOCKED.getCode());
        this.updateById(user);
    }

    /**
     * 启用用户
     *
     * @param id 用户ID
     */
    public void enableUser(Long id) {
        User user = this.getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(UserStatusEnum.ACTIVE.getCode());
        this.updateById(user);
    }

    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     */
    public void deleteUser(Long id) {
        User user = this.getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }

        this.removeById(id);
    }

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @param newPassword 新密码
     */
    public void resetPassword(Long id, String newPassword) {
        User user = this.getById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        this.updateById(user);
    }

    /**
     * 转换为用户信息
     *
     * @param user 用户实体
     * @return 用户信息
     */
    private UserListResponse.UserInfo convertToUserInfo(User user) {
        UserListResponse.UserInfo userInfo = new UserListResponse.UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        userInfo.setCreatedAt(user.getCreatedAt());
        userInfo.setLastLoginAt(user.getLastLogin());
        return userInfo;
    }

    /**
     * 转换为用户响应
     *
     * @param user 用户实体
     * @return 用户响应
     */
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        response.setLastLoginAt(user.getLastLogin());
        return response;
    }

}