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
package com.chestnut.member.cache;

import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.member.domain.MemberExpConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * MemberExpOpMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + MemberExpOpMonitoredCache.ID)
@RequiredArgsConstructor
public class MemberExpOpMonitoredCache implements IMonitoredCache<MemberExpConfig> {

    public static final String ID = "MemberExpOp";

    public final static String CACHE_PREFIX = "cc:member:exp_op:";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.MEMBER_EXP_OP}";
    }

    @Override
    public MemberExpConfig getCache(String cacheKey) {
        return redisCache.getCacheObject(cacheKey, MemberExpConfig.class);
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    public MemberExpConfig get(String opType, String levelType, Supplier<MemberExpConfig> supplier) {
        return this.redisCache.getCacheObject(CACHE_PREFIX + opType + ":" + levelType, MemberExpConfig.class, supplier);
    }

    public void clear(MemberExpConfig memberExpConfig) {
        this.redisCache.deleteObject(CACHE_PREFIX + memberExpConfig.getOpType() + ":" + memberExpConfig.getLevelType());
    }

    public void clearAll(List<MemberExpConfig> memberExpConfigs) {
        memberExpConfigs.forEach(this::clear);
    }
}
