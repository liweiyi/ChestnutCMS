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
package com.chestnut.link.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.chestnut.link.domain.CmsLink;
import com.chestnut.link.domain.CmsLinkGroup;
import com.chestnut.link.service.ILinkGroupService;
import com.chestnut.link.service.ILinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkListener {

	private final ILinkGroupService linkGroupService;

	private final ILinkService linkService;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		try {
			// 删除友链数据
			long total = this.linkService
					.count(new LambdaQueryWrapper<CmsLink>().eq(CmsLink::getSiteId, site.getSiteId()));
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除友链数据：" + (i * pageSize) + "/" + total);
				this.linkService.remove(new LambdaQueryWrapper<CmsLink>().eq(CmsLink::getSiteId, site.getSiteId())
						.last("limit " + pageSize));
			}
			// 删除友链分组数据
			total = this.linkGroupService
					.count(new LambdaQueryWrapper<CmsLinkGroup>().eq(CmsLinkGroup::getSiteId, site.getSiteId()));
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除友链分组数据：" + (i * pageSize) + "/" + total);
				this.linkGroupService.remove(new LambdaQueryWrapper<CmsLinkGroup>()
						.eq(CmsLinkGroup::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除友链数据错误：" + e.getMessage());
			log.error("Delete friend links failed on site[{}] delete.", site.getSiteId());
		}
	}
}
