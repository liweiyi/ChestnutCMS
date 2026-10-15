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
package com.chestnut.common.captcha.storage;

import com.chestnut.common.captcha.ICaptchaStorage;
import com.chestnut.common.redis.RedisCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * RedisCaptchaStorage
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
public class RedisCaptchaStorage implements ICaptchaStorage {

    private static final DefaultRedisScript<String> CONSUME_SCRIPT = new DefaultRedisScript<>("""
            local value = redis.call('get', KEYS[1])
            if value then
                redis.call('del', KEYS[1])
            end
            return value
            """, String.class);

    private final RedisCache redisCache;

    @Override
    public String consume(String key) {
        return redisCache.getRedisTemplate().execute(CONSUME_SCRIPT, List.of(key));
    }

    @Override
    public void set(String key, String value, long expireSeconds) {
        redisCache.setCacheObject(key, value, expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void delete(String key) {
        redisCache.deleteObject(key);
    }

    @Override
    public long getExpires(String key, TimeUnit timeUnit) {
        return this.redisCache.getExpire(key, timeUnit);
    }
}
