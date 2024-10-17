package com.dix.cloud.auth.mapper;

import com.dix.cloud.common.security.bo.AuthAccountInVerifyBO;
import feign.Param;

/**
 * @Author: Base
 * @Date: 2024/10/16
 **/
public interface AuthAccountMapper {

    /**
     * 根据 用户名和系统类型返回用户信息
     * @param inputUsernameType
     * @param username
     * @param sysType
     * @return
     */
    AuthAccountInVerifyBO getAuthAccountByUsername(@Param("inputUsernameType") Integer inputUsernameType,
                                                   @Param("inputUsername") String username,
                                                   @Param("sysType") Integer sysType);

}
