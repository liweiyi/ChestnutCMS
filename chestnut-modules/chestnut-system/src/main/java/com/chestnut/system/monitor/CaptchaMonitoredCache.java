/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.system.monitor;

import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.system.SysConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * CaptchaMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + CaptchaMonitoredCache.ID)
@RequiredArgsConstructor
public class CaptchaMonitoredCache implements IMonitoredCache<String> {

    public static final String ID = "Captcha";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.CAPTCHA}";
    }

    @Override
    public String getCacheKey() {
        return SysConstants.CAPTCHA_CODE_KEY;
    }

    @Override
    public String getCache(String cacheKey) {
        return redisCache.getCacheObject(cacheKey, String.class);
    }

    public void deleteCache(String cacheKey) {
        this.redisCache.deleteObject(cacheKey);
    }

    public void setCache(String cacheKey, String code, Integer captchaExpiration, TimeUnit timeUnit) {
        this.redisCache.setCacheObject(cacheKey, code, captchaExpiration, timeUnit);
    }
}
