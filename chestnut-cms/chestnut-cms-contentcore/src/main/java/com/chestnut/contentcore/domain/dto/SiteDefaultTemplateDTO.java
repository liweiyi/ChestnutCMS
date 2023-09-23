package com.chestnut.contentcore.domain.dto;

import java.util.List;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SiteDefaultTemplateDTO extends BaseDTO {

	/*
	 * 站点ID
	 */
	@LongId
	public Long siteId;

	/*
	 * 已用到指定栏目IDs
	 */
	public List<Long> toCatalogIds;

	/*
	 * 默认模板属性
	 */
	@NotEmpty(message = "{VALIDATOR.CMS.SITE.PUBLISH_PIPE_PROPS_EMPTY}")
	public List<PublishPipeProp> publishPipeProps;
}
