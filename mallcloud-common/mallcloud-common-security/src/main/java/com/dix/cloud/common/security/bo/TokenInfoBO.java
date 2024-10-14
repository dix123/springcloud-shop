package com.dix.cloud.common.security.bo;

import com.dix.cloud.api.auth.bo.UserInfoInTokenBO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author: Base
 * @Date: 2024/9/19
 **/
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TokenInfoBO {

    private UserInfoInTokenBO userInfoInToken;
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
}
