package com.dix.cloud.common.security.filter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.dix.cloud.api.auth.bo.UserInfoInTokenBO;
import com.dix.cloud.api.auth.constant.SysTypeEnum;
import com.dix.cloud.api.auth.feign.TokenFeignClient;
import com.dix.cloud.api.rbac.constant.HttpMethodEnum;
import com.dix.cloud.api.rbac.feign.PermissionFeignClient;
import com.dix.cloud.common.constant.Auth;
import com.dix.cloud.common.feign.FeignInsideAuthConfig;
import com.dix.cloud.common.handler.HttpHandler;
import com.dix.cloud.common.response.ResponseEnum;
import com.dix.cloud.common.response.ServerResponseEntity;
import com.dix.cloud.common.security.AuthUserContext;
import com.dix.cloud.common.security.adapter.AuthConfigAdapter;
import com.dix.cloud.common.util.IpHelper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.security.Permission;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @Author: Base
 * @Date: 2024/10/14
 **/
@Component
@Slf4j
public class AuthFilter implements Filter {
    
    @Autowired
    private HttpHandler handler;

    @Autowired
    private FeignInsideAuthConfig feignInsideAuthConfig;

    @Autowired
    private AuthConfigAdapter authConfigAdapter;

    @Autowired
    private TokenFeignClient tokenFeignClient;

    @Autowired
    private PermissionFeignClient permissionFeignClient;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 1、检查feign请求，根据配置类里的key
        if (!feignRequestCheck(request)) {
            handler.printServerResponseToWeb(ServerResponseEntity.fail(ResponseEnum.UNAUTHORIZED));
            return;
        }
        // 2、不需要检查的直接返回，根据适配类设置的
        if (Auth.CHECK_TOKEN_URI.equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        List<String> excludePatterns = authConfigAdapter.excludePathPatterns();
        if (CollectionUtil.isNotEmpty(excludePatterns)) {
            for (String path : excludePatterns) {
                AntPathMatcher matcher = new AntPathMatcher();
                if (matcher.match(path, request.getRequestURI())) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }
        // 3、验证token
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            handler.printServerResponseToWeb(ServerResponseEntity.fail(ResponseEnum.UNAUTHORIZED));
            return;
        }
        ServerResponseEntity<UserInfoInTokenBO> userInfoInTokenBOServerResponseEntity = tokenFeignClient.checkToken(token);
        if (!userInfoInTokenBOServerResponseEntity.isSuccess()) {
            handler.printServerResponseToWeb(ServerResponseEntity.fail(ResponseEnum.UNAUTHORIZED));
            return;
        }
        // 4、验证权限
        if (!checkRbac(userInfoInTokenBOServerResponseEntity.getData(), request.getRequestURI(), request.getMethod())) {
            handler.printServerResponseToWeb(ServerResponseEntity.fail(ResponseEnum.UNAUTHORIZED));
            return;
        }
        // 5、保存上下文
        try {
            AuthUserContext.set(userInfoInTokenBOServerResponseEntity.getData());
            filterChain.doFilter(request, response);
        } finally {
            AuthUserContext.clean();
        }

    }

    private boolean feignRequestCheck(HttpServletRequest request) {
        if (!request.getRequestURI().startsWith(FeignInsideAuthConfig.FEIGN_INSIDE_URL_PREFIX)) {
            return true;
        }
        String secret = request.getHeader(feignInsideAuthConfig.getKey());
        if (secret == null || !Objects.equals(secret,feignInsideAuthConfig.getSecret())) {
            return false;
        }
        List<String> ips = feignInsideAuthConfig.getIps();
        if (CollectionUtil.isNotEmpty(ips) && !ips.contains(IpHelper.getIpAddr())) {
            log.error("ip not in white list :{}, ip, {}", ips, IpHelper.getIpAddr());
            return false;
        }
        return true;
    }

    public boolean checkRbac(UserInfoInTokenBO userInfoInToken, String uri, String method) {
        if (Objects.equals(SysTypeEnum.MULTISHOP.value(), userInfoInToken.getSysType()) || Objects.equals(SysTypeEnum.PLATFORM.value(), userInfoInToken.getSysType())) {
            return true;
        }
        ServerResponseEntity<Boolean> serverResponse = permissionFeignClient.checkPermission(userInfoInToken.getUserId(), userInfoInToken.getSysType(), uri, userInfoInToken.getIsAdmin(), HttpMethodEnum.valueOf(method.toUpperCase()).value());
        if (!serverResponse.isSuccess()) {
            return false;
        }
        return serverResponse.getData();
    }
}
