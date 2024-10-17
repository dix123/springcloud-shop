package com.dix.cloud.common.handler;

import cn.hutool.core.util.CharsetUtil;
import com.dix.cloud.common.exception.Mall4cloudException;
import com.dix.cloud.common.response.ServerResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Author: Base
 * @Date: 2024/10/14
 **/
@Component
@Slf4j
public class HttpHandler {
    @Autowired
    private ObjectMapper objectMapper;

    public <T> void printServerResponseToWeb(ServerResponseEntity<T> serverResponseEntity) {
        if (serverResponseEntity == null) {
            log.info("print obj is null");
            return;
        }

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes == null) {
            log.error("requestAttribute is null,can not print to web");
            return;
        }
        HttpServletResponse response = servletRequestAttributes.getResponse();
        if (response == null) {
            log.error("httpServletResponse is null,can not print to web");
            return;
        }
        response.setCharacterEncoding(CharsetUtil.UTF_8);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(serverResponseEntity));
        } catch (IOException e) {
            throw new Mall4cloudException("io 异常", e);
        }
    }
}
