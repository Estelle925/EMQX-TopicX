package com.emqx.topichub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emqx.topichub.entity.TopicTag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TopicTagMapper extends BaseMapper<TopicTag> {

    // 可以在这里添加自定义的SQL方法

}