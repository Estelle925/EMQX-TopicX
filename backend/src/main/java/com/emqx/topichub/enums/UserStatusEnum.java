package com.emqx.topichub.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 * @author EMQX Topic Hub Tea
 */
@Getter
public enum UserStatusEnum {


    /**
     * 用户状态枚举 active-活跃，inactive-非活跃，locked-锁定
     */
    ACTIVE("active", "活跃"),
    INACTIVE("inactive", "非活跃"),
    LOCKED("locked", "锁定");

    private final String code;
    private final String message;

    UserStatusEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
