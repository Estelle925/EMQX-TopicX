package com.emqx.topichub.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.emqx.topichub.entity.Group;
import com.emqx.topichub.mapper.GroupMapper;
import org.springframework.stereotype.Service;

/**
 * @author EMQX Topic Hub Team
 */
@Service
public class GroupService extends ServiceImpl<GroupMapper, Group> {

    // 可以在这里添加自定义的业务方法

}