package com.emqx.topichub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emqx.topichub.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    
    // 可以在这里添加自定义的SQL方法
    
}