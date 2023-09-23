package com.chestnut.contentcore.domain.dto;

import java.util.List;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.validator.LongId;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogApplyConfigPropsDTO extends BaseDTO {

	/*
	 * 源栏目ID
	 */
	@LongId
	public Long catalogId;

	/*
	 * 指定更新的目标栏目Ids，为空表示更新所有子栏目
	 */
	public List<Long> toCatalogIds;

	/*
	 * 是否覆盖所有扩展属性配置
	 */
	public boolean allExtends;
	
	/*
	 * 指定应用的扩展属性Keys
	 */
	public List<String> configPropKeys;
}
