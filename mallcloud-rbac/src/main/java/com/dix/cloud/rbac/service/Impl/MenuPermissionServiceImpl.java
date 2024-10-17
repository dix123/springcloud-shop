package com.dix.cloud.rbac.service.Impl;

import com.dix.cloud.api.rbac.bo.UriPermissionBO;
import com.dix.cloud.common.cache.constant.CacheNames;
import com.dix.cloud.rbac.mapper.MenuPermissionMapper;
import com.dix.cloud.rbac.service.MenuPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: Base
 * @Date: 2024/10/15
 **/
@Service
public class MenuPermissionServiceImpl implements MenuPermissionService {

    @Autowired
    private MenuPermissionMapper menuPermissionMapper;

    @Override
    public List<String> listUserPermission(Long userId, Integer sysType, boolean isAdmin) {
        List<String> permissionList = null;
        if (isAdmin) {
            permissionList =  listAllPermission(sysType);
        } else {
            permissionList =  listPermisisonByUserIdAndSysType(userId, sysType);
        }
        return permissionList;
    }


    @Override
    public List<UriPermissionBO> listUriPermissionInfo(Integer sysType) {
        return menuPermissionMapper.listUriPermissionInfo(sysType);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.USER_PERMISSIONS_KEY, key = "#sysType + ':' + #userId"),
            @CacheEvict(cacheNames = CacheNames.MENU_ID_LIST_KEY, key = "#userId")
    })
    public void clearUserPermissionCache(Long userId, Integer sysType) {

    }

    public List<String> listAllPermission(Integer sysType) {
        return menuPermissionMapper.listAllPermissionBySysType(sysType);
    }

    public List<String> listPermisisonByUserIdAndSysType(Long userid, Integer sysType) {
        return menuPermissionMapper.listPermissionByUserIdAndSysType(userid, sysType);
    }

}
