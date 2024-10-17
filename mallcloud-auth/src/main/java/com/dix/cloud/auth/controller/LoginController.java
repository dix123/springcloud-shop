package com.dix.cloud.auth.controller;

import com.dix.cloud.api.auth.bo.UserInfoInTokenBO;
import com.dix.cloud.api.auth.vo.TokenInfoVO;
import com.dix.cloud.api.rbac.dto.ClearUserPermissionsCacheDTO;
import com.dix.cloud.api.rbac.feign.PermissionFeignClient;
import com.dix.cloud.auth.dto.AuthenticationDTO;
import com.dix.cloud.auth.manager.TokenStore;
import com.dix.cloud.auth.service.AuthAccountService;
import com.dix.cloud.common.response.ResponseEnum;
import com.dix.cloud.common.response.ServerResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Base
 * @Date: 2024/10/17
 **/
@RestController
@Tag(name = "登录")
public class LoginController {

    @Autowired
    PermissionFeignClient permissionFeignClient;

    @Autowired
    AuthAccountService authAccountService;

    @Autowired
    TokenStore tokenStore;

    @PostMapping("/ua/login")
    @Operation(summary = "账号密码", description = "通过账号登录，需要携带用户的系统类型")
    public ServerResponseEntity<TokenInfoVO> login(@Valid @RequestBody AuthenticationDTO authenticationDTO) {
        ServerResponseEntity<UserInfoInTokenBO> userInfoInTokenResponse = authAccountService
                .getUserInfoInTokenByUsernameAndPassword(authenticationDTO.getCredentials(), authenticationDTO.getPrincipal(), authenticationDTO.getSysType());
        if (!userInfoInTokenResponse.isSuccess()) {
            return ServerResponseEntity.transform(userInfoInTokenResponse);
        }

        UserInfoInTokenBO userInfoInTokenBO = userInfoInTokenResponse.getData();
        ClearUserPermissionsCacheDTO clearUserPermissionsCacheDTO = new ClearUserPermissionsCacheDTO();
        clearUserPermissionsCacheDTO.setUserId(userInfoInTokenBO.getUserId());
        clearUserPermissionsCacheDTO.setSysType(userInfoInTokenBO.getSysType());
        ServerResponseEntity<Void> clearResponse = permissionFeignClient.clearUserPermissionCache(clearUserPermissionsCacheDTO);
        if (!clearResponse.isSuccess()) {
            ServerResponseEntity.fail(ResponseEnum.UNAUTHORIZED);
        }

        return ServerResponseEntity.success(tokenStore.storeAndGetVo(userInfoInTokenBO));
    }

}
