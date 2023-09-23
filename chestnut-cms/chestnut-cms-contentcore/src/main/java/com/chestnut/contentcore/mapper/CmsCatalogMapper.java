package com.chestnut.contentcore.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.contentcore.domain.CmsCatalog;

public interface CmsCatalogMapper extends BaseMapper<CmsCatalog> {

	/**
	 * 排序标识范围内[startSort, endSort)的所有栏目排序值+1
	 * 
	 * @param startSort
	 * @param endSort
	 * @return
	 */
	@Update("update cms_catalog set sort_flag = sort_flag + 1 where sort_flag >= #{startSort} and sort_flag < #{endSort}")
	public Long catalogSortPlusOne(@Param("startSort") Long startSort, @Param("endSort") Long endSort);

	/**
	 * 排序标识范围内(startSort, endSort]的所有栏目排序值-1
	 * 
	 * @param startSort
	 * @param endSort
	 * @return
	 */
	@Update("update cms_catalog set sort_flag = sort_flag - 1 where sort_flag > #{startSort} and sort_flag <= #{endSort}")
	public Long catalogSortMinusOne(@Param("startSort") Long startSort, @Param("endSort") Long endSort);
}
