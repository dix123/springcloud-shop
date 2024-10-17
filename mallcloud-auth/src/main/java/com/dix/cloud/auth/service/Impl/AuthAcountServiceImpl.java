package com.dix.cloud.auth.service.Impl;

import cn.hutool.core.util.StrUtil;
import com.dix.cloud.api.auth.bo.UserInfoInTokenBO;
import com.dix.cloud.auth.constant.AuthAccountStatusEnum;
import com.dix.cloud.auth.mapper.AuthAccountMapper;
import com.dix.cloud.auth.service.AuthAccountService;
import com.dix.cloud.common.response.ServerResponseEntity;
import com.dix.cloud.common.security.bo.AuthAccountInVerifyBO;
import com.dix.cloud.common.security.constant.InputUserNameEnum;
import com.dix.cloud.common.util.BeanUtil;
import com.dix.cloud.common.util.PrincipalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Author: Base
 * @Date: 2024/10/16
 **/
@Service
public class AuthAcountServiceImpl implements AuthAccountService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthAccountMapper authAccountMapper;

    private static final String USER_NOT_FOUND_SECRET = "USER_NOT_FOUND_SECRET";

    private static String userNotFoundEncodedPassword;

    @Override
    public ServerResponseEntity<UserInfoInTokenBO> getUserInfoInTokenByUsernameAndPassword(String username, String password, Integer sysType) {
        // 1、判空
        if (StrUtil.isBlank(username)) {
            return ServerResponseEntity.showFailMsg("用户名不能为空");
        }
        if (StrUtil.isBlank(password)) {
            return ServerResponseEntity.showFailMsg("密码不能为空");
        }
        // 2、判断输入的类型
        InputUserNameEnum inputUserNameEnum = null;
        if (PrincipalUtil.isSimpleChar(password)) {
            inputUserNameEnum = InputUserNameEnum.USERNAME;
        }
        if (inputUserNameEnum == null) {
            return ServerResponseEntity.showFailMsg("请输入正确的用户名");
        }
        // 3、获取用户信息
        AuthAccountInVerifyBO authAccountInVerifyBO = authAccountMapper.getAuthAccountByUsername(inputUserNameEnum.value(), username, sysType);
        // 4、防止计时攻击
        if (authAccountInVerifyBO == null) {
            prepareTimingAttackProtection();
            mitigateAgainstTimingAttack(password);
            return ServerResponseEntity.showFailMsg("用户名或密码错误");
        }
        // 5、判断账户是否可用
        if (Objects.equals(authAccountInVerifyBO.getStatus(), AuthAccountStatusEnum.DISABLE.value())) {
            return ServerResponseEntity.showFailMsg("账户被禁用");
        }
        // 6、判断密码是否正确
        if (!passwordEncoder.matches(password, authAccountInVerifyBO.getPassword())) {
            return ServerResponseEntity.showFailMsg("用户名或密码错误");
        }
        return ServerResponseEntity.success(BeanUtil.map(authAccountInVerifyBO, UserInfoInTokenBO.class));
    }

    private void prepareTimingAttackProtection() {
        if (userNotFoundEncodedPassword == null) {
            userNotFoundEncodedPassword = passwordEncoder.encode(USER_NOT_FOUND_SECRET);
        }
    }

    private void mitigateAgainstTimingAttack(String password) {
        if (password != null) {
            passwordEncoder.matches(password, userNotFoundEncodedPassword);
        }
    }
}
