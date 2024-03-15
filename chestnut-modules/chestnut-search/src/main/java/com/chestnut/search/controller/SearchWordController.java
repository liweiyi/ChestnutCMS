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
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.search.domain.SearchWord;
import com.chestnut.search.domain.SearchWordHourStat;
import com.chestnut.search.domain.dto.SearchWordToppingDTO;
import com.chestnut.search.service.ISearchWordHourStatService;
import com.chestnut.search.service.ISearchWordService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/search/word")
public class SearchWordController extends BaseRestController {

	private final ISearchWordService searchWordStatService;

	private final ISearchWordHourStatService searchWordHourStatService;
	
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<SearchWord> page = this.searchWordStatService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), SearchWord::getWord, query)
				.orderByDesc(SearchWord::getTopFlag, SearchWord::getSearchTotal)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@GetMapping("/trend")
	public R<?> getTrendStatData(
			@RequestParam @Min(1) Long wordId,
			@RequestParam LocalDateTime beginTime,
			@RequestParam LocalDateTime endTime
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

	@Log(title = "新增搜索词", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addWord(@RequestBody SearchWord wordStat) {
		wordStat.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.searchWordStatService.addWord(wordStat);
		return R.ok();
	}

	@Log(title = "编辑搜索词", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> editWord(@RequestBody SearchWord wordStat) {
		wordStat.updateBy(StpAdminUtil.getLoginUser().getUsername());
		this.searchWordStatService.editWord(wordStat);
		return R.ok();
	}

	@Log(title = "编辑搜索词", businessType = BusinessType.UPDATE)
	@PutMapping("/set_top")
	public R<?> setTop(@RequestBody SearchWordToppingDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.searchWordStatService.setTop(dto);
		return R.ok();
	}

	@Log(title = "编辑搜索词", businessType = BusinessType.UPDATE)
	@PutMapping("/cancel_top")
	public R<?> cancelTop(@RequestBody @NotEmpty List<Long> wordIds) {
		this.searchWordStatService.cancelTop(wordIds, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	@Log(title = "删除搜索词", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> deleteWords(@RequestBody @NotEmpty List<Long> wordIds) {
		this.searchWordStatService.deleteWords(wordIds);
		return R.ok();
	}
}