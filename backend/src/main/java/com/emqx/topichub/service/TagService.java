package com.emqx.topichub.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emqx.topichub.entity.Tag;
import com.emqx.topichub.mapper.TagMapper;
import org.springframework.stereotype.Service;

@Service
public class TagService extends ServiceImpl<TagMapper, Tag> {

    // 可以在这里添加自定义的业务方法

}