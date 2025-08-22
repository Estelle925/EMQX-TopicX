package com.emqx.topichub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emqx.topichub.entity.User;
import com.emqx.topichub.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

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
        if (user.getStatus().equals(1)) {
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

}