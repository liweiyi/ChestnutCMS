/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.cms.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.search.CmsSearchConstants;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.util.CmsRestController;
import com.chestnut.search.domain.SearchWord;
import com.chestnut.search.domain.dto.CreateSearchWordRequest;
import com.chestnut.search.service.ISearchWordService;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/search/word")
public class CMSSearchWordStatController extends CmsRestController {

	private final ISearchWordService searchWordStatService;
	
	@GetMapping("/list")
	public R<?> getPageList(@RequestParam(required = false) String query) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.getCurrentSite();
		Page<SearchWord> page = this.searchWordStatService.lambdaQuery()
				.eq(SearchWord::getSource, CmsSearchConstants.generateSearchSource(site.getSiteId()))
				.like(StringUtils.isNotEmpty(query), SearchWord::getWord, query)
				.orderByDesc(SearchWord::getTopFlag, SearchWord::getSearchTotal)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Log(title = "新增搜索词", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<?> addWord(@RequestBody @Validated CreateSearchWordRequest req) {
		CmsSite site = this.getCurrentSite();
		req.setSource(CmsSearchConstants.generateSearchSource(site.getSiteId()));
		this.searchWordStatService.addWord(req);
		return R.ok();
	}
}