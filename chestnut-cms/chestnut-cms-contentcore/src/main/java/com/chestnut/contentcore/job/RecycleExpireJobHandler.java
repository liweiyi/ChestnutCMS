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
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.domain.BCmsContent;
import com.chestnut.contentcore.properties.RecycleKeepDaysProperty;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.system.schedule.IScheduledHandler;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 回收站内容过期回收
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + RecycleExpireJobHandler.JOB_NAME)
public class RecycleExpireJobHandler extends IJobHandler implements IScheduledHandler {

	static final String JOB_NAME = "RecycleExpireJobHandler";

	private final ISiteService siteService;

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
		this.siteService.list().forEach(site -> {
			int days = RecycleKeepDaysProperty.getValue(site.getConfigProps());
			if (days > 0) {
				BaseMapper<BCmsContent> backupMapper = this.contentService.dao().getBackupMapper();
				LocalDateTime expireTime = DateUtils.getDayStart(LocalDateTime.now().minusDays(days));

				long total = backupMapper.selectCount(new LambdaQueryWrapper<BCmsContent>()
						.lt(BCmsContent::getBackupTime, expireTime));
				long pageSize = 500;
				LambdaQueryWrapper<BCmsContent> q = new LambdaQueryWrapper<BCmsContent>()
						.select(BCmsContent::getContentId, BCmsContent::getContentType)
						.lt(BCmsContent::getBackupTime, expireTime);
				for (int i = 0; i * pageSize < total; i++) {
					logger.debug("Job '{}' running: 【{}】 {} / {}", JOB_NAME, site.getName(), i * pageSize, total);
					try {
						Page<BCmsContent> page = backupMapper.selectPage(new Page<>(i, pageSize, false), q);
						page.getRecords().forEach(rc -> {
							IContentType contentType = ContentCoreUtils.getContentType(rc.getContentType());
							contentType.deleteBackups(rc.getContentId());
						});
					} catch (Exception e) {
						logger.error("Job '{}' err: " + e.getMessage(), JOB_NAME);
					}
				}
			}
		});
		logger.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		this.exec();
	}
}
