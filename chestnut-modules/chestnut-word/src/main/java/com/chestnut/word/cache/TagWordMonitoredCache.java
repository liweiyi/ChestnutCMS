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
import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.domain.TagWord;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TagWordMonitoredCache
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMonitoredCache.BEAN_PREFIX + TagWordMonitoredCache.ID)
@RequiredArgsConstructor
public class TagWordMonitoredCache implements IMonitoredCache<Map<String, TagWordMonitoredCache.TagWordCache>> {

    public static final String ID = "TagWord";

    private static final String CACHE_PREFIX = "cc:tagword:";

    private final RedisCache redisCache;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getCacheName() {
        return "{MONITORED.CACHE.TAG_WORD}";
    }

    @Override
    public Map<String, TagWordCache> getCache(String cacheKey) {
        return this.redisCache.getCacheMap(cacheKey, TagWordCache.class);
    }

    @Override
    public String getCacheKey() {
        return CACHE_PREFIX;
    }

    private static String cacheKey(String owner, String groupCode) {
        return CACHE_PREFIX + owner + ":" + groupCode;
    }

    public Map<String, TagWordCache> getTagWords(String owner, String groupCode) {
        return this.redisCache.getCacheMap(cacheKey(owner, groupCode), TagWordCache.class);
    }

    public void setTagWords(String owner, String groupCode, List<TagWordCache> tagWords) {
        Map<String, TagWordCache> collect = tagWords.stream()
                .collect(Collectors.toMap(w -> w.getId().toString(), w -> w));
        this.redisCache.setCacheMap(cacheKey(owner, groupCode), collect);
    }

    public void addCache(String owner, String groupCode, List<TagWord> tagWords) {
        Map<String, TagWordCache> collect = tagWords.stream().map(TagWordCache::new)
                .collect(Collectors.toMap(w -> w.getId().toString(), w -> w));
        this.redisCache.setCacheMapValues(cacheKey(owner, groupCode), collect);
    }

    public void removeCache(String owner, String groupCode, Long... tagIds) {
        if (StringUtils.isEmpty(tagIds)) {
            this.redisCache.deleteObject(cacheKey(owner, groupCode));
            return;
        }
        this.redisCache.deleteCacheMapValue(cacheKey(owner, groupCode), tagIds);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TagWordCache {
        private Long id;
        private String word;
        private String logo;
        private Long sort;
        private String remark;
        public TagWordCache(TagWord tagWord) {
            this.id = tagWord.getWordId();
            this.word = tagWord.getWord();
            this.logo = tagWord.getLogo();
            this.sort = tagWord.getSortFlag();
            this.remark = tagWord.getRemark();
        }
    }
}
