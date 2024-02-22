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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.advertisement.AdSpacePageWidgetType;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.contentcore.core.IPageWidgetType;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.fixed.dict.PageWidgetStatus;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.contentcore.service.IPublishService;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.schedule.IScheduledHandler;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 广告定时发布下线任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + AdvertisementPublishJob.JOB_NAME)
public class AdvertisementPublishJob extends IJobHandler implements IScheduledHandler {

	static final String JOB_NAME = "AdvertisementPublishJob";

	private final IPageWidgetService pageWidgetService;

	private final IAdvertisementService advertisementService;
	
	private final IPublishService publishService;

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
		LocalDateTime now = LocalDateTime.now();
		List<CmsPageWidget> list = this.pageWidgetService.list(new LambdaQueryWrapper<CmsPageWidget>()
				.eq(CmsPageWidget::getState, PageWidgetStatus.PUBLISHED)
				.eq(CmsPageWidget::getType, AdSpacePageWidgetType.ID));
		for (CmsPageWidget adSpace : list) {
			boolean changed = false;
			List<CmsAdvertisement> toOnlineList = this.advertisementService.list(new LambdaQueryWrapper<CmsAdvertisement>()
					.eq(CmsAdvertisement::getState, EnableOrDisable.DISABLE)
					.eq(CmsAdvertisement::getAdSpaceId, adSpace.getPageWidgetId())
					.le(CmsAdvertisement::getOnlineDate, now)
					.ge(CmsAdvertisement::getOfflineDate, now));
			if (toOnlineList != null && toOnlineList.size() > 0) {
				changed = true;
				for (CmsAdvertisement ad : toOnlineList) {
					ad.setState(EnableOrDisable.ENABLE);
				}
				this.advertisementService.updateBatchById(toOnlineList);
			}
			// 下线时间小于当前时间的启用广告标记为停用
			List<CmsAdvertisement> toOfflineList = this.advertisementService.list(new LambdaQueryWrapper<CmsAdvertisement>()
					.eq(CmsAdvertisement::getState, EnableOrDisable.ENABLE)
					.eq(CmsAdvertisement::getAdSpaceId, adSpace.getPageWidgetId())
					.lt(CmsAdvertisement::getOfflineDate, now));
			if (toOfflineList != null && toOfflineList.size() > 0) {
				changed = true;
				for (CmsAdvertisement ad : toOfflineList) {
					ad.setState(EnableOrDisable.DISABLE);
				}
				this.advertisementService.updateBatchById(toOfflineList);
			}
			// 有变化重新发布广告版位
			if (changed) {
				IPageWidgetType pwt = this.pageWidgetService.getPageWidgetType(adSpace.getType());
				this.publishService.pageWidgetStaticize(pwt.loadPageWidget(adSpace));
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
