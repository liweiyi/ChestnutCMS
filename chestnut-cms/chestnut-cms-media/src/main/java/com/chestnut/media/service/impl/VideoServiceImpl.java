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
package com.chestnut.media.service.impl;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.storage.local.LocalFileStorageType;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.core.impl.InternalDataType_Resource;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.media.dao.CmsVideoDAO;
import com.chestnut.media.domain.CmsVideo;
import com.chestnut.media.service.IVideoService;
import com.chestnut.media.util.MediaUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import ws.schild.jave.EncoderException;
import ws.schild.jave.info.MultimediaInfo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static com.chestnut.contentcore.core.impl.InternalDataType_Resource.InternalUrl_Param_StorageType;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements IVideoService {

	private final CmsVideoDAO dao;

	private final IResourceService resourceService;

	@Override
	public List<CmsVideo> getAlbumVideoList(Long contentId) {
		return this.dao().lambdaQuery().eq(CmsVideo::getContentId, contentId).list();
	}

	/**
	 * 处理视频信息
	 */
	@Override
	public void progressVideoInfo(CmsVideo video) {
		video.setDuration(-1L);
		video.setWidth(0);
		video.setHeight(0);
		video.setBitRate(-1);
		video.setFrameRate(-1);
		String url = InternalUrlUtils.getActualPreviewUrl(video.getPath());
		MultimediaInfo multimediaInfo = MediaUtils.getMultimediaInfo(url);
		if (Objects.nonNull(multimediaInfo)) {
			video.setFormat(multimediaInfo.getFormat());
			video.setDuration(multimediaInfo.getDuration());
			video.setWidth(multimediaInfo.getVideo().getSize().getWidth());
			video.setHeight(multimediaInfo.getVideo().getSize().getHeight());
			video.setDecoder(multimediaInfo.getVideo().getDecoder());
			video.setBitRate(multimediaInfo.getVideo().getBitRate());
			video.setFrameRate(Float.valueOf(multimediaInfo.getVideo().getFrameRate()).intValue());
		}
	}

	@Override
	public CmsResource videoScreenshot(CmsSite site, String videoPath, Long timestamp, LoginUser operator)
			throws EncoderException, IOException {
		InternalURL internalURL = InternalUrlUtils.parseInternalUrl(videoPath);
		if (Objects.isNull(internalURL)) {
			throw new RuntimeException("InternalUrl parse failed.");
		}
		String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
        String storageType = internalURL.getParams().get(InternalUrl_Param_StorageType);
		if (LocalFileStorageType.TYPE.equals(storageType)) {
			videoPath = siteResourceRoot + internalURL.getPath();
		} else {
			IInternalDataType idt = ContentCoreUtils.getInternalDataType(InternalDataType_Resource.ID);
			videoPath = idt.getLink(internalURL, 1, "", false);
		}
		File screenshotFile = new File(siteResourceRoot + "tmp/video_screenshot/" + IdUtils.simpleUUID() + ".jpg");
		FileUtils.forceMkdirParent(screenshotFile);

		try {
			if (ServletUtils.isHttpUrl(videoPath)) {
				MediaUtils.generateRemoteVideoScreenshot(new URL(videoPath), screenshotFile, timestamp);
			} else {
				MediaUtils.generateVideoScreenshot(new File(videoPath), screenshotFile, timestamp);
			}
			CmsResource imgResource = this.resourceService.addImageFromFile(site,
					operator.getUsername(), screenshotFile);
			imgResource.setSrc(this.resourceService.getResourceLink(imgResource, null, true));
			imgResource.setInternalUrl(InternalDataType_Resource.getInternalUrl(imgResource));
			return imgResource;
		} finally {
			if (screenshotFile.exists()) {
				FileUtils.delete(screenshotFile);
			}
		}
	}

	@Override
	public CmsVideoDAO dao() {
		return dao;
	}
}
