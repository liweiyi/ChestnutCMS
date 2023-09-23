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
