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
package com.chestnut.contentcore.service.impl;

import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.ContentDynamicDataVO;
import com.chestnut.contentcore.service.IContentService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内容动态数据服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentDynamicDataService {

    /**
     * 内容浏览次数缓存KEY
     */
    private static final String CONTENT_DYNAMIC_DATA_CACHE = "cms:content:dynamic";

    private final IContentService contentService;

    private final RedisCache redisCache;

    private final RedissonClient redissonClient;

    private static final ConcurrentHashMap<Long, ContentDynamicDataVO> dynamicUpdates = new ConcurrentHashMap<>();

    public void increaseFavoriteCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.Favorite, true);
    }

    public void decreaseFavoriteCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.Favorite, false);
    }

    public void increaseCommentCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.Comment, true);
    }

    public void decreaseCommentCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.Comment, false);
    }

    public void increaseLikeCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.Like, true);
    }

    public void decreaseLikeCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.Like, false);
    }

    public void increaseViewCount(Long contentId) {
        this.updateContentDynamicData(contentId, ContentDynamicDataVO.DynamicDataType.View, true);
    }

    public List<ContentDynamicDataVO> getContentDynamicDataList(List<String> contentIds) {
        if (contentIds.isEmpty()) {
            return List.of();
        }
        List<ContentDynamicDataVO> values = new ArrayList<>(contentIds.size());
        List<String> findContentIds = new ArrayList<>();
        this.redisCache.getMultiCacheMapValue(CONTENT_DYNAMIC_DATA_CACHE, contentIds).forEach(o -> {
            if (o != null) {
                ContentDynamicDataVO vo = (ContentDynamicDataVO) o;
                values.add(vo);
                findContentIds.add(vo.getContentId().toString());
            }
        });
        contentIds.forEach(contentId -> {
            if (!findContentIds.contains(contentId)) {
                CmsContent content = this.contentService.dao().getById(contentId);
                if (content != null) {
                    ContentDynamicDataVO data = new ContentDynamicDataVO(content);
                    this.redisCache.setCacheMapValue(CONTENT_DYNAMIC_DATA_CACHE, contentId, data);
                    values.add(data);
                }
            }
        });
        return values;
    }

    /**
     * 更新内容动态数据缓存
     *
     * @param contentId
     * @param type
     * @return
     */
    private void updateContentDynamicData(Long contentId, ContentDynamicDataVO.DynamicDataType type, boolean increase) {
        if (!IdUtils.validate(contentId)) {
            return;
        }
        RLock lock = redissonClient.getLock("ContentView-" + contentId);
        lock.lock();
        try {
            ContentDynamicDataVO data = this.redisCache.getCacheMapValue(CONTENT_DYNAMIC_DATA_CACHE, contentId.toString());
            if (Objects.isNull(data)) {
                CmsContent content = this.contentService.dao().getById(contentId);
                if (Objects.isNull(content)) {
                    return;
                }
                data = new ContentDynamicDataVO(content);
            }
            if (increase) {
                data.increase(type);
            } else {
                data.decrease(type);
            }

            this.redisCache.setCacheMapValue(CONTENT_DYNAMIC_DATA_CACHE, contentId.toString(), data);
            dynamicUpdates.put(contentId, data);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 每5分钟更新记录的动态数据缓存到数据库
     */
    public void saveDynamicDataToDB() {
        dynamicUpdates.keys().asIterator().forEachRemaining(contentId -> {
            ContentDynamicDataVO data = dynamicUpdates.remove(contentId);
            if (data != null) {
                this.contentService.dao().lambdaUpdate().set(CmsContent::getFavoriteCount, data.getFavorites())
                        .set(CmsContent::getLikeCount, data.getLikes())
                        .set(CmsContent::getCommentCount, data.getComments())
                        .set(CmsContent::getViewCount, data.getViews())
                        .eq(CmsContent::getContentId, data.getContentId())
                        .update();
            }
        });
    }

    @PreDestroy
    public void preDestroy() {
        log.info("Update content dynamic data to database.");
        this.saveDynamicDataToDB();
    }
}
