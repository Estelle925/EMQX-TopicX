package com.emqx.topichub.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Topic更新请求DTO
 * 用于接收更新Topic的请求参数
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class TopicUpdateRequest {

    /**
     * Topic名称
     */
    @NotBlank(message = "Topic名称不能为空")
    @Size(max = 255, message = "Topic名称长度不能超过255个字符")
    private String name;

    /**
     * Topic路径
     */
    @NotBlank(message = "Topic路径不能为空")
    @Size(max = 500, message = "Topic路径长度不能超过500个字符")
    private String path;

    /**
     * 所属业务ID（可选）
     */
    private Long groupId;

    /**
     * Payload说明文档（可选）
     */
    @Size(max = 10000, message = "Payload说明文档长度不能超过10000个字符")
    private String payloadDoc;

    /**
     * 关联的标签ID列表（可选）
     */
    private List<Long> tagIds;
}