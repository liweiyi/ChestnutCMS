package com.chestnut.link.domain.dto;

import org.springframework.beans.BeanUtils;

import com.chestnut.link.domain.CmsLink;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkDTO {

    private Long linkId;

    private Long siteId;

    @NotNull
    private Long groupId;

    @NotNull
    private String name;

    @NotNull
    private String url;

    private String logo;
    
    private Long sortFlag;
    
	public static LinkDTO newInstance(CmsLink link) {
		LinkDTO dto = new LinkDTO();
    	BeanUtils.copyProperties(link, dto);
		return dto;
	}
}
