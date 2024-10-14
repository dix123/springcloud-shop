package com.dix.cloud.common.cache.bo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *通过cacheName配置和ttl告诉缓存多久清除一次
 *
 * @Author: Base
 * @Date: 2024/9/18
 **/
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CacheNameWithTtlBO {
    private String cacheName;
    private Integer ttl;
}
