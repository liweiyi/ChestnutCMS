package com.chestnut.contentcore.domain.dto;

import com.chestnut.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublishCatalogDTO {

	/**
	 * 栏目ID
	 */
	@LongId
	private Long catalogId;
	
	/**
	 * 是否发布子栏目
	 */
	@NotNull
	private Boolean publishChild;
	
	/**
	 * 是否发布栏目内容
	 */
	@NotNull
	private Boolean publishDetail;
	
	/**
	 * 发布内容状态
	 */
	@NotEmpty
	private String publishStatus;
}
