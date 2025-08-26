package com.emqx.topichub.dto;

import lombok.Data;

/**
 * Topic同步结果DTO
 * 用于返回Topic同步操作的结果信息
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
public class TopicSyncResult {

    /**
     * 新增的Topic数量
     */
    private int syncedCount;

    /**
     * 更新的Topic数量
     */
    private int updatedCount;

    /**
     * 同步结果消息
     */
    private String message;

    /**
     * 构造函数
     */
    public TopicSyncResult() {
    }

    /**
     * 构造函数
     *
     * @param syncedCount  新增数量
     * @param updatedCount 更新数量
     * @param message      结果消息
     */
    public TopicSyncResult(int syncedCount, int updatedCount, String message) {
        this.syncedCount = syncedCount;
        this.updatedCount = updatedCount;
        this.message = message;
    }

    /**
     * 创建成功结果
     *
     * @param syncedCount  新增数量
     * @param updatedCount 更新数量
     * @return 同步结果
     */
    public static TopicSyncResult success(int syncedCount, int updatedCount) {
        String message = String.format("同步完成：新增 %d 个Topic，更新 %d 个Topic", syncedCount, updatedCount);
        return new TopicSyncResult(syncedCount, updatedCount, message);
    }

    /**
     * 创建失败结果
     *
     * @param message 错误消息
     * @return 同步结果
     */
    public static TopicSyncResult failure(String message) {
        return new TopicSyncResult(0, 0, message);
    }
}