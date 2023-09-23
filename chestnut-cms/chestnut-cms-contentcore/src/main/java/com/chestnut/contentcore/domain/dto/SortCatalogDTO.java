package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortCatalogDTO extends BaseDTO {

	/**
	 * 排序栏目ID
	 */
	@NotNull
	@Min(1)
	public Long catalogId;
	
	/**
	 * 顺序变化值（正数向上移动，负数向下移动）
	 */
	@NotNull
	public Integer sort;
}
