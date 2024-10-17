package com.dix.cloud.api.auth.feign;

import com.dix.cloud.api.auth.bo.UserInfoInTokenBO;
import com.dix.cloud.common.constant.Auth;
import com.dix.cloud.common.response.ServerResponseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: Base
 * @Date: 2024/10/15
 **/
@FeignClient(value = "mall4cloud-auth", contextId = "token")
public interface TokenFeignClient {

    @GetMapping(value = Auth.CHECK_TOKEN_URI)
    ServerResponseEntity<UserInfoInTokenBO> checkToken(@RequestParam("accessToken") String accessToken);

}
