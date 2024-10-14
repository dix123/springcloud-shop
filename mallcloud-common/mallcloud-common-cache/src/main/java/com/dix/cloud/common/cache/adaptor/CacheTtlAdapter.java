package com.dix.cloud.common.cache.adaptor;

import com.dix.cloud.common.cache.bo.CacheNameWithTtlBO;

import java.util.List;

/**
 * @Author: Base
 * @Date: 2024/9/18
 **/
public interface CacheTtlAdapter {

    /**
     *根据cacheName和Ttl对缓存进行过期
     * @return
     */
    List<CacheNameWithTtlBO> listCacheNameWithTtl();

}
