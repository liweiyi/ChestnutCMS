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
package com.chestnut.advertisement.service.impl;

import com.chestnut.advertisement.domain.CmsAdClickLog;
import com.chestnut.advertisement.domain.CmsAdViewLog;
import com.chestnut.advertisement.mapper.CmsAdClickLogMapper;
import com.chestnut.advertisement.mapper.CmsAdViewLogMapper;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.advertisement.service.IAdvertisementStatService;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.redis.RedisCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertisementStatServiceImpl implements IAdvertisementStatService {

	public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");

	public static final String CLICK_CACHE_PREFIX = "adv:stat-click:";

	public static final String VIEW_CACHE_PREFIX = "adv:stat-view:";

	private final CmsAdClickLogMapper clickLogMapper;

	private final CmsAdViewLogMapper viewLogMapper;

	private final RedisCache redisCache;

	private final IAdvertisementService advService;

	private final AsyncTaskManager asyncTaskManager;

	@Override
	public void adClick(CmsAdClickLog clickLog) {
		Map<String, String> advMap = advService.getAdvertisementMap();
		if (Objects.isNull(advMap) || Objects.isNull(clickLog.getAdId())
				|| !advMap.containsKey(clickLog.getAdId().toString())) {
			if (log.isDebugEnabled()) {
				log.debug("Cms adv click log err, invalid id: " + clickLog.getAdId());
			}
			return;
		}
		this.asyncTaskManager.execute(() -> {
			// redis 广告小时点击数+1
			String cacheKey = CLICK_CACHE_PREFIX + clickLog.getEvtTime().format(DATE_TIME_FORMAT);
			redisCache.zsetIncr(cacheKey, clickLog.getAdId().toString(), 1);
			// 记录点击日志
			this.clickLogMapper.insert(clickLog);
		});
	}

	@Override
	public void adView(CmsAdViewLog viewLog) {
		Map<String, String> advMap = advService.getAdvertisementMap();
		if (Objects.isNull(advMap) || Objects.isNull(viewLog.getAdId())
				|| !advMap.containsKey(viewLog.getAdId().toString())) {
			if (log.isDebugEnabled()) {
				log.debug("Cms adv view log err, invalid id: " + viewLog.getAdId());
			}
			return;
		}
		this.asyncTaskManager.execute(() -> {
			// redis 广告日展现数+1
			String cacheKey = VIEW_CACHE_PREFIX + viewLog.getEvtTime().format(DATE_TIME_FORMAT);
			redisCache.zsetIncr(cacheKey, viewLog.getAdId().toString(), 1);
			// 记录展现日志
			this.viewLogMapper.insert(viewLog);
		});
	}
}
