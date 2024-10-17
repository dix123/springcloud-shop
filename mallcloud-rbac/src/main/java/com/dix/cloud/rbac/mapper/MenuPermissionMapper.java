package com.dix.cloud.rbac.mapper;

import com.dix.cloud.api.rbac.bo.UriPermissionBO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: Base
 * @Date: 2024/10/15
 **/
public interface MenuPermissionMapper {

    /**
     * 根据系统类型返回所有的权限
     * @param sysType
     * @return
     */
    List<String> listAllPermissionBySysType(@Param("sysType") Integer sysType);

    /**
     * 根据用户id和系统类型返回权限列表
     * @param userId
     * @param sysType
     * @return
     */
    List<String> listPermissionByUserIdAndSysType(@Param("userId") Long userId, @Param("sysType") Integer sysType);


    /**
     * 根据系统类型获取数据
     * @param sysType
     * @return
     */
    List<UriPermissionBO> listUriPermissionInfo(@Param("sysType") Integer sysType);
}
