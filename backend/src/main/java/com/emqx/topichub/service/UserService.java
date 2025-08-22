package com.emqx.topichub.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emqx.topichub.entity.User;
import com.emqx.topichub.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    // 可以在这里添加自定义的业务方法
    
}