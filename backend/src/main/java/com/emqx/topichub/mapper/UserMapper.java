package com.emqx.topichub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emqx.topichub.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    // 可以在这里添加自定义的SQL方法
    
}