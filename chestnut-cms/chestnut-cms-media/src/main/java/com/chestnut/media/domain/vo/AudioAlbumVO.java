package com.chestnut.media.domain.vo;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.domain.CmsAudio;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AudioAlbumVO extends ContentVO {

	private List<CmsAudio> audioList;

	public static AudioAlbumVO newInstance(CmsContent content, List<CmsAudio> audioList) {
		AudioAlbumVO dto = new AudioAlbumVO();
		BeanUtils.copyProperties(content, dto);
		dto.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
    	dto.setAudioList(audioList);
		return dto;
	}
}
