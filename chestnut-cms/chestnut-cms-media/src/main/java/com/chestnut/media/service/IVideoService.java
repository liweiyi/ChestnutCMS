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
package com.chestnut.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.media.domain.CmsVideo;
import ws.schild.jave.EncoderException;

import java.io.IOException;
import java.util.List;

public interface IVideoService extends IService<CmsVideo> {

	/**
	 * 获取视频集视频列表
	 * 
	 * @param contentId
	 * @return
	 */
	public List<CmsVideo> getAlbumVideoList(Long contentId);

	/**
	 * 处理视频信息
	 * 
	 * @param video
	 */
	void progressVideoInfo(CmsVideo video);

	/**
	 * 视频截图
	 *
	 * @param site
	 * @param videoPath
	 * @param timestamp
	 * @param operator
	 * @return
	 * @throws EncoderException
	 * @throws IOException
	 */
    CmsResource videoScreenshot(CmsSite site, String videoPath, Long timestamp, LoginUser operator)
            throws EncoderException, IOException;
}
