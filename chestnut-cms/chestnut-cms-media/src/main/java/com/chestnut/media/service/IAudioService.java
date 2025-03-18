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
package com.chestnut.media.service;

import com.chestnut.common.db.mybatisplus.HasDAO;
import com.chestnut.media.dao.CmsAudioDAO;
import com.chestnut.media.domain.CmsAudio;

import java.util.List;

public interface IAudioService extends HasDAO<CmsAudioDAO> {

	/**
	 * 获取音频集音频列表数据
	 * 
	 * @param contentId 内容ID
	 * @return 音频列表
	 */
	List<CmsAudio> getAlbumAudioList(Long contentId);

	/**
	 * 处理音频信息
	 * 
	 * @param audio 音频数据
	 */
	void progressAudioInfo(CmsAudio audio);
}
