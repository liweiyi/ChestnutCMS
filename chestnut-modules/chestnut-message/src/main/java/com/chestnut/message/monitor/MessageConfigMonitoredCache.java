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
package com.chestnut.message.monitor;

import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.mapper.CcMessageConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * MessageConfigMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + MessageConfigMonitoredCache.ID)
@RequiredArgsConstructor
public class MessageConfigMonitoredCache implements IMonitoredCache<CcMessageConfig> {

    public static final String ID = "MessageConfig";

    public static final String CACHE_PREFIX = "cc:message:config:";

    private final RedisCache redisCache;

    private final CcMessageConfigMapper messageConfigMapper;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.MESSAGE_CONFIG}";
    }

    @Override
    public CcMessageConfig getCache(String cacheKey) {
        return redisCache.getCacheObject(cacheKey, CcMessageConfig.class);
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    public CcMessageConfig get(Long configId) {
        return redisCache.getCacheObject(this.getCacheKey() + configId.toString(), CcMessageConfig.class, () -> {
            return messageConfigMapper.selectById(configId);
        });
    }

    public void deleteCache(Long configId) {
        redisCache.deleteObject(this.getCacheKey() + configId.toString());
    }
}
