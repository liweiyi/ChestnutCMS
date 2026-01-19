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
package com.chestnut.media.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.chestnut.media.domain.BCmsAudio;
import com.chestnut.media.domain.BCmsVideo;
import com.chestnut.media.domain.CmsAudio;
import com.chestnut.media.domain.CmsVideo;
import com.chestnut.media.mapper.BCmsAudioMapper;
import com.chestnut.media.mapper.BCmsVideoMapper;
import com.chestnut.media.service.IAudioService;
import com.chestnut.media.service.IVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaListener {

	private final IAudioService audioService;

	private final IVideoService videoService;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		// 删除音频内容数据
		try {
			long total = this.audioService.dao().lambdaQuery().eq(CmsAudio::getSiteId, site.getSiteId()).count();
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除音频内容数据：" + (i * pageSize) + "/" + total);
				List<Long> audioIds = this.audioService.dao().lambdaQuery()
						.select(CmsAudio::getAudioId)
						.eq(CmsAudio::getSiteId, site.getSiteId())
						.page(new Page<>(0, pageSize, false))
						.getRecords().stream().map(CmsAudio::getAudioId).toList();
				this.audioService.dao().removeBatchByIds(audioIds);
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除音频内容错误：" + e.getMessage());
			log.error("Delete cms_audio failed on site[{}] delete.", site.getSiteId(), e);
		}
		try {
			BCmsAudioMapper backupMapper = this.audioService.dao().getBackupMapper();
			long total = backupMapper.selectCount(new LambdaQueryWrapper<BCmsAudio>()
					.eq(BCmsAudio::getSiteId, site.getSiteId()));
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除音频内容备份数据：" + (i * pageSize) + "/" + total);
				List<Long> backupIds = backupMapper.selectPage(
						new Page<>(0, pageSize, false),
						new LambdaQueryWrapper<BCmsAudio>()
								.select(BCmsAudio::getBackupId)
								.eq(BCmsAudio::getSiteId, site.getSiteId())

				).getRecords().stream().map(BCmsAudio::getBackupId).toList();
				backupMapper.deleteByIds(backupIds);
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除音频内容备份错误：" + e.getMessage());
			log.error("Delete backup cms_audio failed on site[{}] delete.", site.getSiteId(), e);
		}
		// 删除视频内容数据
		try {
			long total = this.videoService.dao().lambdaQuery().eq(CmsVideo::getSiteId, site.getSiteId()).count();
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除视频内容数据：" + (i * pageSize) + "/" + total);
				List<Long> audioIds = this.videoService.dao().lambdaQuery()
						.select(CmsVideo::getVideoId)
						.eq(CmsVideo::getSiteId, site.getSiteId())
						.page(new Page<>(0, pageSize, false))
						.getRecords().stream().map(CmsVideo::getVideoId).toList();
				this.videoService.dao().removeBatchByIds(audioIds);
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除视频内容错误：" + e.getMessage());
			log.error("Delete cms_video failed on site[{}] delete.", site.getSiteId(), e);
		}
		try {
			BCmsVideoMapper backupMapper = this.videoService.dao().getBackupMapper();
			long total = backupMapper.selectCount(new LambdaQueryWrapper<BCmsVideo>()
					.eq(BCmsVideo::getSiteId, site.getSiteId()));
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除视频内容备份数据：" + (i * pageSize) + "/" + total);
				List<Long> backupIds = backupMapper.selectPage(
						new Page<>(0, pageSize, false),
						new LambdaQueryWrapper<BCmsVideo>()
								.select(BCmsVideo::getBackupId)
								.eq(BCmsVideo::getSiteId, site.getSiteId())

				).getRecords().stream().map(BCmsVideo::getBackupId).toList();
				backupMapper.deleteByIds(backupIds);
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除视频内容备份错误：" + e.getMessage());
			log.error("Delete backup cms_video failed on site[{}] delete.", site.getSiteId(), e);
		}
	}
}
