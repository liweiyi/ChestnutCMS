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
package com.chestnut.cms.stat.controller;

import com.chestnut.cms.stat.baidu.BaiduTjMetrics;
import com.chestnut.cms.stat.baidu.BaiduTongjiConfig;
import com.chestnut.cms.stat.baidu.api.*;
import com.chestnut.cms.stat.baidu.dto.BaiduSourceAllDTO;
import com.chestnut.cms.stat.baidu.dto.BaiduSourceEngineDTO;
import com.chestnut.cms.stat.baidu.dto.BaiduSourceSearchWordDTO;
import com.chestnut.cms.stat.baidu.dto.BaiduTimeTrendDTO;
import com.chestnut.cms.stat.properties.BaiduTjAccessTokenProperty;
import com.chestnut.cms.stat.properties.BaiduTjDomainProperty;
import com.chestnut.cms.stat.service.ICmsStatService;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.util.CmsRestController;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 百度统计数据
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/stat/baidu")
public class BaiduTongjiController extends CmsRestController {

	private final ICmsStatService cmsStatService;

	@PostMapping("/refreshToken")
	public R<?> refreshAccessToken() {
		CmsSite site = this.getCurrentSite();
		this.cmsStatService.refreshBaiduAccessToken(site);
		return R.ok();
	}

	@GetMapping("/sites")
	public R<?> getSiteList() {
		CmsSite site = this.getCurrentSite();
		BaiduTongjiConfig config = BaiduTongjiConfig.read(site.getConfigProps());
		if (StringUtils.isEmpty(config.getAccessToken())) {
			return R.ok(List.of());
		}
		SiteListApi siteListApi = SiteListApi.builder()
				.access_token(config.getAccessToken())
				.build();
		SiteListResponse siteListResponse = siteListApi.get();
		if (!siteListResponse.isSuccess()) {
			return R.ok(List.of()); // 不返回错误了，直接返回空数组
		}
		String[] domains = BaiduTjDomainProperty.getValue(site.getConfigProps());
		List<SiteListResponse.BaiduSite> list = siteListResponse.getList().stream()
				.filter(vo -> ArrayUtils.contains(domains, vo.getDomain())).toList();
		return R.ok(list);
	}

	/**
	 * 趋势概览数据
	 */
	@GetMapping("/trendOverview")
	public R<?> getSiteOverviewTrendReport(@RequestParam Long bdSiteId, @RequestParam LocalDateTime startDate,
										   @RequestParam LocalDateTime endDate) {
		CmsSite site = this.getCurrentSite();
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		List<BaiduTjMetrics> metrics = List.of(BaiduTjMetrics.pv_count, BaiduTjMetrics.visitor_count, BaiduTjMetrics.ip_count, BaiduTjMetrics.avg_visit_time);
		OverviewGetTimeTrendRptApi request = OverviewGetTimeTrendRptApi.builder()
				.access_token(accessToken)
				.site_id(bdSiteId.toString())
				.start_date(startDate.format(DateUtils.FORMAT_YYYYMMDD))
				.end_date(endDate.format(DateUtils.FORMAT_YYYYMMDD))
				.metrics(metrics)
				.build();
		OverviewGetTimeTrendRptResponse response = request.request();
		if (!response.isSuccess()) {
			return R.fail(response.getError_msg());
		}
		return R.ok(response);
	}

	/**
	 * 区域分布概览数据
	 */
	@GetMapping("/districtOverview")
	public R<?> getSiteOverviewDistrctReport(@RequestParam Long bdSiteId, @RequestParam LocalDateTime startDate,
											 @RequestParam LocalDateTime endDate) {
		CmsSite site = this.getCurrentSite();
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		OverviewGetDistrictRptApi api = OverviewGetDistrictRptApi.builder()
				.access_token(accessToken)
				.site_id(bdSiteId.toString())
				.start_date(startDate.format(DateUtils.FORMAT_YYYYMMDD))
				.end_date(endDate.format(DateUtils.FORMAT_YYYYMMDD))
				.build();
		OverviewGetDistrictRptResponse response = api.request();
		if (!response.isSuccess()) {
			return R.fail(response.getError_msg());
		}
		return R.ok(response);
	}

	/**
	 * 其他概览数据
	 */
	@GetMapping("/otherOverview")
	public R<?> getSiteOtherOverviewDatas(@RequestParam Long bdSiteId, @RequestParam LocalDateTime startDate,
										  @RequestParam LocalDateTime endDate) {
		CmsSite site = this.getCurrentSite();
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		OverviewGetCommonTrackRptApi api = OverviewGetCommonTrackRptApi.builder()
				.access_token(accessToken)
				.site_id(bdSiteId.toString())
				.start_date(startDate.format(DateUtils.FORMAT_YYYYMMDD))
				.end_date(endDate.format(DateUtils.FORMAT_YYYYMMDD))
				.build();
		OverviewGetCommonTrackRptResponse request = api.request();
		if (!request.isSuccess()) {
			return R.fail(request.getError_msg());
		}
		return R.ok(request);
	}

