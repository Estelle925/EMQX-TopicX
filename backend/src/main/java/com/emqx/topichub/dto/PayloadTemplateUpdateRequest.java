package com.emqx.topichub.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload模板更新请求DTO
 * 用于接收更新Payload模板的请求参数
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class PayloadTemplateUpdateRequest {

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 255, message = "模板名称长度不能超过255个字符")
    private String name;

    /**
     * 所属业务ID
     */
    @NotNull(message = "业务分组不能为空")
    private Long groupId;

    /**
     * 模板类型 (JSON, XML, Text, Binary)
     */
    @NotBlank(message = "模板类型不能为空")
    private String type;

    /**
     * 模板描述
     */
    @Size(max = 1000, message = "模板描述长度不能超过1000个字符")
    private String description;

    /**
     * Payload内容
     */
    @NotBlank(message = "Payload内容不能为空")
    @Size(max = 10000, message = "Payload内容长度不能超过10000个字符")
    private String payload;
}