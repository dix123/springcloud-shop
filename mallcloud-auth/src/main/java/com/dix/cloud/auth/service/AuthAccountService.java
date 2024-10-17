package com.dix.cloud.auth.service;

import com.dix.cloud.api.auth.bo.UserInfoInTokenBO;
import com.dix.cloud.common.response.ServerResponseEntity;

/**
 * @Author: Base
 * @Date: 2024/10/16
 **/
public interface AuthAccountService {

    /**
     * 验证用户名和密码获取用户信息
     * @param username
     * @param password
     * @param sysType
     * @return
     */
    ServerResponseEntity<UserInfoInTokenBO> getUserInfoInTokenByUsernameAndPassword(String username, String password, Integer sysType);

}
