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
package com.chestnut.contentcore.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.publish.CatalogPublishTask;
import com.chestnut.contentcore.publish.ContentPublishTask;
import com.chestnut.contentcore.publish.SitePublishTask;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.schedule.IScheduledHandler;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时发布任务<br/>
 * 
 * 仅发布待发布状态且发布时间为空或发布时间在当前时间之前的内容
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + SitePublishJobHandler.JOB_NAME)
public class SitePublishJobHandler extends IJobHandler implements IScheduledHandler {
	
	static final String JOB_NAME = "SitePublishJobHandler";

	private final ISiteService siteService;
	
	private final ICatalogService catalogService;
	
	private final IContentService contentService;

	private final SitePublishTask sitePublisher;

	private final CatalogPublishTask catalogPublisher;

	private final ContentPublishTask contentPublisher;

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
		List<CmsSite> sites = this.siteService.list();
		for (CmsSite site : sites) {
			List<CmsCatalog> catalogList = catalogService
					.list(new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId, site.getSiteId()));
			for (CmsCatalog catalog : catalogList) {
				// 先发布内容
				long pageSize = 500;
				LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
						.select(CmsContent::getContentId)
						.eq(CmsContent::getCatalogId, catalog.getCatalogId())
						.eq(CmsContent::getStatus, ContentStatus.TO_PUBLISHED)
						.le(CmsContent::getPublishDate, LocalDateTime.now());
				long total = contentService.count(q);
				for (int i = 0; i * pageSize < total; i++) {
					Page<CmsContent> page = contentService.page(new Page<>(i, pageSize, false), q);
					for (CmsContent xContent : page.getRecords()) {
						contentPublisher.publish(xContent);
					}
				}
			}
			// 发布栏目
			for (CmsCatalog catalog : catalogList) {
				catalogPublisher.publish(catalog);
			}
			// 发布站点
			sitePublisher.publish(site);
		}
		logger.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		this.exec();
	}
}
