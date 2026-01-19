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
package com.chestnut.word.cache;

import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.word.domain.TagWord;
import com.chestnut.word.domain.TagWordGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * TagWordGroupMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + TagWordGroupMonitoredCache.ID)
@RequiredArgsConstructor
public class TagWordGroupMonitoredCache implements IMonitoredCache<TagWordGroup> {

    public static final String ID = "TagWordGroup";

    private static final String CACHE_PREFIX = "cc:tagwordgroup:";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.TAG_WORD_GROUP}";
    }

    @Override
    public TagWordGroup getCache(String cacheKey) {
        return this.redisCache.getCacheObject(cacheKey, TagWordGroup.class);
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    public static String cacheKey(String owner, String groupCode) {
        return CACHE_PREFIX + owner + ":" + groupCode;
    }

    public TagWordGroup getCacheValue(String owner, String groupCode, Supplier<TagWordGroup> supplier) {
        return this.redisCache.getCacheObject(cacheKey(owner, groupCode), TagWordGroup.class, supplier);
    }

    public void setCacheValue(String owner, String groupCode, TagWordGroup tagWordGroup) {
        this.redisCache.setCacheObject(cacheKey(owner, groupCode), tagWordGroup);
    }

    public void removeCache(TagWordGroup tagWordGroup) {
        this.redisCache.deleteObject(cacheKey(tagWordGroup.getOwner(), tagWordGroup.getCode()));
    }

    public void removeCache(String owner, String groupCode) {
        this.redisCache.deleteObject(cacheKey(owner, groupCode));
    }

    @Getter
    @Setter
    public static class TagWordCache {
        private String word;
        private String logo;
        private Long sort;
        private String remark;
        public TagWordCache(TagWord tagWord) {
            this.word = tagWord.getWord();
            this.logo = tagWord.getLogo();
            this.sort = tagWord.getSortFlag();
            this.remark = tagWord.getRemark();
        }
    }
}
