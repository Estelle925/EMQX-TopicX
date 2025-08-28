package com.emqx.topichub.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新标签请求对象
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class TagUpdateRequest {

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    private String name;

    /**
     * 标签颜色（十六进制色值）
     */
    @NotBlank(message = "标签颜色不能为空")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "标签颜色格式不正确，请使用十六进制色值（如：#FF0000）")
    private String color;
}