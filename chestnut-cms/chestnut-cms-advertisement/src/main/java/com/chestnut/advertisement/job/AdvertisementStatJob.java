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
package com.chestnut.advertisement.job;

import com.chestnut.advertisement.domain.CmsAdHourStat;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.advertisement.service.IAdHourStatService;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.advertisement.service.impl.AdvertisementStatServiceImpl;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.system.schedule.IScheduledHandler;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 广告统计任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + AdvertisementStatJob.JOB_NAME)
public class AdvertisementStatJob extends IJobHandler implements IScheduledHandler {

	static final String JOB_NAME = "AdvertisementStatJob";

	private final IAdHourStatService adStatService;

	private final IAdvertisementService advertisementService;

	private final RedisCache redisCache;

	@Override
	public String getId() {
		return JOB_NAME;
	}

	@Override
	public String getName() {
		return "{SCHEDULED_TASK." + JOB_NAME + "}";
	}

	@Override
	public void exec() throws Exception {
		logger.info("Job start: {}", JOB_NAME);
		long s = System.currentTimeMillis();
        // 数据更新
        String hour = LocalDateTime.now().format(AdvertisementStatServiceImpl.DATE_TIME_FORMAT);
        this.saveToDb(hour, false);
        // 尝试更新上一个小时数据并删除cache
        String yesterday = LocalDateTime.now().minusHours(1).format(AdvertisementStatServiceImpl.DATE_TIME_FORMAT);
        this.saveToDb(yesterday, true);
        logger.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}

	private void saveToDb(String hour, boolean deleteCache) {
		String clickCacheKey = AdvertisementStatServiceImpl.CLICK_CACHE_PREFIX + hour;
		String viewCacheKey = AdvertisementStatServiceImpl.VIEW_CACHE_PREFIX + hour;

		Map<Long, CmsAdHourStat> stats = this.adStatService.lambdaQuery().eq(CmsAdHourStat::getHour, hour).list()
				.stream().collect(Collectors.toMap(CmsAdHourStat::getAdvertisementId, stat -> stat));

		Map<Long, Long> advertisements = advertisementService.lambdaQuery()
				.select(List.of(CmsAdvertisement::getAdvertisementId, CmsAdvertisement::getSiteId)).list().stream()
				.collect(Collectors.toMap(CmsAdvertisement::getAdvertisementId, CmsAdvertisement::getSiteId));

		List<Long> insertAdvIds = new ArrayList<>();
		for (Long advertisementId : advertisements.keySet()) {
			int click = this.redisCache.getZsetScore(clickCacheKey, advertisementId.toString()).intValue();
			int view = this.redisCache.getZsetScore(viewCacheKey, advertisementId.toString()).intValue();
			if (click > 0 || view > 0) {
				CmsAdHourStat stat = stats.get(advertisementId);
				if (Objects.isNull(stat)) {
					stat = new CmsAdHourStat();
					stat.setSiteId(advertisements.get(advertisementId));
					stat.setHour(hour);
					stat.setAdvertisementId(advertisementId);

					stats.put(advertisementId, stat);
					insertAdvIds.add(advertisementId);
				}
				stat.setClick(Math.max(click, 0));
				stat.setView(Math.max(view, 0));
			}
		}
		// 更新数据库
		List<CmsAdHourStat> inserts = stats.values().stream()
				.filter(stat -> insertAdvIds.contains(stat.getAdvertisementId())).toList();
		this.adStatService.saveBatch(inserts);
		List<CmsAdHourStat> updates = stats.values().stream()
				.filter(stat -> !insertAdvIds.contains(stat.getAdvertisementId())).toList();
		this.adStatService.updateBatchById(updates);
		// 清理过期缓存
		if (deleteCache) {
			this.redisCache.deleteObject(clickCacheKey);
			this.redisCache.deleteObject(viewCacheKey);
		}
	}

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		this.exec();
	}
}
