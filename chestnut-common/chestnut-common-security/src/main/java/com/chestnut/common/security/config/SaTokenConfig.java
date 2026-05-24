/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.common.security.config;

import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.config.properties.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SaTokenConfig implements WebMvcConfigurer {

    private final SecurityProperties securityProperties;

    public SaTokenConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        SecurityUtils.setSecurityProperties(securityProperties);
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        if (!securityProperties.getCors().hasAllowedOrigin()) {
            // 空白名单：关闭凭证反射，回退到同源策略
            config.setAllowCredentials(false);
        } else {
            config.setAllowCredentials(securityProperties.getCors().isAllowCredentials());
            // 设置访问源地址
            securityProperties.getCors().getAllowedOrigins().forEach(config::addAllowedOrigin);
            securityProperties.getCors().getAllowedOriginsPattern().forEach(config::addAllowedOriginPattern);
        }
        // 设置访问源请求头，默认：*
        securityProperties.getCors().getAllowedHeaders().forEach(config::addAllowedHeader);
        // 设置访问源请求方法
        config.setAllowedMethods(securityProperties.getCors().getAllowedMethods());
        // 有效期，默认：1800秒
        config.setMaxAge(securityProperties.getCors().getMaxAge());
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // 返回新的CorsFilter
        return new CorsFilter(source);
    }
}