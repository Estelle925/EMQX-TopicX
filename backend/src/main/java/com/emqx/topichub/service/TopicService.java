package com.emqx.topichub.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emqx.topichub.entity.Topic;
import com.emqx.topichub.mapper.TopicMapper;
import org.springframework.stereotype.Service;

@Service
public class TopicService extends ServiceImpl<TopicMapper, Topic> {
    
    // 可以在这里添加自定义的业务方法
    
}