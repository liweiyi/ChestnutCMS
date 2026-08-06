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
import com.chestnut.message.domain.CcMessageEvent;
import com.chestnut.message.mapper.CcMessageEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * MessageEventMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + MessageEventMonitoredCache.ID)
@RequiredArgsConstructor
public class MessageEventMonitoredCache implements IMonitoredCache<CcMessageEvent> {

    public static final String ID = "MessageEvent";

    public static final String CACHE_PREFIX = "cc:message:event:";

    private final RedisCache redisCache;

    private final CcMessageEventMapper messageEventMapper;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.MESSAGE_EVENT}";
    }

    @Override
    public CcMessageEvent getCache(String cacheKey) {
        return redisCache.getCacheObject(cacheKey, CcMessageEvent.class);
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    public CcMessageEvent get(Long templateId) {
        return redisCache.getCacheObject(this.getCacheKey() + templateId.toString(), CcMessageEvent.class, () -> {
            return messageEventMapper.selectById(templateId);
        });
    }

    public void deleteCache(Long eventId) {
        redisCache.deleteObject(this.getCacheKey() + eventId.toString());
    }
}
