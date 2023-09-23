package com.chestnut.cms.image.domain.vo;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.chestnut.cms.image.domain.CmsImage;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.util.InternalUrlUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageAlbumVO extends ContentVO {

	private List<CmsImage> imageList;

	public static ImageAlbumVO newInstance(CmsContent content, List<CmsImage> images) {
		ImageAlbumVO dto = new ImageAlbumVO();
		BeanUtils.copyProperties(content, dto);
		dto.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
    	dto.setImageList(images);
		return dto;
	}
}
