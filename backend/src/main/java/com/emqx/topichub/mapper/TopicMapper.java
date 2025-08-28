package com.emqx.topichub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emqx.topichub.entity.Topic;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author EMQX Topic Hub Team
 */
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

    // 可以在这里添加自定义的SQL方法

}