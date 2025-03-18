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
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.core.AbstractContent;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.media.domain.CmsVideo;
import com.chestnut.media.service.IVideoService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VideoContent extends AbstractContent<List<CmsVideo>> {

	/**
	 * 视频类型：第三方分享
	 */
	public static final String TYPE_SHARE = "SHARE";

	private IVideoService videoService;

	@Override
	public String getContentType() {
		return VideoContentType.ID;
	}

	@Override
	protected void add0() {
		if (!this.hasExtendEntity()) {
			return;
		}
		List<CmsVideo> videoList = this.getExtendEntity();
		if (StringUtils.isNotEmpty(videoList)) {
			for (int i = 0; i < videoList.size(); i++) {
				CmsVideo video = videoList.get(i);
				video.setVideoId(IdUtils.getSnowflakeId());
				video.setContentId(this.getContentEntity().getContentId());
				video.setSiteId(this.getContentEntity().getSiteId());
				video.setSiteId(this.getSiteId());
				video.setSortFlag(i);
				video.createBy(this.getOperatorUName());
				if (!TYPE_SHARE.equals(video.getType())) {
					video.setType(FileExUtils.getExtension(video.getPath()).toUpperCase());
					this.getVideoService().progressVideoInfo(video);
				}
			}
			this.getVideoService().dao().saveBatch(videoList);
		}
	}

	@Override
	protected void save0() {
		// 链接或映射内容直接删除所有视频数据
		if (!this.hasExtendEntity()) {
			this.getVideoService().dao().remove(new LambdaQueryWrapper<CmsVideo>().eq(CmsVideo::getContentId,
					this.getContentEntity().getContentId()));
			return;
		}
		// 视频数处理
		List<CmsVideo> videoList = this.getExtendEntity();
		// 先删除视频
		List<Long> updateVideoIds = videoList.stream().map(CmsVideo::getVideoId)
				.filter(IdUtils::validate).toList();
		this.getVideoService().dao()
				.remove(new LambdaQueryWrapper<CmsVideo>()
						.eq(CmsVideo::getContentId, this.getContentEntity().getContentId())
						.notIn(!updateVideoIds.isEmpty(), CmsVideo::getVideoId, updateVideoIds));
		// 查找剩余需要修改的视频
		Map<Long, CmsVideo> oldVideoMap = this.getVideoService().dao().lambdaQuery()
				.eq(CmsVideo::getContentId, this.getContentEntity().getContentId()).list().stream()
				.collect(Collectors.toMap(CmsVideo::getVideoId, a -> a));
		// 遍历请求视频列表，修改的视频数据path变更需重新设置视频属性
		for (int i = 0; i < videoList.size(); i++) {
			CmsVideo video = videoList.get(i);
			if (IdUtils.validate(video.getVideoId())) {
				CmsVideo dbVideo = oldVideoMap.get(video.getVideoId());
				dbVideo.setTitle(video.getTitle());
				dbVideo.setDescription(video.getDescription());
				dbVideo.setRemark(video.getRemark());
				dbVideo.setSortFlag(i);
				if (!dbVideo.getPath().equals(video.getPath())) {
					dbVideo.setPath(video.getPath());
					dbVideo.setType(video.getType());
					if (!TYPE_SHARE.equals(video.getType())) {
						dbVideo.setType(FileExUtils.getExtension(dbVideo.getPath()).toUpperCase());
						this.getVideoService().progressVideoInfo(dbVideo);
					}
				}
				dbVideo.setCover(video.getCover());
				dbVideo.updateBy(this.getOperatorUName());
				this.getVideoService().dao().updateById(dbVideo);
			} else {
				video.setVideoId(IdUtils.getSnowflakeId());
				video.setContentId(this.getContentEntity().getContentId());
				video.setSiteId(this.getSiteId());
				video.setSortFlag(i);
				video.createBy(this.getOperatorUName());
				if (!TYPE_SHARE.equals(video.getType())) {
					video.setType(FileExUtils.getExtension(video.getPath()).toUpperCase());
					this.getVideoService().progressVideoInfo(video);
				}
				this.getVideoService().progressVideoInfo(video);
				this.getVideoService().dao().save(video);
			}
		}
	}

	@Override
	protected void delete0() {
		if (this.hasExtendEntity()) {
			this.getVideoService().dao().deleteByContentIdAndBackup(
					this.getContentEntity().getContentId(),
					this.getOperatorUName()
			);
		}
	}

	@Override
	public void copyTo0(CmsContent newContent, Integer copyType) {
		if (this.hasExtendEntity() && ContentCopyType.isIndependency(copyType)) {
			List<CmsVideo> videoList = this.getVideoService().getAlbumVideoList(this.getContentEntity().getContentId());
			for (CmsVideo video : videoList) {
				video.createBy(this.getOperatorUName());
				video.setVideoId(IdUtils.getSnowflakeId());
				video.setContentId(newContent.getContentId());
				this.getVideoService().dao().save(video);
			}
		}
    }

	private IVideoService getVideoService() {
		if (this.videoService == null) {
			this.videoService = SpringUtils.getBean(IVideoService.class);
		}
		return this.videoService;
	}
}
