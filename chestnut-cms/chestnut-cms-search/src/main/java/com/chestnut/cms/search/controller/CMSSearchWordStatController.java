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
package com.chestnut.cms.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.search.CmsSearchConstants;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.search.domain.SearchWord;
import com.chestnut.search.service.ISearchWordService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/search/word")
public class CMSSearchWordStatController extends BaseRestController {

	private final ISiteService siteService;

	private final ISearchWordService searchWordStatService;
	
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		Page<SearchWord> page = this.searchWordStatService.lambdaQuery()
				.eq(SearchWord::getSource, CmsSearchConstants.generateSearchSource(site.getSiteId()))
				.like(StringUtils.isNotEmpty(query), SearchWord::getWord, query)
				.orderByDesc(SearchWord::getTopFlag, SearchWord::getSearchTotal)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Log(title = "新增搜索词", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addWord(@RequestBody SearchWord wordStat) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		wordStat.setSource(CmsSearchConstants.generateSearchSource(site.getSiteId()));
		wordStat.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.searchWordStatService.addWord(wordStat);
		return R.ok();
	}
}