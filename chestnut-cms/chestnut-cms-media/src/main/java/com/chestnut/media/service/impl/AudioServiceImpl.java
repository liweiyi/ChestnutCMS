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
package com.chestnut.media.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.domain.CmsAudio;
import com.chestnut.media.mapper.CmsAudioMapper;
import com.chestnut.media.service.IAudioService;
import com.chestnut.media.util.MediaUtils;

import lombok.RequiredArgsConstructor;
import ws.schild.jave.info.MultimediaInfo;

@RequiredArgsConstructor
@Service
public class AudioServiceImpl extends ServiceImpl<CmsAudioMapper, CmsAudio> implements IAudioService {
	
	@Override
	public List<CmsAudio> getAlbumAudioList(Long contentId) {
		return this.lambdaQuery().eq(CmsAudio::getContentId, contentId).list();
	}
	
	/**
	 * 处理音频信息
	 * 
	 * @param audio
	 */
	@Override
	public void progressAudioInfo(CmsAudio audio) {
		audio.setDuration(-1L);
		audio.setChannels(-1);
		audio.setBitRate(-1);
		audio.setSamplingRate(-1);
		String url = InternalUrlUtils.getActualPreviewUrl(audio.getPath());
		MultimediaInfo multimediaInfo = MediaUtils.getMultimediaInfo(url);
		if (Objects.nonNull(multimediaInfo)) {
			audio.setFormat(multimediaInfo.getFormat());
			audio.setDuration(multimediaInfo.getDuration());
			audio.setDecoder(multimediaInfo.getAudio().getDecoder());
			audio.setChannels(multimediaInfo.getAudio().getChannels());
			audio.setBitRate(multimediaInfo.getAudio().getBitRate());
			audio.setSamplingRate(multimediaInfo.getAudio().getSamplingRate());
		}
	}
}
