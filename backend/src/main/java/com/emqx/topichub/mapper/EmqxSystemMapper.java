package com.emqx.topichub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emqx.topichub.entity.EmqxSystem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmqxSystemMapper extends BaseMapper<EmqxSystem> {

    // 可以在这里添加自定义的SQL方法

}