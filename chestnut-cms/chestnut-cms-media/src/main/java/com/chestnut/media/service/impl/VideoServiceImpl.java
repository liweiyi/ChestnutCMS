package com.chestnut.media.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.core.impl.InternalDataType_Resource;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.SiteUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.domain.CmsVideo;
import com.chestnut.media.mapper.CmsVideoMapper;
import com.chestnut.media.service.IVideoService;
import com.chestnut.media.util.MediaUtils;

import ws.schild.jave.EncoderException;
import ws.schild.jave.info.MultimediaInfo;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl extends ServiceImpl<CmsVideoMapper, CmsVideo> implements IVideoService {

	private final ISiteService siteService;

	private final IResourceService resourceService;

	@Override
	public List<CmsVideo> getAlbumVideoList(Long contentId) {
		return this.lambdaQuery().eq(CmsVideo::getContentId, contentId).list();
	}

	/**
	 * 处理视频信息
	 *
	 * @param video
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
		String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
		videoPath = siteResourceRoot + videoPath;
		File screenshotFile = new File(siteResourceRoot + "tmp/video_screenshot/" + IdUtils.simpleUUID() + ".jpg");
		FileUtils.forceMkdirParent(screenshotFile);

		try {
			MediaUtils.generateVideoScreenshot(new File(videoPath), screenshotFile, timestamp);
			CmsResource imgResource = this.resourceService.addImageFromFile(site,
					operator.getUsername(), screenshotFile);
			imgResource.setSrc(this.resourceService.getResourceLink(imgResource, true));
			imgResource.setInternalUrl(InternalDataType_Resource.getInternalUrl(imgResource));
			return imgResource;
		} catch (Exception e) {
			throw e;
		} finally {
			if (screenshotFile.exists()) {
				FileUtils.delete(screenshotFile);
			}
		}
	}
}