	@GetMapping("/timeTrend")
	public R<?> getSiteTimeTrend(@Validated BaiduTimeTrendDTO dto) {
		CmsSite site = this.getCurrentSite();
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		if (StringUtils.isEmpty(dto.getGran())) {
			dto.setGran("day");
		}
		List<BaiduTjMetrics> metrics = List.of(
				BaiduTjMetrics.pv_count,
				BaiduTjMetrics.ip_count,
				BaiduTjMetrics.visitor_count,
				BaiduTjMetrics.visit_count
		);
		TrendTimeAApi request = TrendTimeAApi.builder()
				.access_token(accessToken)
				.site_id(dto.getSiteId().toString())
				.start_date(dto.getStartDate().format(DateUtils.FORMAT_YYYYMMDD))
				.end_date(dto.getEndDate().format(DateUtils.FORMAT_YYYYMMDD))
				.metrics(metrics)
				.gran(dto.getGran())
				.area(dto.getArea())
				.clientDevice(dto.getDeviceType())
				.source(dto.getSource())
				.visitor(dto.getVisitor())
				.build();
		TrendTimeAResponse response = request.request();
		if (!response.isSuccess()) {
			return R.fail(response.getError_msg());
		}
		return R.ok(response);
	}

	/**
	 * 来源统计
	 */
	@GetMapping("/sourceAll")
	public R<?> getSiteSourceAll(@Validated BaiduSourceAllDTO dto) {
		CmsSite site = this.getCurrentSite();
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		List<BaiduTjMetrics> metrics = List.of(
				BaiduTjMetrics.pv_count,
				BaiduTjMetrics.pv_ratio,
				BaiduTjMetrics.visitor_count,
				BaiduTjMetrics.new_visitor_count,
				BaiduTjMetrics.new_visitor_ratio,
				BaiduTjMetrics.ip_count,
				BaiduTjMetrics.bounce_ratio,
				BaiduTjMetrics.avg_visit_time,
				BaiduTjMetrics.avg_visit_pages
		);
		SourceAllAApi request = SourceAllAApi.builder()
				.access_token(accessToken)
				.site_id(dto.getSiteId().toString())
				.start_date(dto.getStartDate().format(DateUtils.FORMAT_YYYYMMDD))
				.end_date(dto.getEndDate().format(DateUtils.FORMAT_YYYYMMDD))
				.metrics(metrics)
				.viewType(dto.getViewType())
				.clientDevice(dto.getClientDevice())
				.visitor(dto.getVisitor())
				.build();
		SourceAllAResponse response = request.request();
		if (!response.isSuccess()) {
			return R.fail(response.getError_msg());
		}
		return R.ok(response);
	}

	/**
	 * 搜索引擎来源统计
	 */
	@GetMapping("/sourceEngine")
	public R<?> getSiteSourceEngine(@Validated BaiduSourceEngineDTO dto) {
		CmsSite site = this.getCurrentSite();
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		List<BaiduTjMetrics> metrics = List.of(
				BaiduTjMetrics.pv_count,
				BaiduTjMetrics.pv_ratio,
				BaiduTjMetrics.visitor_count,
				BaiduTjMetrics.new_visitor_count,
				BaiduTjMetrics.new_visitor_ratio,
				BaiduTjMetrics.ip_count,
				BaiduTjMetrics.bounce_ratio,
				BaiduTjMetrics.avg_visit_time,
				BaiduTjMetrics.avg_visit_pages
		);
		SourceEngineAApi request = SourceEngineAApi.builder()
				.access_token(accessToken)
				.site_id(dto.getSiteId().toString())
				.start_date(dto.getStartDate().format(DateUtils.FORMAT_YYYYMMDD))
				.end_date(dto.getEndDate().format(DateUtils.FORMAT_YYYYMMDD))
				.metrics(metrics)
				.area(dto.getArea())
				.clientDevice(dto.getClientDevice())
				.visitor(dto.getVisitor())
				.build();
		SourceEngineAResponse response = request.request();
		if (!response.isSuccess()) {
			return R.fail(response.getError_msg());
		}
		return R.ok(response);
	}

	/**
	 * 搜索词来源统计
	 */
	@GetMapping("/sourceSearchWord")
	public R<?> getSiteSourceSearchWord(@Validated BaiduSourceSearchWordDTO dto) {
		CmsSite site = this.getCurrentSite();
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		SourceSearchWordAApi request = SourceSearchWordAApi.builder()
				.access_token(accessToken)
				.site_id(dto.getSiteId().toString())
				.start_date(dto.getStartDate().format(DateUtils.FORMAT_YYYYMMDD))
				.end_date(dto.getEndDate().format(DateUtils.FORMAT_YYYYMMDD))
				.source(dto.getSource())
				.clientDevice(dto.getClientDevice())
				.visitor(dto.getVisitor())
				.build();
		SourceSearchWordAResponse response = request.request();
		if (!response.isSuccess()) {
			return R.fail(response.getError_msg());
		}
		return R.ok(response);
	}
}

