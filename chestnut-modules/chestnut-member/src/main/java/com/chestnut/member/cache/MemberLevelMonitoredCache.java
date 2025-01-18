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
package com.chestnut.member.cache;

import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.member.domain.MemberLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * MemberLevelMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + MemberLevelMonitoredCache.ID)
@RequiredArgsConstructor
public class MemberLevelMonitoredCache implements IMonitoredCache<Map<String, MemberLevel>> {

    public static final String ID = "MemberLevel";

    public final static String CACHE_PREFIX = "cc:member:level:";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.MEMBER_LEVEL}";
    }

    @Override
    public Map<String, MemberLevel> getCache(String cacheKey) {
        return redisCache.getCacheMap(cacheKey, MemberLevel.class);
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    public MemberLevel get(Long memberId, String levelType, Supplier<MemberLevel> supplier) {
        String cacheKey = CACHE_PREFIX + memberId;
        if (!this.redisCache.hasMapKey(cacheKey, levelType)) {
            MemberLevel memberLevel = supplier.get();
            if (Objects.nonNull(memberLevel)) {
                this.redisCache.setCacheMapValue(cacheKey, levelType, memberLevel);
                return memberLevel;
            }
        }
        return this.redisCache.getCacheMapValue(cacheKey, levelType);
    }

    public Map<String, MemberLevel> get(Long memberId, Supplier<Map<String, MemberLevel>> supplier) {
        return this.redisCache.getCacheMap(CACHE_PREFIX + memberId, MemberLevel.class, supplier);
    }
}
