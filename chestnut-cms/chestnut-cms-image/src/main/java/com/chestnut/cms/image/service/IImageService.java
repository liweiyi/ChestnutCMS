package com.chestnut.cms.image.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.cms.image.domain.CmsImage;

import java.util.List;

public interface IImageService extends IService<CmsImage> {

	/**
	 * 获取图集中的图片列表
	 * 
	 * @param contentId
	 * @return
	 */
	public List<CmsImage> getAlbumImages(Long contentId);
}
