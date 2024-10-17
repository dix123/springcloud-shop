package com.dix.cloud.common.database.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Base
 * @Date: 2024/10/15
 **/
@Configuration
@MapperScan("com.dix.cloud.**.mapper")
public class MybatisConfig {
}
