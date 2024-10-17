package com.dix.cloud.rbac.feign;

import com.dix.cloud.api.rbac.bo.UriPermissionBO;
import com.dix.cloud.api.rbac.dto.ClearUserPermissionsCacheDTO;
import com.dix.cloud.api.rbac.feign.PermissionFeignClient;
import com.dix.cloud.common.response.ResponseEnum;
import com.dix.cloud.common.response.ServerResponseEntity;
import com.dix.cloud.rbac.service.MenuPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @Author: Base
 * @Date: 2024/10/15
 **/
@RestController
public class PermissionFeignController implements PermissionFeignClient {

    @Autowired
    MenuPermissionService menuPermissionService;

    @Override
    public ServerResponseEntity<Boolean> checkPermission(@RequestParam("userid") Long userId, @RequestParam("sysType") Integer sysType, @RequestParam("uri") String uri, @RequestParam("isAdmin") Integer isAdmin, @RequestParam("method") Integer method) {
        List<String> userPermissions = menuPermissionService.listUserPermission(userId, sysType, Boolean.TRUE);
        List<UriPermissionBO> uriPermissions = menuPermissionService.listUriPermissionInfo(sysType);

        AntPathMatcher matcher = new AntPathMatcher();
        for (UriPermissionBO uriPermission : uriPermissions) {
            if (matcher.match(uriPermission.getUri(), uri) && Objects.equals(uriPermission.getMethod(), method)) {
                if (userPermissions.contains(uriPermission.getPermission())) {
                    return ServerResponseEntity.success(Boolean.TRUE);
                } else {
                    return ServerResponseEntity.fail(ResponseEnum.UNAUTHORIZED);
                }
            }
        }
        return ServerResponseEntity.success(Boolean.TRUE);
    }

    @Override
    public ServerResponseEntity<Void> clearUserPermissionCache(ClearUserPermissionsCacheDTO clearUserPermissionsCacheDTO) {
        menuPermissionService.clearUserPermissionCache(clearUserPermissionsCacheDTO.getUserId(), clearUserPermissionsCacheDTO.getSysType());
        return ServerResponseEntity.success();
    }
}
