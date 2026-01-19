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
import com.chestnut.member.domain.vo.MemberCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * MemberMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + MemberMonitoredCache.ID)
@RequiredArgsConstructor
public class MemberMonitoredCache implements IMonitoredCache<MemberCache> {

    public static final String ID = "Member";

    public final static String CACHE_PREFIX = "cc:member:";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.MEMBER}";
    }

    @Override
    public MemberCache getCache(String cacheKey) {
        return redisCache.getCacheObject(cacheKey, MemberCache.class);
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    public MemberCache get(Long memberId, Supplier<MemberCache> supplier) {
        return this.redisCache.getCacheObject(CACHE_PREFIX + memberId, MemberCache.class, supplier);
    }

    public void clear(Long memberId) {
        this.redisCache.deleteObject(CACHE_PREFIX + memberId);
    }
}
