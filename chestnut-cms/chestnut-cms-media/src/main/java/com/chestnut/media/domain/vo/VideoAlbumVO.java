package com.chestnut.media.domain.vo;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.domain.CmsVideo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoAlbumVO extends ContentVO {

	private List<CmsVideo> videoList;

	public static VideoAlbumVO newInstance(CmsContent content, List<CmsVideo> videoList) {
		VideoAlbumVO dto = new VideoAlbumVO();
		BeanUtils.copyProperties(content, dto);
		dto.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
    	dto.setVideoList(videoList);
		return dto;
	}
}
