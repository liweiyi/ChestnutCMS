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
package com.chestnut.cms.stat.controller;

import com.chestnut.cms.stat.baidu.BaiduTongjiUtils;
import com.chestnut.cms.stat.baidu.dto.BaiduTimeTrendDTO;
import com.chestnut.cms.stat.baidu.vo.BaiduOverviewReportVO;
import com.chestnut.cms.stat.baidu.vo.BaiduSiteVO;
import com.chestnut.cms.stat.baidu.vo.BaiduTimeTrendVO;
import com.chestnut.cms.stat.properties.BaiduTjAccessTokenProperty;
import com.chestnut.cms.stat.properties.BaiduTjDomainProperty;
import com.chestnut.cms.stat.service.ICmsStatService;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class BaiduTongjiController extends BaseRestController {

	private final ISiteService siteService;

	private final ICmsStatService cmsStatService;

	@PutMapping("/refreshToken")
	public R<?> refreshAccessToken() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.cmsStatService.refreshBaiduAccessToken(site);
		return R.ok();
	}

	@GetMapping("/sites")
	public R<?> getSiteList() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		R<List<BaiduSiteVO>> r = this.cmsStatService.getBaiduSiteList(site);
		if (r.isSuccess()) {
			String[] domains = BaiduTjDomainProperty.getValue(site.getConfigProps());
			List<BaiduSiteVO> list = r.getData().stream().filter(vo -> ArrayUtils.contains(domains, vo.getDomain())).toList();
			return R.ok(list);
		}
		return R.ok(); // 不返回错误了，直接返回空数组
	}

	/**
	 * 趋势概览数据
	 *
	 * @param bdSiteId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	@GetMapping("/trendOverview")
	public R<?> getSiteOverviewTrendReport(@RequestParam Long bdSiteId, @RequestParam LocalDateTime startDate,
										   @RequestParam LocalDateTime endDate) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		BaiduOverviewReportVO report = BaiduTongjiUtils.getSiteOverviewTimeTrend(accessToken, bdSiteId, startDate,
				endDate);
		return R.ok(report);
	}

	/**
	 * 区域分布概览数据
	 *
	 * @param bdSiteId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	@GetMapping("/districtOverview")
	public R<?> getSiteOverviewDistrctReport(@RequestParam Long bdSiteId, @RequestParam LocalDateTime startDate,
											 @RequestParam LocalDateTime endDate) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		BaiduOverviewReportVO report = BaiduTongjiUtils.getSiteOverviewDistrict(accessToken, bdSiteId, startDate,
				endDate);
		return R.ok(BaiduTongjiUtils.parseOverviewReportToTableData(report));
	}

	/**
	 * 其他概览数据
	 *
	 * @param bdSiteId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	@GetMapping("/otherOverview")
	public R<?> getSiteOtherOverviewDatas(@RequestParam Long bdSiteId, @RequestParam LocalDateTime startDate,
										  @RequestParam LocalDateTime endDate) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		Map<String, BaiduOverviewReportVO> siteOverviewOthers = BaiduTongjiUtils.getSiteOverviewOthers(accessToken,
				bdSiteId, startDate, endDate);
		Map<String, List<Map<String, Object>>> datas = siteOverviewOthers.entrySet().stream().map(e -> {
			Map<String, List<Map<String, Object>>> map = Map.of(e.getKey(),
					BaiduTongjiUtils.parseOverviewReportToTableData(e.getValue()));
			return map.entrySet().iterator().next();
		}).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		return R.ok(datas);
	}

	@GetMapping("/timeTrend")
	public R<?> getSiteTimeTrend(@Validated BaiduTimeTrendDTO dto) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		if (StringUtils.isEmpty(dto.getGran())) {
			dto.setGran("day");
		}
		dto.setMetrics(List.of("pv_count", "visitor_count", "ip_count", "visit_count"));
		BaiduTimeTrendVO siteTimeTrend = BaiduTongjiUtils.getSiteTimeTrend(accessToken, dto);
		return R.ok(siteTimeTrend);
	}
}

