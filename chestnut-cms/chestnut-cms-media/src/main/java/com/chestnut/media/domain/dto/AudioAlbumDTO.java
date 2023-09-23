package com.chestnut.media.domain.dto;

import java.util.List;

import org.springframework.beans.BeanUtils;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.dto.ContentDTO;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.domain.CmsAudio;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AudioAlbumDTO extends ContentDTO {

	private List<CmsAudio> audioList;

	public static AudioAlbumDTO newInstance(CmsContent content, List<CmsAudio> audioList) {
		AudioAlbumDTO dto = new AudioAlbumDTO();
		BeanUtils.copyProperties(content, dto);
		dto.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
    	dto.setAudioList(audioList);
		return dto;
	}
}
