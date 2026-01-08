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
package com.chestnut.contentcore.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.security.domain.Operator;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.publish.IPublishStrategy;
import com.chestnut.contentcore.publish.staticize.CatalogStaticizeType;
import com.chestnut.contentcore.publish.staticize.SiteStaticizeType;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.IPublishService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.system.schedule.IScheduledHandler;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	private final IPublishStrategy publishStrategy;

    private final IPublishService publishService;

    private final TransactionTemplate transactionTemplate;

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
		Set<Long> catalogIds = new HashSet<>();
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
				long total = contentService.dao().count(q);
				for (int i = 0; i * pageSize < total; i++) {
					Page<CmsContent> page = contentService.dao().page(new Page<>(i, pageSize, false), q);
					for (CmsContent xContent : page.getRecords()) {
                        IContentType contentType = ContentCoreUtils.getContentType(xContent.getContentType());
                        IContent<?> content = contentType.loadContent(xContent);
                        content.setOperator(Operator.defalutOperator());
                        transactionTemplate.execute(callback -> content.publish());
                        publishService.asyncStaticizeContent(content);
					}
				}
				if (total > 0) {
					catalogIds.add(catalog.getCatalogId());
					// 发布关联栏目：内容所属栏目及其所有父级栏目
					long parentId = catalog.getParentId();
					while (parentId > 0) {
						CmsCatalog parent = catalogService.getCatalog(parentId);
						if (parent == null) {
							break;
						}
						catalogIds.add(parent.getCatalogId());
						parentId = parent.getParentId();
					}
				}
			}
			// 发布相关栏目
			catalogIds.forEach(catalogId -> {
				publishStrategy.publish(CatalogStaticizeType.TYPE, catalogId.toString());
			});
			// 发布站点首页
			publishStrategy.publish(SiteStaticizeType.TYPE, site.getSiteId().toString());
		}
		logger.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		this.exec();
	}
}
