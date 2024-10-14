package com.dix.cloud.api.auth.bo;

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
public class UserInfoInTokenBO {
    /**
     * 用户id
     */
    private Long userId;
    private Long uid;
    private Long tenantId;
    /**
     * @see com.dix.cloud.api.auth.constant.SysTypeEnum
     */
    private Integer SysType;
    private Integer isAdmin;
    private String bizUserId;
    private String BizUid;
}
