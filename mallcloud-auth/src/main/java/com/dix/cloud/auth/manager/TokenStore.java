package com.dix.cloud.auth.manager;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.dix.cloud.api.auth.bo.UserInfoInTokenBO;
import com.dix.cloud.api.auth.constant.SysTypeEnum;
import com.dix.cloud.common.cache.constant.CacheNames;
import com.dix.cloud.common.security.bo.TokenInfoBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Base
 * @Date: 2024/9/19
 **/
@Component
@RefreshScope
@Slf4j
public class TokenStore {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate redisTemplate;

    @Autowired
    public TokenStore(StringRedisTemplate stringRedisTemplate, RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public TokenInfoBO storeAccessToken(UserInfoInTokenBO userInfoInToken) {
        TokenInfoBO tokenInfoBO = new TokenInfoBO();
        tokenInfoBO.setUserInfoInToken(userInfoInToken);
        tokenInfoBO.setExpiresIn(getExpiresIn(userInfoInToken.getSysType()));

        String accessToken = IdUtil.simpleUUID().toString();
        String refreshToken = IdUtil.simpleUUID().toString();

        String uidToAccessKeyStr = getUidToAccessKey(getApprovalKey(userInfoInToken));
        String accessKeyStr = getAccessKey(accessToken);
        String refreshToAccessKeyStr = getRefreshToAccesskey(refreshToken);

        List<String> existsAccessTokens = new ArrayList<>();
        existsAccessTokens.add(accessToken + StrUtil.COLON + refreshToken);

        Long size = redisTemplate.opsForSet().size(uidToAccessKeyStr);
        if (size!=null && size != 0) {
            List<String> accessWithRefreshTokenList = redisTemplate.opsForSet().pop(uidToAccessKeyStr, size);
            if (accessWithRefreshTokenList != null) {
                for (String accessWithRefreshToken : accessWithRefreshTokenList) {
                    String accessTokenData = accessWithRefreshToken.split(":")[0];
                    if (redisTemplate.hasKey(getAccessKey(accessTokenData))) {
                        existsAccessTokens.add(accessWithRefreshToken);
                    }
                }
            }
        }
        redisTemplate.executePipelined(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                Integer expiresIn = tokenInfoBO.getExpiresIn();
                for (String existsAccessToken : existsAccessTokens) {
                    operations.opsForSet().add(uidToAccessKeyStr, existsAccessToken);
                }
                operations.expire(uidToAccessKeyStr, expiresIn, TimeUnit.SECONDS);
                operations.opsForValue().set(accessKeyStr, userInfoInToken, expiresIn, TimeUnit.SECONDS);
                operations.opsForValue().set(refreshToAccessKeyStr, accessToken, expiresIn * 2, TimeUnit.SECONDS);
                return null;
            }
        });
        tokenInfoBO.setAccessToken(encryptToken(accessToken, userInfoInToken.getSysType()));
        tokenInfoBO.setRefreshToken(encryptToken(refreshToken, userInfoInToken.getSysType()));
        return tokenInfoBO;
    }

    private String encryptToken(String accessToken, Integer sysType) {
        return Base64.encode(accessToken + System.currentTimeMillis() + sysType);
    }

    private String getRefreshToAccesskey(String refreshToken) {
        return CacheNames.REFRESH_TO_ACCESS + refreshToken;
    }

    private String getAccessKey(String accessToken) {
        return CacheNames.ACCESS + accessToken;
    }

    private String getUidToAccessKey(String approvalKey) {
        return CacheNames.UID_TO_ACCESS + approvalKey;
    }

    private String getApprovalKey(UserInfoInTokenBO userInfoInToken) {
        return getApprovalKey(userInfoInToken.getSysType().toString(), userInfoInToken.getUid());
    }

    private String getApprovalKey(String appId, Long uid) {
        return uid == null ? appId : appId + StrUtil.COLON + uid;
    }

    private Integer getExpiresIn(int sysType) {
        int expiresIn = 60;
        //普通用户token过期时间1小时
        if (ObjectUtil.equals(sysType, SysTypeEnum.ORDINARY.value())) {
            expiresIn = expiresIn * 60;
        }
        //管理员用户token过期时间2小时
        if (ObjectUtil.equal(sysType, SysTypeEnum.MULTISHOP.value()) || ObjectUtil.equal(sysType, SysTypeEnum.PLATFORM.value())) {
            expiresIn = expiresIn * 60 * 2;
        }
        return expiresIn;
    }
}
