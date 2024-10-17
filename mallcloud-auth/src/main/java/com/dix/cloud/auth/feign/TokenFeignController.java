package com.dix.cloud.auth.feign;

import com.dix.cloud.api.auth.bo.UserInfoInTokenBO;
import com.dix.cloud.api.auth.feign.TokenFeignClient;
import com.dix.cloud.auth.manager.TokenStore;
import com.dix.cloud.common.response.ServerResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Base
 * @Date: 2024/10/15
 **/
@RestController
public class TokenFeignController implements TokenFeignClient {

    @Autowired
    private TokenStore tokenStore;

    @Override
    public ServerResponseEntity<UserInfoInTokenBO> checkToken(String accessToken) {
        ServerResponseEntity<UserInfoInTokenBO> userInfoByAccessTokenResponse = tokenStore.getUserInfoByAccessToken(accessToken, true);
        if (!userInfoByAccessTokenResponse.isSuccess()) {
            return ServerResponseEntity.transform(userInfoByAccessTokenResponse);
        }
        return userInfoByAccessTokenResponse;
    }
}
