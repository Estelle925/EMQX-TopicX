package com.emqx.topichub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 连接测试结果DTO
 * 用于封装EMQX系统连接测试的详细结果
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionTestResult {

    /**
     * 系统ID
     */
    private Long systemId;

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 连接是否成功
     */
    private Boolean success;

    /**
     * 连接状态：online-在线，offline-离线
     */
    private String status;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 测试时间
     */
    private LocalDateTime testTime;

    /**
     * 错误信息（如果连接失败）
     */
    private String errorMessage;

    /**
     * EMQX版本信息（如果连接成功）
     */
    private String version;

    /**
     * 节点信息（如果连接成功）
     */
    private String nodeInfo;

    /**
     * 创建成功的测试结果
     *
     * @param systemId     系统ID
     * @param systemName   系统名称
     * @param responseTime 响应时间
     * @param version      EMQX版本
     * @param nodeInfo     节点信息
     * @return 测试结果
     */
    public static ConnectionTestResult success(Long systemId, String systemName,
                                               Long responseTime, String version, String nodeInfo) {
        ConnectionTestResult result = new ConnectionTestResult();
        result.setSystemId(systemId);
        result.setSystemName(systemName);
        result.setSuccess(true);
        result.setStatus("online");
        result.setResponseTime(responseTime);
        result.setTestTime(LocalDateTime.now());
        result.setVersion(version);
        result.setNodeInfo(nodeInfo);
        return result;
    }

    /**
     * 创建失败的测试结果
     *
     * @param systemId     系统ID
     * @param systemName   系统名称
     * @param errorMessage 错误信息
     * @return 测试结果
     */
    public static ConnectionTestResult failure(Long systemId, String systemName, String errorMessage) {
        ConnectionTestResult result = new ConnectionTestResult();
        result.setSystemId(systemId);
        result.setSystemName(systemName);
        result.setSuccess(false);
        result.setStatus("offline");
        result.setTestTime(LocalDateTime.now());
        result.setErrorMessage(errorMessage);
        return result;
    }
}