package com.emqx.topichub.common;

/**
 * 异常工具类
 * 提供便捷的异常抛出方法
 *
 * @author EMQX Topic Hub Team
 * @since 1.0.0
 */
public class ExceptionUtils {

    /**
     * 抛出业务异常
     *
     * @param message 错误信息
     */
    public static void throwBusiness(String message) {
        throw new BusinessException(message);
    }

    /**
     * 抛出业务异常
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public static void throwBusiness(Integer code, String message) {
        throw new BusinessException(code, message);
    }

    /**
     * 条件为真时抛出业务异常
     *
     * @param condition 条件
     * @param message   错误信息
     */
    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BusinessException(message);
        }
    }

    /**
     * 条件为真时抛出业务异常
     *
     * @param condition 条件
     * @param code      错误码
     * @param message   错误信息
     */
    public static void throwIf(boolean condition, Integer code, String message) {
        if (condition) {
            throw new BusinessException(code, message);
        }
    }

    /**
     * 对象为空时抛出业务异常
     *
     * @param obj     对象
     * @param message 错误信息
     */
    public static void throwIfNull(Object obj, String message) {
        if (obj == null) {
            throw new BusinessException(message);
        }
    }

    /**
     * 对象为空时抛出业务异常
     *
     * @param obj     对象
     * @param code    错误码
     * @param message 错误信息
     */
    public static void throwIfNull(Object obj, Integer code, String message) {
        if (obj == null) {
            throw new BusinessException(code, message);
        }
    }

    /**
     * 字符串为空时抛出业务异常
     *
     * @param str     字符串
     * @param message 错误信息
     */
    public static void throwIfEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(message);
        }
    }

    /**
     * 字符串为空时抛出业务异常
     *
     * @param str     字符串
     * @param code    错误码
     * @param message 错误信息
     */
    public static void throwIfEmpty(String str, Integer code, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(code, message);
        }
    }
}