package com.dix.cloud.auth.manager;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.dix.cloud.api.auth.bo.UserInfoInTokenBO;
import com.dix.cloud.api.auth.constant.SysTypeEnum;
import com.dix.cloud.api.auth.vo.TokenInfoVO;
import com.dix.cloud.common.cache.constant.CacheNames;
import com.dix.cloud.common.response.ServerResponseEntity;
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

    public TokenInfoVO storeAndGetVo(UserInfoInTokenBO userInfoInToken) {
        TokenInfoBO tokenInfoBO = storeAccessToken(userInfoInToken);

        TokenInfoVO tokenInfoVO = new TokenInfoVO();
        tokenInfoVO.setAccessToken(tokenInfoBO.getAccessToken());
        tokenInfoVO.setRefreshToken(tokenInfoBO.getRefreshToken());
        tokenInfoVO.setExpiresIn(tokenInfoBO.getExpiresIn());
        return tokenInfoVO;
    }

    private String encryptToken(String accessToken, Integer sysType) {
        return Base64.encode(accessToken + System.currentTimeMillis() + sysType);
    }

    public ServerResponseEntity<UserInfoInTokenBO> getUserInfoByAccessToken(String accessToken, boolean needDecrypt) {
        if (StrUtil.isBlank(accessToken)) {
            return ServerResponseEntity.showFailMsg("accessToken is blank");
        }
        String realAccessToken;
        if (needDecrypt) {
            ServerResponseEntity<String> decryptTokenEntity = decryptToken(accessToken);
            if (!decryptTokenEntity.isSuccess()) {
                return ServerResponseEntity.transform(decryptTokenEntity);
            }
            realAccessToken = decryptTokenEntity.getData();
        }
        else {
            realAccessToken = accessToken;
        }
        UserInfoInTokenBO userInfoInTokenBO = (UserInfoInTokenBO) redisTemplate.opsForValue()
                .get(getAccessKey(realAccessToken));

        if (userInfoInTokenBO == null) {
            return ServerResponseEntity.showFailMsg("accessToken 已过期");
        }
        return ServerResponseEntity.success(userInfoInTokenBO);
    }

    private ServerResponseEntity<String> decryptToken(String data) {
        String decryptStr;
        String decryptToken;
        try {
            decryptStr = Base64.decodeStr(data);
            decryptToken = decryptStr.substring(0,32);
            // 创建token的时间，token使用时效性，防止攻击者通过一堆的尝试找到aes的密码，虽然aes是目前几乎最好的加密算法
            long createTokenTime = Long.parseLong(decryptStr.substring(32,45));
            // 系统类型
            int sysType = Integer.parseInt(decryptStr.substring(45));
            // token的过期时间
            int expiresIn = getExpiresIn(sysType);
            long second = 1000L;
            if (System.currentTimeMillis() - createTokenTime > expiresIn * second) {
                return ServerResponseEntity.showFailMsg("token 格式有误");
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return ServerResponseEntity.showFailMsg("token 格式有误");
        }

        // 防止解密后的token是脚本，从而对redis进行攻击，uuid只能是数字和小写字母
        if (true) {
            return ServerResponseEntity.showFailMsg("token 格式有误");
        }
        return ServerResponseEntity.success(decryptToken);
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
