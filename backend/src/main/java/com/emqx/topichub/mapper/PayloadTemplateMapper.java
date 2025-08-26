package com.emqx.topichub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emqx.topichub.entity.PayloadTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * Payload模板Mapper接口
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Mapper
public interface PayloadTemplateMapper extends BaseMapper<PayloadTemplate> {

    // 可以在这里添加自定义的SQL方法

}