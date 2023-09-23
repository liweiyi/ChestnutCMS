package com.chestnut.advertisement.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.chestnut.advertisement.domain.CmsAdClickLog;
import com.chestnut.advertisement.domain.CmsAdViewLog;
import com.chestnut.advertisement.mapper.CmsAdClickLogMapper;
import com.chestnut.advertisement.mapper.CmsAdViewLogMapper;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.advertisement.service.IAdvertisementStatService;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.IdUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertisementStatServiceImpl implements IAdvertisementStatService {

	public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");

	public static final String CLIC_CACHE_PREFIX = "adv:stat-click:";

	public static final String VIEW_CACHE_PREFIX = "adv:stat-view:";

	private final CmsAdClickLogMapper clickLogMapper;

	private final CmsAdViewLogMapper viewLogMapper;

	private final RedisCache redisCache;

	private final IAdvertisementService advService;

	@Async
	@Override
	public void adClick(CmsAdClickLog clickLog) {
		Map<String, String> advMap = advService.getAdvertisementMap();
		if (Objects.isNull(advMap) || Objects.isNull(clickLog.getAdId())
				|| !advMap.containsKey(clickLog.getAdId().toString())) {
			log.warn("Cms adv click log err, invalid id: " + clickLog.getAdId());
			return;
		}
		// redis 广告小时点击数+1
		String cacheKey = CLIC_CACHE_PREFIX + clickLog.getEvtTime().format(DATE_TIME_FORMAT);
		redisCache.zsetIncr(cacheKey, clickLog.getAdId().toString(), 1);
		// 记录点击日志
		this.clickLogMapper.insert(clickLog);
	}

	@Async
	@Override
	public void adView(CmsAdViewLog viewLog) {
		Map<String, String> advMap = advService.getAdvertisementMap();
		if (Objects.isNull(advMap) || Objects.isNull(viewLog.getAdId())
				|| !advMap.containsKey(viewLog.getAdId().toString())) {
			log.warn("Cms adv view log err, invalid id: " + viewLog.getAdId());
			return;
		}
		// redis 广告日展现数+1
		String cacheKey = VIEW_CACHE_PREFIX + viewLog.getEvtTime().format(DATE_TIME_FORMAT);
		redisCache.zsetIncr(cacheKey, viewLog.getAdId().toString(), 1);
		// 记录展现日志
		this.viewLogMapper.insert(viewLog);
	}
}
