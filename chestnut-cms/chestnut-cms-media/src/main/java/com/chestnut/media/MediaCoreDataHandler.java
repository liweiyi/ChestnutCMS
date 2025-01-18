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
package com.chestnut.media;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.core.ICoreDataHandler;
import com.chestnut.contentcore.core.SiteExportContext;
import com.chestnut.contentcore.core.SiteImportContext;
import com.chestnut.media.domain.CmsAudio;
import com.chestnut.media.domain.CmsVideo;
import com.chestnut.media.service.IAudioService;
import com.chestnut.media.service.IVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 音视频内容核心数据处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediaCoreDataHandler implements ICoreDataHandler {

    private final IAudioService audioService;

    private final IVideoService videoService;

    @Override
    public void onSiteExport(SiteExportContext context) {
        // cms_audio
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导出音频内容数据");
        int pageSize = 200;
        long offset = 0;
        int fileIndex = 1;
        while (true) {
            LambdaQueryWrapper<CmsAudio> q = new LambdaQueryWrapper<CmsAudio>()
                    .eq(CmsAudio::getSiteId, context.getSite().getSiteId())
                    .gt(CmsAudio::getAudioId, offset)
                    .orderByAsc(CmsAudio::getAudioId);
            Page<CmsAudio> page = audioService.dao().page(new Page<>(1, pageSize, false), q);
            if (!page.getRecords().isEmpty()) {
                context.saveData(CmsAudio.TABLE_NAME, JacksonUtils.to(page.getRecords()), fileIndex);
                if (page.getRecords().size() < pageSize) {
                    break;
                }
                offset = page.getRecords().get(page.getRecords().size() - 1).getContentId();
                fileIndex++;
            } else {
                break;
            }
        }
        // cms_video
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导出视频频内容数据");
        offset = 0;
        fileIndex = 1;
        while (true) {
            LambdaQueryWrapper<CmsVideo> q = new LambdaQueryWrapper<CmsVideo>()
                    .eq(CmsVideo::getSiteId, context.getSite().getSiteId())
                    .gt(CmsVideo::getVideoId, offset)
                    .orderByAsc(CmsVideo::getVideoId);
            Page<CmsVideo> page = videoService.dao().page(new Page<>(1, pageSize, false), q);
            if (!page.getRecords().isEmpty()) {
                context.saveData(CmsVideo.TABLE_NAME, JacksonUtils.to(page.getRecords()), fileIndex);
                if (page.getRecords().size() < pageSize) {
                    break;
                }
                offset = page.getRecords().get(page.getRecords().size() - 1).getContentId();
                fileIndex++;
            } else {
                break;
            }
        }
    }

    @Override
    public void onSiteImport(SiteImportContext context) {
        // cms_audio
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入音频内容数据");
        List<File> files = context.readDataFiles(CmsAudio.TABLE_NAME);
        files.forEach(f -> {
            List<CmsAudio> list = JacksonUtils.fromList(f, CmsAudio.class);
            for (CmsAudio data : list) {
                Long oldAudioId = data.getAudioId();
                try {
                    data.setAudioId(IdUtils.getSnowflakeId());
                    data.setSiteId(context.getSite().getSiteId());
                    data.setContentId(context.getContentIdMap().get(data.getContentId()));
                    data.createBy(context.getOperator());
                    data.setPath(context.dealInternalUrl(data.getPath()));
                    audioService.dao().save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入音频内容数据`" + oldAudioId + "`失败：" + e.getMessage());
                    log.error("Import cms_audio failed: {}", data.getAudioId(), e);
                }
            }
        });
        // cms_video
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入视频内容数据");
        files = context.readDataFiles(CmsVideo.TABLE_NAME);
        files.forEach(f -> {
            List<CmsVideo> list = JacksonUtils.fromList(f, CmsVideo.class);
            for (CmsVideo data : list) {
                Long oldVideoId = data.getVideoId();
                try {
                    data.setVideoId(IdUtils.getSnowflakeId());
                    data.setCover(context.dealInternalUrl(data.getCover()));
                    data.setSiteId(context.getSite().getSiteId());
                    data.setContentId(context.getContentIdMap().get(data.getContentId()));
                    data.createBy(context.getOperator());
                    data.setPath(context.dealInternalUrl(data.getPath()));
                    videoService.dao().save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入视频内容数据`" + oldVideoId + "`失败：" + e.getMessage());
                    log.error("Import cms_video failed: {}", data.getVideoId(), e);
                }
            }
        });
    }
}
