package com.dix.cloud.api.auth.constant;

/**
 * @Author: Base
 * @Date: 2024/9/19
 **/
public enum SysTypeEnum {
    /**
     * 用户端
     */
    ORDINARY(0),

    /**
     * 商家端
     */
    MULTISHOP(1),

    /**
     * 平台端
     */
    PLATFORM(2),
    ;
    private final Integer value;

    public Integer value() {
        return value;
    }

    SysTypeEnum(Integer value) {
        this.value = value;
    }
}
