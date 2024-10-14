package com.dix.cloud.common.exception;

import com.dix.cloud.common.response.ResponseEnum;
import lombok.Getter;

/**
 * @Author: Base
 * @Date: 2024/9/18
 **/
@Getter
public class Mall4cloudException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private Object object;
    private ResponseEnum responseEnum;

    public Mall4cloudException(String msg) {
        super(msg);
    }

    public Mall4cloudException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public Mall4cloudException(String msg, Object object) {
        super(msg);
        this.object = object;
    }

    public Mall4cloudException(ResponseEnum responseEnum) {
        super(responseEnum.getMsg());
        this.responseEnum = responseEnum;
    }

    public Mall4cloudException(ResponseEnum responseEnum, Object object) {
        super(responseEnum.getMsg());
        this.object = object;
    }
}
