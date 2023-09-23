package com.chestnut.contentcore.domain.dto;

import com.chestnut.system.validator.LongId;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublishSiteDTO {

	/**
	 * 站点ID
	 */
	@LongId
	private Long siteId;
	
	/**
	 * 是否只发布首页
	 */
	@NotNull
	private boolean publishIndex;
	
	/**
	 * 发布内容状态
	 */
	private String contentStatus;
}
