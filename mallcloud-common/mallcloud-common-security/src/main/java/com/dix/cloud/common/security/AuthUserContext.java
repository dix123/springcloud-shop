package com.dix.cloud.common.security;

import com.dix.cloud.api.auth.bo.UserInfoInTokenBO;
import org.apache.catalina.User;

/**
 * @Author: Base
 * @Date: 2024/10/16
 **/
public class AuthUserContext {
    private static final ThreadLocal<UserInfoInTokenBO> USER_INFO_IN_TOKEN_HANDLER = new ThreadLocal<>();

    public static UserInfoInTokenBO get() {
        return USER_INFO_IN_TOKEN_HANDLER.get();
    }

    public static void set(UserInfoInTokenBO userInfoInTokenBO) {
        USER_INFO_IN_TOKEN_HANDLER.set(userInfoInTokenBO);
    }

    public static void clean() {
        if (USER_INFO_IN_TOKEN_HANDLER.get() != null) {
            USER_INFO_IN_TOKEN_HANDLER.remove();
        }
    }
}
