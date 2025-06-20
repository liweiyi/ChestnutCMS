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
package com.chestnut.cms.stat.listener;

import com.chestnut.cms.stat.job.CatalogContentCountByStatus;
import com.chestnut.cms.stat.job.UserContentCountByStatus;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.listener.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CmsStatEventListener {

	private final CatalogContentCountByStatus catalogContentCountByStatus;

	private final UserContentCountByStatus userContentCountByStatus;

	private void triggerStatUpdate(CmsContent content) {
		// 更新栏目内容统计数据
		catalogContentCountByStatus.triggerChange(content.getCatalogId());
		if (!IdUtils.validate(content.getContributorId())) {
			// 更新用户内容统计数据
			userContentCountByStatus.triggerChange(content.getCreateBy(), content.getSiteId());
		}
	}

	@EventListener
	public void afterContentSave(AfterContentSaveEvent event) {
		triggerStatUpdate(event.getContent().getContentEntity());
	}

	@EventListener
	public void afterContentDelete(AfterContentDeleteEvent event) {
		triggerStatUpdate(event.getContent().getContentEntity());
	}

	@EventListener
	public void afterContentToPublish(AfterContentToPublishEvent event) {
		triggerStatUpdate(event.getContent().getContentEntity());
	}

	@EventListener
	public void afterContentPublish(AfterContentPublishEvent event) {
		triggerStatUpdate(event.getContent().getContentEntity());
	}

	@EventListener
	public void afterContentOffline(AfterContentOfflineEvent event) {
		triggerStatUpdate(event.getContent().getContentEntity());
	}
}
