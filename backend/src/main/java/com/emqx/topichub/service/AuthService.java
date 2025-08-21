package com.emqx.topichub.service;

import com.emqx.topichub.dto.LoginRequest;
import com.emqx.topichub.dto.LoginResponse;
import com.emqx.topichub.util.JwtUtil;
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

    private final JwtUtil jwtUtil;

    /**
     * 系统内置管理员用户名
     */
    private static final String ADMIN_USERNAME = "admin";

    /**
     * 系统内置管理员密码
     */
    private static final String ADMIN_PASSWORD = "admin123";

    /**
     * 用户登录验证
     * 验证用户名密码，生成JWT令牌
     * 
     * @param request 登录请求参数
     * @return 登录响应结果
     * @throws RuntimeException 当用户名或密码错误时抛出异常
     */
    public LoginResponse login(LoginRequest request) {
        // 验证用户名密码
        if (!ADMIN_USERNAME.equals(request.getUsername()) || 
            !ADMIN_PASSWORD.equals(request.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成JWT令牌
        String token = jwtUtil.generateToken(ADMIN_USERNAME);
        Long expiresAt = System.currentTimeMillis() + jwtUtil.getExpiration();

        return new LoginResponse(token, ADMIN_USERNAME, expiresAt);
    }

    /**
     * 用户登出
     * 清除用户会话信息
     */
    public void logout() {
        // 简单实现，实际可以将token加入黑名单
        // 这里暂时不做处理，依赖前端清除token
    }

}