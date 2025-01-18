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
package com.chestnut.xmodel.cache;

import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.xmodel.core.MetaModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * XModelMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + XModelMonitoredCache.ID)
@RequiredArgsConstructor
public class XModelMonitoredCache implements IMonitoredCache<MetaModel> {

    public static final String ID = "XModel";

    public final static String CACHE_PREFIX = "xmodel:";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.XMODEL}";
    }

    @Override
    public MetaModel getCache(String cacheKey) {
        return redisCache.getCacheObject(cacheKey, MetaModel.class);
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    public MetaModel get(Long modelId, Supplier<MetaModel> supplier) {
        return this.redisCache.getCacheObject(CACHE_PREFIX + modelId, MetaModel.class, supplier);
    }

    public void clear(Long modelId) {
        this.redisCache.deleteObject(CACHE_PREFIX + modelId);
    }
}
