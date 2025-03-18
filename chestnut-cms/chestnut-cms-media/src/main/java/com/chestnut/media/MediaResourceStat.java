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
package com.chestnut.media;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.core.IResourceStat;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.dao.CmsAudioDAO;
import com.chestnut.media.dao.CmsVideoDAO;
import com.chestnut.media.domain.CmsAudio;
import com.chestnut.media.domain.CmsVideo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 音视频内容资源引用统计
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IResourceStat.BEAN_PREFIX + MediaResourceStat.TYPE)
public class MediaResourceStat implements IResourceStat {

    public static final String TYPE = "Media";

    private final CmsVideoDAO videoDao;

    private final CmsAudioDAO audioDAO;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void statQuotedResource(Long siteId, Map<Long, Long> quotedResources) throws InterruptedException {
        statAudioResource(siteId, quotedResources);
        statVideoResource(siteId, quotedResources);
    }

    private void statAudioResource(Long siteId, Map<Long, Long> quotedResources) throws InterruptedException {
        int pageSize = 1000;
        long lastId = 0L;
        int count = 0;
        long total = audioDAO.lambdaQuery().eq(CmsAudio::getSiteId, siteId).count();
        while (true) {
            LambdaQueryWrapper<CmsAudio> q = new LambdaQueryWrapper<CmsAudio>()
                    .select(List.of(CmsAudio::getPath))
                    .eq(CmsAudio::getSiteId, siteId)
                    .gt(CmsAudio::getAudioId, lastId)
                    .orderByAsc(CmsAudio::getAudioId);
            Page<CmsAudio> page = audioDAO.page(new Page<>(0, pageSize, false), q);
            for (CmsAudio audio : page.getRecords()) {
                AsyncTaskManager.setTaskProgressInfo((int) (count * 100 / total),
                        "正在统计音频资源引用：" + count + " / " + total + "]");
                lastId = audio.getAudioId();
                // 音频
                InternalURL internalURL = InternalUrlUtils.parseInternalUrl(audio.getPath());
                if (Objects.nonNull(internalURL)) {
                    quotedResources.compute(internalURL.getId(), (k, v) -> Objects.isNull(v) ? 1 : v + 1);
                }
                AsyncTaskManager.checkInterrupt();
                count++;
            }
            if (page.getRecords().size() < pageSize) {
                break;
            }
        }
    }

    private void statVideoResource(Long siteId, Map<Long, Long> quotedResources) throws InterruptedException {
        int pageSize = 1000;
        long lastId = 0L;
        int count = 0;
        long total = videoDao.lambdaQuery().eq(CmsVideo::getSiteId, siteId).count();
        while (true) {
            LambdaQueryWrapper<CmsVideo> q = new LambdaQueryWrapper<CmsVideo>()
                    .select(List.of(CmsVideo::getPath, CmsVideo::getCover))
                    .eq(CmsVideo::getSiteId, siteId)
                    .gt(CmsVideo::getVideoId, lastId)
                    .orderByAsc(CmsVideo::getVideoId);
            Page<CmsVideo> page = videoDao.page(new Page<>(0, pageSize, false), q);
            for (CmsVideo video : page.getRecords()) {
                AsyncTaskManager.setTaskProgressInfo((int) (count * 100 / total),
                        "正在统计视频资源引用：" + count + " / " + total + "]");
                lastId = video.getVideoId();
                // 视频封面
                InternalURL internalURL = InternalUrlUtils.parseInternalUrl(video.getCover());
                if (Objects.nonNull(internalURL)) {
                    quotedResources.compute(internalURL.getId(), (k, v) -> Objects.isNull(v) ? 1 : v + 1);
                }
                // 视频
                if (!VideoContent.TYPE_SHARE.equals(video.getType())) {
                    internalURL = InternalUrlUtils.parseInternalUrl(video.getPath());
                    if (Objects.nonNull(internalURL)) {
                        quotedResources.compute(internalURL.getId(), (k, v) -> Objects.isNull(v) ? 1 : v + 1);
                    }
                }
                AsyncTaskManager.checkInterrupt();
                count++;
            }
            if (page.getRecords().size() < pageSize) {
                break;
            }
        }
    }
}
