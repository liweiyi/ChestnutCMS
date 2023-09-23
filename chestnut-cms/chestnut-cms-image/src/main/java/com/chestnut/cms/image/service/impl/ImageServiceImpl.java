package com.chestnut.cms.image.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.cms.image.domain.CmsImage;
import com.chestnut.cms.image.mapper.CmsImageMapper;
import com.chestnut.cms.image.service.IImageService;

@Service
public class ImageServiceImpl extends ServiceImpl<CmsImageMapper, CmsImage> implements IImageService {

	@Override
	public List<CmsImage> getAlbumImages(Long contentId) {
		return this.lambdaQuery().eq(CmsImage::getContentId, contentId).list();
	}
}
