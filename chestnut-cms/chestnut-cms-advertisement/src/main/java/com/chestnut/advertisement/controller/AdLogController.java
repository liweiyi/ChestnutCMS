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
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.security.AdminUserType;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/ad/stat")
public class AdLogController extends BaseRestController {
	
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMddHH");

	private final ISiteService siteService;

	private final IAdvertisementService advService;

	private final CmsAdHourStatMapper advHourStatMapper;

	private final CmsAdClickLogMapper adClickLogMapper;

	private final CmsAdViewLogMapper adViewLogMapper;

	@GetMapping
	public R<?> getAdStatSum(@RequestParam(required = false) Date beginTime,
			@RequestParam(required = false) Date endTime) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String begin = Objects.isNull(beginTime) ? null : FORMAT.format(beginTime);
		String end = Objects.isNull(endTime) ? null : FORMAT.format(endTime);
		List<CmsAdHourStat> list = this.advHourStatMapper.selectGroupByAdvId(site.getSiteId(), begin, end);
		if (list.size() > 0) {
			Map<String, String> map = this.advService.getAdvertisementMap();
			list.forEach(l -> l.setAdName(map.get(l.getAdvertisementId().toString())));
		}
		return this.bindDataTable(list);
	}

	@GetMapping("/chart")
	public R<?> getLineChartStatDatas(@RequestParam @Min(1) Long advertisementId, @RequestParam Date beginTime, @RequestParam Date endTime) {
		List<CmsAdHourStat> list = this.advHourStatMapper.selectHourStat(advertisementId, FORMAT.format(beginTime), FORMAT.format(endTime));
		if (list.size() > 0) {
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
			viewDatas.add(Objects.isNull(stat) ? 0 : stat.getView());
			
			beginTime = DateUtils.addHours(beginTime, 1);
		}
		lineDatas.put("Click", clickDatas);
		lineDatas.put("View", viewDatas);
		
		return R.ok(Map.of("xAxisDatas", xAxisDatas, "lineDatas", lineDatas));
	}

	@GetMapping("/click")
	public R<?> listAdClickLogs() {
		PageRequest pr = getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		LambdaQueryWrapper<CmsAdClickLog> q = new LambdaQueryWrapper<CmsAdClickLog>()
				.eq(CmsAdClickLog::getSiteId, site.getSiteId()).orderByDesc(CmsAdClickLog::getLogId);
		Page<CmsAdClickLog> page = adClickLogMapper.selectPage(new Page<>(pr.getPageNumber(), pr.getPageSize(), true),
				q);
		if (page.getRecords().size() > 0) {
			Map<String, String> map = this.advService.getAdvertisementMap();
			page.getRecords().forEach(l -> l.setAdName(map.get(l.getAdId().toString())));
		}
		return this.bindDataTable(page);
	}

	@GetMapping("/view")
	public R<?> listAdViewLogs() {
		PageRequest pr = getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		LambdaQueryWrapper<CmsAdViewLog> q = new LambdaQueryWrapper<CmsAdViewLog>()
				.eq(CmsAdViewLog::getSiteId, site.getSiteId()).orderByDesc(CmsAdViewLog::getLogId);
		Page<CmsAdViewLog> page = adViewLogMapper.selectPage(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		if (page.getRecords().size() > 0) {
			Map<String, String> map = this.advService.getAdvertisementMap();
			page.getRecords().forEach(l -> l.setAdName(map.get(l.getAdId().toString())));
		}
		return this.bindDataTable(page);
	}
}
