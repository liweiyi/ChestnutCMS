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
package com.chestnut.advertisement.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.advertisement.domain.CmsAdClickLog;
import com.chestnut.advertisement.domain.CmsAdHourStat;
import com.chestnut.advertisement.domain.CmsAdViewLog;
import com.chestnut.advertisement.mapper.CmsAdClickLogMapper;
import com.chestnut.advertisement.mapper.CmsAdHourStatMapper;
import com.chestnut.advertisement.mapper.CmsAdViewLogMapper;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.util.CmsRestController;
import com.chestnut.system.security.AdminUserType;


import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@XComment("{API.DOC.CMS.AD_LOG.MODULE}")
@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/ad/stat")
public class AdLogController extends CmsRestController {

	private final IAdvertisementService advService;

	private final CmsAdHourStatMapper advHourStatMapper;

	private final CmsAdClickLogMapper adClickLogMapper;

	private final CmsAdViewLogMapper adViewLogMapper;

	@XComment("{API.DOC.CMS.AD_LOG.GET_STAT_SUM}")
	@GetMapping
	public R<TableData<CmsAdHourStat>> getAdStatSum(
			@RequestParam(required = false) @XComment("{API.DOC.CMS.AD_LOG.BEGIN_TIME}") Date beginTime,
			@RequestParam(required = false) @XComment("{API.DOC.CMS.AD_LOG.END_TIME}") Date endTime) {
        CmsSite site = this.getCurrentSite();
		String begin = Objects.isNull(beginTime) ? null : DateUtils.parseDateToStr(DateUtils.YYYYMMDDHH, beginTime);
		String end = Objects.isNull(endTime) ? null : DateUtils.parseDateToStr(DateUtils.YYYYMMDDHH, endTime);
		List<CmsAdHourStat> list = this.advHourStatMapper.selectGroupByAdvId(site.getSiteId(), begin, end);
		if (!list.isEmpty()) {
			Map<String, String> map = this.advService.getAdvertisementMap();
			list.forEach(l -> l.setAdName(map.get(l.getAdvertisementId().toString())));
		}
		return this.bindDataTable(list);
	}

	@XComment("{API.DOC.CMS.AD_LOG.GET_CHART}")
	@GetMapping("/chart")
	public R<?> getLineChartStatDatas(
			@RequestParam @Min(1) @XComment("{API.DOC.CMS.AD_LOG.AD_ID}") Long advertisementId,
			@RequestParam @XComment("{API.DOC.CMS.AD_LOG.BEGIN_TIME}") Date beginTime,
			@RequestParam @XComment("{API.DOC.CMS.AD_LOG.END_TIME}") Date endTime
	) {
		LambdaQueryWrapper<CmsAdHourStat> q = new LambdaQueryWrapper<CmsAdHourStat>()
				.eq(CmsAdHourStat::getAdvertisementId, advertisementId)
				.gt(Objects.nonNull(beginTime), CmsAdHourStat::getHour, beginTime)
				.gt(Objects.nonNull(endTime), CmsAdHourStat::getHour, endTime)
				.orderByAsc(CmsAdHourStat::getHour);
		List<CmsAdHourStat> list = this.advHourStatMapper.selectList(q);
		if (!list.isEmpty()) {
			Map<String, String> map = this.advService.getAdvertisementMap();
			list.forEach(l -> l.setAdName(map.get(l.getAdvertisementId().toString())));
		}
		Map<String, CmsAdHourStat> collect = list.stream().collect(Collectors.toMap(CmsAdHourStat::getHour, s -> s));
		
		List<String> xAxisDatas = new ArrayList<>();
		Map<String, List<Integer>> lineDatas = new HashMap<>();
		List<Integer> clickDatas = new ArrayList<>();
		List<Integer> viewDatas = new ArrayList<>();
		
		while (!beginTime.after(endTime)) {
			String hourStr = DateUtils.parseDateToStr("yyyyMMddHH", beginTime);
			xAxisDatas.add(hourStr);
			
			CmsAdHourStat stat = collect.get(hourStr);
			clickDatas.add(Objects.isNull(stat) ? 0 : stat.getClick());
			viewDatas.add(Objects.isNull(stat) ? 0 : stat.getViewTotal());
			
			beginTime = DateUtils.addHours(beginTime, 1);
		}
		lineDatas.put("Click", clickDatas);
		lineDatas.put("View", viewDatas);
		
		return R.ok(Map.of("xAxisDatas", xAxisDatas, "lineDatas", lineDatas));
	}

	@XComment("{API.DOC.CMS.AD_LOG.GET_CLICK_LIST}")
	@GetMapping("/click")
	public R<TableData<CmsAdClickLog>> listAdClickLogs() {
		PageRequest pr = getPageRequest();
		CmsSite site = this.getCurrentSite();
		LambdaQueryWrapper<CmsAdClickLog> q = new LambdaQueryWrapper<CmsAdClickLog>()
				.eq(CmsAdClickLog::getSiteId, site.getSiteId()).orderByDesc(CmsAdClickLog::getLogId);
		Page<CmsAdClickLog> page = adClickLogMapper.selectPage(new Page<>(pr.getPageNumber(), pr.getPageSize(), true),
				q);
		if (!page.getRecords().isEmpty()) {
			Map<String, String> map = this.advService.getAdvertisementMap();
			page.getRecords().forEach(l -> l.setAdName(map.get(l.getAdId().toString())));
		}
		return this.bindDataTable(page);
	}

	@XComment("{API.DOC.CMS.AD_LOG.GET_VIEW_LIST}")
	@GetMapping("/view")
	public R<TableData<CmsAdViewLog>> listAdViewLogs() {
		PageRequest pr = getPageRequest();
        CmsSite site = this.getCurrentSite();
		LambdaQueryWrapper<CmsAdViewLog> q = new LambdaQueryWrapper<CmsAdViewLog>()
				.eq(CmsAdViewLog::getSiteId, site.getSiteId()).orderByDesc(CmsAdViewLog::getLogId);
		Page<CmsAdViewLog> page = adViewLogMapper.selectPage(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		if (!page.getRecords().isEmpty()) {
			Map<String, String> map = this.advService.getAdvertisementMap();
			page.getRecords().forEach(l -> l.setAdName(map.get(l.getAdId().toString())));
		}
		return this.bindDataTable(page);
	}
}
