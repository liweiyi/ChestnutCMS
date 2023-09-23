package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.validator.LongId;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortContentDTO extends BaseDTO {

	/**
	 * 排序内容ID
	 */
	@LongId
	public Long contentId;
	
	/**
	 * 排序目标内容ID
	 */
	@LongId
	public Long targetContentId;
}
