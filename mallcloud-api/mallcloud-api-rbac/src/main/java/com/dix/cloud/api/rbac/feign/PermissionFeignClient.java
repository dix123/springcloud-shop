package com.dix.cloud.api.rbac.feign;

import com.dix.cloud.api.rbac.dto.ClearUserPermissionsCacheDTO;
import com.dix.cloud.common.constant.Auth;
import com.dix.cloud.common.feign.FeignInsideAuthConfig;
import com.dix.cloud.common.response.ServerResponseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: Base
 * @Date: 2024/10/15
 **/
@FeignClient(value = "mall4cloud-rbac", contextId = "permission")
public interface PermissionFeignClient {

    @GetMapping(Auth.CHECK_RBAC_URI)
    ServerResponseEntity<Boolean> checkPermission(@RequestParam("userId") Long userId, @RequestParam("sysType") Integer sysType,
                                                  @RequestParam("uri") String uri, @RequestParam("isAdmin") Integer isAdmin,
                                                  @RequestParam("method") Integer method);

    @PostMapping(FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX + "/insider/permission/clearUserPermissionCache")
    ServerResponseEntity<Void> clearUserPermissionCache(@RequestBody ClearUserPermissionsCacheDTO clearUserPermissionsCacheDTO);
}
