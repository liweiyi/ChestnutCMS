package com.chestnut.contentcore.domain.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Pattern;

import org.springframework.beans.BeanUtils;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.contentcore.domain.CmsSite;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteDTO extends BaseDTO {

	private Long siteId;

	private String name;

	private String description;

	private String logo;

	private String logoSrc;

	@Pattern(regexp = "^[A-Za-z0-9]+$", message = "站点目录只能使用大小写字母及数字组合")
	private String path;

	private String url;

	private String resourceUrl;

	private String staticSuffix;

	private String deptCode;

	private String seoKeywords;

	private String seoDescription;

	private String seoTitle;

	private Map<String, String> configProps;

	private List<PublishPipeProp> publishPipeDatas;

	private Map<String, Object> params;

	public static SiteDTO newInstance(CmsSite site) {
		SiteDTO dto = new SiteDTO();
		BeanUtils.copyProperties(site, dto);
		return dto;
	}
}
