package com.chestnut.cms.image.domain.dto;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.chestnut.cms.image.domain.CmsImage;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.dto.ContentDTO;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.util.InternalUrlUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageAlbumDTO extends ContentDTO {

	private List<CmsImage> imageList;

	public static ImageAlbumDTO newInstance(CmsContent content, List<CmsImage> images) {
		ImageAlbumDTO dto = new ImageAlbumDTO();
		BeanUtils.copyProperties(content, dto);
		dto.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
    	dto.setImageList(images);
		return dto;
	}
}
