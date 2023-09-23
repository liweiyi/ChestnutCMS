package com.chestnut.media.domain.dto;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.dto.ContentDTO;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.domain.CmsVideo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoAlbumDTO extends ContentDTO {

	private List<CmsVideo> videoList;

	public static VideoAlbumDTO newInstance(CmsContent content, List<CmsVideo> videoList) {
		VideoAlbumDTO dto = new VideoAlbumDTO();
		BeanUtils.copyProperties(content, dto);
		dto.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
    	dto.setVideoList(videoList);
		return dto;
	}
}
