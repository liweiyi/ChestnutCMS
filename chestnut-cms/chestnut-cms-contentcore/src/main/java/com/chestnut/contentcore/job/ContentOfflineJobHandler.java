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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.system.schedule.IScheduledHandler;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 内容定时下线任务<br/>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + ContentOfflineJobHandler.JOB_NAME)
public class ContentOfflineJobHandler extends IJobHandler implements IScheduledHandler {
	
	static final String JOB_NAME = "ContentOfflineJobHandler";
	
	private final IContentService contentService;

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

		long pageSize = 500;
		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
				.select(CmsContent::getContentId)
				.eq(CmsContent::getStatus, ContentStatus.PUBLISHED)
				.le(CmsContent::getOfflineDate, LocalDateTime.now());
		long total = contentService.dao().count(q);
		for (int i = 0; i * pageSize < total; i++) {
			Page<CmsContent> page = contentService.dao().page(new Page<>(i, pageSize, false), q);
			for (CmsContent xContent : page.getRecords()) {
				contentService.offline(xContent, null);
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
