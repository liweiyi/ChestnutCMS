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
import com.chestnut.media.domain.CmsAudio;
import com.chestnut.media.service.IAudioService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AudioContent extends AbstractContent<List<CmsAudio>> {

	private IAudioService audioService;

	@Override
	public String getContentType() {
		return AudioContentType.ID;
	}

	@Override
	protected void add0() {
		if (!this.hasExtendEntity()) {
			return;
		}
		List<CmsAudio> audioList = this.getExtendEntity();
		if (StringUtils.isNotEmpty(audioList)) {
			for (int i = 0; i < audioList.size(); i++) {
				CmsAudio audio = audioList.get(i);
				audio.setAudioId(IdUtils.getSnowflakeId());
				audio.setContentId(this.getContentEntity().getContentId());
				audio.setSiteId(this.getContentEntity().getSiteId());
				audio.setSiteId(this.getSiteId());
				audio.setType(FileExUtils.getExtension(audio.getPath()).toUpperCase());
				audio.setSortFlag(i);
				audio.createBy(this.getOperatorUName());
				this.getAudioService().progressAudioInfo(audio);
			}
			this.getAudioService().dao().saveBatch(audioList);
		}
	}

	@Override
	protected void save0() {
		// 链接或映射内容直接删除所有音频数据
		if (!this.hasExtendEntity()) {
			this.getAudioService().dao().remove(new LambdaQueryWrapper<CmsAudio>().eq(CmsAudio::getContentId,
					this.getContentEntity().getContentId()));
			return;
		}
		// 音频数处理
		List<CmsAudio> audioList = this.getExtendEntity();
		// 先删除音频
		List<Long> updateAudioIds = audioList.stream().map(CmsAudio::getAudioId)
				.filter(IdUtils::validate).toList();
		this.getAudioService().dao()
				.remove(new LambdaQueryWrapper<CmsAudio>()
						.eq(CmsAudio::getContentId, this.getContentEntity().getContentId())
						.notIn(!updateAudioIds.isEmpty(), CmsAudio::getAudioId, updateAudioIds));
		// 查找需要修改的音频
		Map<Long, CmsAudio> oldAudioMap = this.getAudioService().dao().lambdaQuery()
				.eq(CmsAudio::getContentId, this.getContentEntity().getContentId()).list().stream()
				.collect(Collectors.toMap(CmsAudio::getAudioId, a -> a));
		// 遍历请求音频列表，修改的音频数据path变更需重新设置音频属性
		for (int i = 0; i < audioList.size(); i++) {
			CmsAudio audio = audioList.get(i);
			if (IdUtils.validate(audio.getAudioId())) {
				CmsAudio dbAudio = oldAudioMap.get(audio.getAudioId());
				dbAudio.setTitle(audio.getTitle());
				dbAudio.setRemark(audio.getRemark());
				dbAudio.setAuthor(audio.getAuthor());
				dbAudio.setDescription(audio.getDescription());
				dbAudio.setSortFlag(i);
                dbAudio.setCover(audio.getCover());
				if (dbAudio.getPath().equals(audio.getPath())) {
					dbAudio.setPath(audio.getPath());
					dbAudio.setType(FileExUtils.getExtension(audio.getPath()).toUpperCase());
					this.getAudioService().progressAudioInfo(dbAudio);
				}
				dbAudio.updateBy(this.getOperatorUName());
				this.getAudioService().dao().updateById(dbAudio);
			} else {
				audio.setAudioId(IdUtils.getSnowflakeId());
				audio.setContentId(this.getContentEntity().getContentId());
				audio.setSiteId(this.getContentEntity().getSiteId());
				audio.setSiteId(this.getSiteId());
				audio.setType(FileExUtils.getExtension(audio.getPath()).toUpperCase());
				audio.setSortFlag(i);
				audio.createBy(this.getOperatorUName());
				this.getAudioService().progressAudioInfo(audio);
				this.getAudioService().dao().save(audio);
			}
		}
	}

	@Override
	protected void delete0() {
		if (this.hasExtendEntity()) {
			this.getAudioService().dao().deleteByContentIdAndBackup(
					this.getContentEntity().getContentId(),
					this.getOperatorUName()
			);
		}
	}

	@Override
	public void copyTo0(CmsContent newContent, Integer copyType) {
		if (this.hasExtendEntity() && ContentCopyType.isIndependency(copyType)) {
			List<CmsAudio> audioList = this.getAudioService().getAlbumAudioList(this.getContentEntity().getContentId());
			for (CmsAudio audio : audioList) {
				audio.createBy(this.getOperatorUName());
				audio.setAudioId(IdUtils.getSnowflakeId());
				audio.setContentId(newContent.getContentId());
				this.getAudioService().dao().save(audio);
			}
		}
    }

	private IAudioService getAudioService() {
		if (this.audioService == null) {
			this.audioService = SpringUtils.getBean(IAudioService.class);
		}
		return this.audioService;
	}
}
