package com.emqx.topichub.service;

import cn.dev33.satoken.stp.StpUtil;
import com.emqx.topichub.dto.LoginRequest;
import com.emqx.topichub.dto.LoginResponse;
import com.emqx.topichub.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认证服务类
 * 处理用户登录认证业务逻辑
 * 
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    /**
     * 用户登录验证
     * 验证用户名密码，生成Sa-Token令牌
     * 
     * @param request 登录请求参数
     * @return 登录响应结果
     * @throws RuntimeException 当用户名或密码错误时抛出异常
     */
    public LoginResponse login(LoginRequest request) {
        // 使用UserService验证用户
        User user = userService.validateUser(request.getUsername(), request.getPassword());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 使用Sa-Token进行登录
        StpUtil.login(user.getId());
        
        // 更新用户最后登录时间
        userService.updateLastLoginTime(user.getId());
        
        // 获取token
        String token = StpUtil.getTokenValue();
        Long expiresAt = System.currentTimeMillis() + StpUtil.getTokenTimeout() * 1000;

        return new LoginResponse(token, user.getUsername(), expiresAt);
    }

    /**
     * 用户登出
     * 清除用户会话信息
     */
    public void logout() {
        StpUtil.logout();
    }
    
    /**
     * 检查当前用户是否已登录
     * 
     * @return 是否已登录
     */
    public boolean isLogin() {
        return StpUtil.isLogin();
    }
    
    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID
     */
    public Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
    
    /**
     * 获取当前登录用户信息
     * 
     * @return 用户信息
     */
    public User getCurrentUser() {
        if (!isLogin()) {
            return null;
        }
        return userService.getById(getCurrentUserId());
    }

}