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
package com.chestnut.contentcore.job;

import com.chestnut.contentcore.cache.CatalogMonitoredCache;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.fixed.config.MaxCatalogPagePublishTask;
import com.chestnut.contentcore.publish.IPublishStrategy;
import com.chestnut.contentcore.publish.staticize.CatalogListStaticizeType;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.system.schedule.IScheduledHandler;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 栏目列表延迟发布任务<br/>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + CatalogLazyPublishJobHandler.JOB_NAME)
public class CatalogLazyPublishJobHandler extends IJobHandler implements IScheduledHandler {
	
	static final String JOB_NAME = "CatalogLazyPublishJobHandler";

	private final ICatalogService catalogService;

	private final CatalogMonitoredCache catalogMonitoredCache;

	private final IPublishStrategy publishStrategy;

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
		int max = MaxCatalogPagePublishTask.getValue();
		List<Long> catalogIds = catalogMonitoredCache.getCatalogPublishingKeys();
		while(max > 0 && !catalogIds.isEmpty()) {
			for (Long catalogId : catalogIds) {
				logger.info("Add catalog publish task: {}", catalogId);
				CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
				publishStrategy.publish(CatalogListStaticizeType.TYPE, catalog.getCatalogId().toString());
				if (--max <= 0) {
					break; // 最多只提交max个发布任务
				}
			}
		}
		logger.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		this.exec();
	}
}
