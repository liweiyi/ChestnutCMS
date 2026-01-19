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
package com.chestnut.common.captcha.config;

import com.chestnut.common.captcha.ICaptchaStorage;
import com.chestnut.common.captcha.config.properties.CaptchaProperties;
import com.chestnut.common.captcha.storage.RedisCaptchaStorage;
import com.chestnut.common.redis.RedisCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CaptchaConfig
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Configuration
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ICaptchaStorage captchaStorage(RedisCache redisCache) {
        return new RedisCaptchaStorage(redisCache);
    }
}
