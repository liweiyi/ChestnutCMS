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
package com.chestnut.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.search.domain.SearchLog;
import com.chestnut.search.service.ISearchLogService;
import com.chestnut.system.security.AdminUserType;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/search/log")
public class SearchLogController extends BaseRestController {

	private final ISearchLogService searchLogService;
	
	@Priv(type = AdminUserType.TYPE)
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<SearchLog> page = this.searchLogService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), SearchLog::getWord, query)
				.orderByDesc(SearchLog::getLogId)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Log(title = "删除检索日志", businessType = BusinessType.DELETE)
	@Priv(type = AdminUserType.TYPE)
	@DeleteMapping
	public R<?> delete(@RequestBody @NotEmpty List<String> logIds) {
		this.searchLogService.removeByIds(logIds);
		return R.ok();
	}
}