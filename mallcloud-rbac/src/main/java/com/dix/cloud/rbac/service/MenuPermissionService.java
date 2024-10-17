package com.dix.cloud.rbac.service;

import com.dix.cloud.api.rbac.bo.UriPermissionBO;

import java.util.List;

/**
 * @Author: Base
 * @Date: 2024/10/15
 **/
public interface MenuPermissionService {

    /**
     * 根据用户id和所在的系统返回用户权限
     * @param userId
     * @param sysType
     * @param isAdmin
     * @return
     */
    List<String> listUserPermission(Long userId, Integer sysType, boolean isAdmin);

    /**
     * 根据系统类型返回全部的权限列表
     * @param sysType
     * @return
     */
    List<UriPermissionBO> listUriPermissionInfo(Integer sysType);

    void clearUserPermissionCache(Long userId, Integer sysType);
}
