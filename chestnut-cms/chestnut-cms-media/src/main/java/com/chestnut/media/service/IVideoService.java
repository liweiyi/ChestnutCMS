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
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.media.dao.CmsVideoDAO;
import com.chestnut.media.domain.CmsVideo;
import ws.schild.jave.EncoderException;

import java.io.IOException;
import java.util.List;

public interface IVideoService extends HasDAO<CmsVideoDAO> {

	/**
	 * 获取视频集视频列表
	 * 
	 * @param contentId 内容ID
	 * @return 视频列表
	 */
	List<CmsVideo> getAlbumVideoList(Long contentId);

	/**
	 * 处理视频信息
	 * 
	 * @param video 视频数据
	 */
	void progressVideoInfo(CmsVideo video);

	/**
	 * 视频截图
	 *
	 * @param site 站点
	 * @param videoPath 视频路径
	 * @param timestamp 时间戳
	 * @param operator 操作人
	 */
    CmsResource videoScreenshot(CmsSite site, String videoPath, Long timestamp, LoginUser operator)
            throws EncoderException, IOException;
}
