package com.dix.cloud.common.cache.constant;

/**
 * @Author: Base
 * @Date: 2024/9/19
 **/
public interface OauthCacheNames {
    /**
     * Oauth授权相关key
     */
    String OAUTH_PREFIX = "mallcloud_oauth:";

    /**
     * Token相关key
     */
    String OAUTH_TOKEN_PREFIX = OAUTH_PREFIX + "token:";
    /**
     *获取保存的用户信息使用key
     */
    String ACCESS = OAUTH_TOKEN_PREFIX + "access:";
    /**
     *  刷新token使用的key
     */
    String REFRESH_TO_ACCESS = OAUTH_TOKEN_PREFIX + "refresh_to_access:";
    /**
     *  uid获取保存token的key
     */
    String UID_TO_ACCESS = OAUTH_TOKEN_PREFIX + "uid+to_access:";
}
