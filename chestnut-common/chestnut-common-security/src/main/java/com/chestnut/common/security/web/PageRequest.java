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
package com.chestnut.common.security.web;

import com.chestnut.common.security.config.ChestnutPageConfig;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class PageRequest {

	/**
	 * 当前页码，从1开始，通过`chestnut.page.start-page-number`配置
	 */
	private static final String GET_PARAM_PAGENUM = "pageNum";

	/**
	 * 每页条数，默认20，通过`chestnut.page.default-page-size`配置
	 */
	private static final String GET_PARAM_PAGESIZE = "pageSize";

	/**
	 * 排序条件，格式：column1#desc@column2#asc
	 */
	private static final String GET_PARAM_SORTS = "sorts";

	/**
	 * 是否导出Excel
	 */
	private static final String GET_PARAM_EXPORT = "export";

	/**
	 * 页码
	 */
	private int pageNum;

	/**
	 * 每页数量
	 */
	private int pageSize;

	/**
	 * 排序条件集合
	 */
	private List<SortOrder> sorts;

	public static PageRequest of(int pageNumber, int pageSize) {
		PageRequest pageRequest = new PageRequest();
		pageRequest.pageNum = pageNumber;
		pageRequest.pageSize = pageSize;
		pageRequest.sorts = List.of();
		return pageRequest;
	}

	public static PageRequest of(int pageNumber, int pageSize, List<SortOrder> sorts) {
		PageRequest pageRequest = new PageRequest();
		pageRequest.pageNum = pageNumber;
		pageRequest.pageSize = pageSize;
		pageRequest.sorts = sorts;
		return pageRequest;
	}

	public int getPageNumber() {
		return this.pageNum;
	}

	/**
	 * 通过默认请求参数构造分页信息
	 * RequestParameters: pageNum=1&pageSize=20&sorts=col1#asc@col2#desc
	 */
	public static PageRequest build() {
		HttpServletRequest request = ServletUtils.getRequest();
		int page = ServletUtils.getParameterToInt(request, GET_PARAM_PAGENUM, ChestnutPageConfig.getStartPageNumber());
		int size = ServletUtils.getParameterToInt(request, GET_PARAM_PAGESIZE, ChestnutPageConfig.getDefaultPageSize());
		size = Math.min(size, ChestnutPageConfig.getMaxPageSize()); // 分页数量不超过1W

		String sortStr = ServletUtils.getParameter(GET_PARAM_SORTS);
		List<PageRequest.SortOrder> sorts = List.of();
		if (StringUtils.isNotEmpty(sortStr)) {
			sorts = StringUtils.splitToMap(sortStr, "@", "#").entrySet().stream()
					.map(e ->
							Sort.Direction.fromOptionalString(e.getValue())
									.map(direction ->
											new PageRequest.SortOrder(e.getKey(), direction)).orElse(null))
					.filter(Objects::nonNull).toList();
		}
		return PageRequest.of(page, size, sorts);
	}

	@Getter
	@Setter
	public static class SortOrder {

		/**
		 * 排序字段
		 */
		private String column;

		/**
		 * 升序/降序
		 */
		private Sort.Direction direction;

		public SortOrder(String column, Sort.Direction direction) {
			this.column = column;
			this.direction = direction;
		}
	}
}
