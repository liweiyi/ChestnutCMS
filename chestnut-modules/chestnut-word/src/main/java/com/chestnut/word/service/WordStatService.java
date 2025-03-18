/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.word.service;

import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.word.domain.HotWord;
import com.chestnut.word.domain.TagWord;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 词汇统计数据服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WordStatService {

    /**
     * 热词点击次数缓存KEY
     */
    private static final String HOT_WORD_CLICK_CACHE = "word:hotword:click";

    /**
     * TAG词点击次数缓存KEY
     */
    private static final String TAG_WORD_CLICK_CACHE = "word:tagword:click";

    private final IHotWordService hotWordService;

    private final ITagWordService tagWordService;

    private final RedisCache redisCache;

    private static final ConcurrentHashMap<Long, Long> hotWordClickUpdates = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Long, Long> tagWordClickUpdates = new ConcurrentHashMap<>();

    /**
     * 更新热词点击数据缓存
     *
     * @param wordId
     */
    public void updateHotWordClick(Long wordId) {
        if (!IdUtils.validate(wordId)) {
            return;
        }
        long clickCount = this.redisCache.getZsetScore(HOT_WORD_CLICK_CACHE, wordId.toString()).longValue();
        if (clickCount < 0) {
            HotWord word = hotWordService.getById(wordId);
            if (Objects.isNull(word)) {
                return;
            }
            clickCount = word.getHitCount() + 1;
            this.redisCache.addZset(HOT_WORD_CLICK_CACHE, wordId.toString(), clickCount);
        } else {
            clickCount++;
            this.redisCache.zsetIncr(HOT_WORD_CLICK_CACHE, wordId.toString(), 1);
        }
        hotWordClickUpdates.put(wordId, clickCount);
    }

    /**
     * 更新TAG词点击数据缓存
     *
     * @param wordId
     */
    public void updateTagWordClick(Long wordId) {
        if (!IdUtils.validate(wordId)) {
            return;
        }
        long clickCount = this.redisCache.getZsetScore(TAG_WORD_CLICK_CACHE, wordId.toString()).longValue();
        if (clickCount < 0) {
            TagWord word = tagWordService.getById(wordId);
            if (Objects.isNull(word)) {
                return;
            }
            clickCount = word.getHitCount() + 1;
            this.redisCache.addZset(TAG_WORD_CLICK_CACHE, wordId.toString(), clickCount);
        } else {
            clickCount++;
            this.redisCache.zsetIncr(TAG_WORD_CLICK_CACHE, wordId.toString(), 1);
        }
        tagWordClickUpdates.put(wordId, clickCount);
    }

    public void saveHotWordHitCountToDB() {
        hotWordClickUpdates.keys().asIterator().forEachRemaining(wordId -> {
            Long clickCount = hotWordClickUpdates.remove(wordId);
            if (clickCount > 0) {
                this.hotWordService.lambdaUpdate()
                        .set(HotWord::getHitCount, clickCount)
                        .eq(HotWord::getWordId, wordId)
                        .update();
            }
        });
    }

    public void saveTagWordHitCountToDB() {
        tagWordClickUpdates.keys().asIterator().forEachRemaining(wordId -> {
            Long clickCount = tagWordClickUpdates.remove(wordId);
            if (clickCount > 0) {
                this.tagWordService.lambdaUpdate()
                        .set(TagWord::getHitCount, clickCount)
                        .eq(TagWord::getWordId, wordId)
                        .update();
            }
        });
    }

    @PreDestroy
    public void preDestroy() {
        log.info("Update hot/tag word click count to database.");
        this.saveHotWordHitCountToDB();
        this.saveTagWordHitCountToDB();
    }
}
