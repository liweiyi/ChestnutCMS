/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.search.SearchConsts;
import com.chestnut.search.domain.SearchWord;
import com.chestnut.search.domain.SearchWordHourStat;
import com.chestnut.search.domain.dto.CreateSearchWordRequest;
import com.chestnut.search.domain.dto.SearchWordToppingRequest;
import com.chestnut.search.domain.dto.UpdateSearchWordRequest;
import com.chestnut.search.service.ISearchWordHourStatService;
import com.chestnut.search.service.ISearchWordService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;


import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@XComment("{API.DOC.SEARCH.WORD_MODULE}")
@Priv(type = AdminUserType.TYPE, value = SearchConsts.SearchPriv.LOG_VIEW)
@RequiredArgsConstructor
@RestController
@RequestMapping("/search/word")
public class SearchWordController extends BaseRestController {

	private final ISearchWordService searchWordStatService;

	private final ISearchWordHourStatService searchWordHourStatService;

	@XComment("{API.DOC.SEARCH.WORD_GET_LIST}")
	@GetMapping("/list")
	public R<TableData<SearchWord>> getPageList(@RequestParam(required = false) @Length(max = 255) @XComment("{API.DOC.SEARCH.QUERY}") String query) {
		PageRequest pr = this.getPageRequest();
		Page<SearchWord> page = this.searchWordStatService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), SearchWord::getWord, query)
				.orderByDesc(SearchWord::getTopFlag, SearchWord::getSearchTotal)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}
	@XComment("{API.DOC.SEARCH.WORD_TREND}")
	@GetMapping("/trend")
	public R<?> getTrendStatData(
			@RequestParam @LongId @XComment("{API.DOC.SEARCH.WORD_ID}") Long wordId,
			@RequestParam @XComment("{CC.ENTITY.BEGIN_TIME}") LocalDateTime beginTime,
			@RequestParam @XComment("{CC.ENTITY.END_TIME}") LocalDateTime endTime
	) {
		List<SearchWordHourStat> list = this.searchWordHourStatService.lambdaQuery()
				.eq(SearchWordHourStat::getWordId, wordId)
				.ge(Objects.nonNull(beginTime), SearchWordHourStat::getHour, DateUtils.FORMAT_YYYYMMDDHH.format(beginTime))
				.le(Objects.nonNull(endTime), SearchWordHourStat::getHour, DateUtils.FORMAT_YYYYMMDDHH.format(endTime))
				.orderByAsc(SearchWordHourStat::getHour)
				.list();
		Map<String, SearchWordHourStat> collect = list.stream()
				.collect(Collectors.toMap(SearchWordHourStat::getHour, s -> s));


		List<String> xAxisDatas = new ArrayList<>();
		Map<String, List<Integer>> lineDatas = new HashMap<>();
		List<Integer> searchCountDatas = new ArrayList<>();

		while (!beginTime.isAfter(endTime)) {
			String hourStr = DateUtils.FORMAT_YYYYMMDDHH.format(beginTime);
			xAxisDatas.add(hourStr);

			SearchWordHourStat stat = collect.get(hourStr);
			searchCountDatas.add(Objects.isNull(stat) ? 0 : stat.getSearchCount());

			beginTime = beginTime.plusHours(1);
		}
		lineDatas.put("SearchCount", searchCountDatas);

		return R.ok(Map.of("xAxisDatas", xAxisDatas, "lineDatas", lineDatas));
	}

	@XComment("{API.DOC.SEARCH.WORD_ADD}")
	@Log(title = "新增搜索词", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<Void> addWord(@RequestBody CreateSearchWordRequest req) {
		this.searchWordStatService.addWord(req);
		return R.ok();
	}

	@XComment("{API.DOC.SEARCH.WORD_UPDATE}")
	@Log(title = "编辑搜索词", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<Void> editWord(@RequestBody @Validated UpdateSearchWordRequest req) {
		this.searchWordStatService.editWord(req);
		return R.ok();
	}

	@XComment("{API.DOC.SEARCH.WORD_SET_TOP}")
	@Log(title = "搜索词置顶", businessType = BusinessType.UPDATE)
	@PostMapping("/set_top")
	public R<Void> setTop(@RequestBody SearchWordToppingRequest req) {
		this.searchWordStatService.setTop(req);
		return R.ok();
	}

	@XComment("{API.DOC.SEARCH.WORD_CANCEL_TOP}")
	@Log(title = "取消搜索词置顶", businessType = BusinessType.UPDATE)
	@PostMapping("/cancel_top")
	public R<Void> cancelTop(@RequestBody @NotEmpty @XComment("{API.DOC.SEARCH.WORD_IDS}") List<Long> wordIds) {
		this.searchWordStatService.cancelTop(wordIds, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	@XComment("{API.DOC.SEARCH.WORD_DELETE}")
	@Log(title = "删除搜索词", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<Void> deleteWords(@RequestBody @NotEmpty @XComment("{API.DOC.SEARCH.WORD_IDS}") List<Long> wordIds) {
		this.searchWordStatService.deleteWords(wordIds);
		return R.ok();
	}
}