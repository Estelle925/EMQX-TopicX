package com.emqx.topichub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Topic数据传输对象
 * 用于返回Topic信息给前端
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class TopicDTO {

    /**
     * Topic ID
     */
    private Long id;

    /**
     * Topic名称
     */
    private String name;

    /**
     * Topic路径
     */
    private String path;

    /**
     * 所属EMQX系统ID
     */
    private Long systemId;

    /**
     * 所属业务ID
     */
    private Long groupId;

    /**
     * 所属业务名称
     */
    private String groupName;

    /**
     * 最后活动时间
     */
    private LocalDateTime lastActivity;

    /**
     * Payload说明文档
     */
    private String payloadDoc;

    /**
     * 关联的标签列表
     */
    private List<TagDTO> tags;

    /**
     * Topic状态（根据最后活动时间计算）
     */
    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}