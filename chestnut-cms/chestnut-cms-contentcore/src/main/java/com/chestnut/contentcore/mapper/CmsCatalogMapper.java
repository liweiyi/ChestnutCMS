/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
