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
package com.chestnut.cms.stat.listener;

import com.chestnut.cms.stat.domain.CmsCatalogContentStat;
import com.chestnut.cms.stat.domain.CmsUserContentStat;
import com.chestnut.cms.stat.domain.vo.ContentStatusTotal;
import com.chestnut.cms.stat.mapper.CmsCatalogContentStatMapper;
import com.chestnut.cms.stat.mapper.CmsUserContentStatMapper;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.listener.event.*;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CmsStatEventListener {

	private static final Logger logger = LoggerFactory.getLogger(CmsStatEventListener.class);

	private final ISysUserService userService;

	private final AsyncTaskManager asyncTaskManager;

	private final CmsCatalogContentStatMapper catalogContentStatMapper;

	private final CmsUserContentStatMapper userContentStatMapper;

	@EventListener
	public void afterContentSave(AfterContentSaveEvent event) {
		if (!IdUtils.validate(event.getContent().getContentEntity().getContributorId())) {
			// 更新栏目内容统计数据
			this.updateCatalogContentStat(event.getContent().getSiteId(), event.getContent().getCatalogId());
			// 更新用户内容统计数据
			Optional<SysUser> opt = this.userService.lambdaQuery().eq(SysUser::getUserName, event.getContent().getContentEntity().getCreateBy()).oneOpt();
			opt.ifPresent(user -> updateUserContentStat(event.getContent().getSiteId(), user));
		}
	}

	@EventListener
	public void afterContentDelete(AfterContentDeleteEvent event) {
		if (!IdUtils.validate(event.getContent().getContentEntity().getContributorId())) {
			// 更新栏目内容统计数据
			this.updateCatalogContentStat(event.getContent().getSiteId(), event.getContent().getCatalogId());
			// 更新用户内容统计数据
			Optional<SysUser> opt = this.userService.lambdaQuery().eq(SysUser::getUserName, event.getContent().getContentEntity().getCreateBy()).oneOpt();
			opt.ifPresent(user -> updateUserContentStat(event.getContent().getSiteId(), user));
		}
	}

	@EventListener
	public void afterContentToPublish(AfterContentToPublishEvent event) {
		if (!IdUtils.validate(event.getContent().getContentEntity().getContributorId())) {
			// 更新栏目内容统计数据
			this.updateCatalogContentStat(event.getContent().getSiteId(), event.getContent().getCatalogId());
			// 更新用户内容统计数据
			Optional<SysUser> opt = this.userService.lambdaQuery().eq(SysUser::getUserName, event.getContent().getContentEntity().getCreateBy()).oneOpt();
			opt.ifPresent(user -> updateUserContentStat(event.getContent().getSiteId(), user));
		}
	}

	@EventListener
	public void afterContentPublish(AfterContentPublishEvent event) {
		if (!IdUtils.validate(event.getContent().getContentEntity().getContributorId())) {
			// 更新栏目内容统计数据
			this.updateCatalogContentStat(event.getContent().getSiteId(), event.getContent().getCatalogId());
			// 更新用户内容统计数据
			Optional<SysUser> opt = this.userService.lambdaQuery().eq(SysUser::getUserName, event.getContent().getContentEntity().getCreateBy()).oneOpt();
			opt.ifPresent(user -> updateUserContentStat(event.getContent().getSiteId(), user));
		}
	}

	@EventListener
	public void afterContentOffline(AfterContentOfflineEvent event) {
		if (!IdUtils.validate(event.getContent().getContentEntity().getContributorId())) {
			// 更新栏目内容统计数据
			this.updateCatalogContentStat(event.getContent().getSiteId(), event.getContent().getCatalogId());
			// 更新用户内容统计数据
			Optional<SysUser> opt = this.userService.lambdaQuery().eq(SysUser::getUserName, event.getContent().getContentEntity().getCreateBy()).oneOpt();
			opt.ifPresent(user -> updateUserContentStat(event.getContent().getSiteId(), user));
		}
	}

	private void updateCatalogContentStat(Long siteId, Long catalogId) {
		asyncTaskManager.execute(() -> {
			long s = System.currentTimeMillis();
			Map<String, Integer> dataMap = catalogContentStatMapper.statContentByStatus(catalogId)
					.stream().collect(Collectors.toMap(ContentStatusTotal::getStatus, ContentStatusTotal::getTotal));

			CmsCatalogContentStat stat = catalogContentStatMapper.selectById(catalogId);
			if (Objects.isNull(stat)) {
				stat = new CmsCatalogContentStat();
				stat.setCatalogId(catalogId);
				stat.setSiteId(siteId);
				stat.setDraftTotal(dataMap.get(ContentStatus.DRAFT));
				stat.setToPublishTotal(dataMap.get(ContentStatus.TO_PUBLISHED));
				stat.setPublishedTotal(dataMap.get(ContentStatus.PUBLISHED));
				stat.setOfflineTotal(dataMap.get(ContentStatus.OFFLINE));
				stat.setEditingTotal(dataMap.get(ContentStatus.EDITING));
				catalogContentStatMapper.insert(stat);
			} else {
				stat.setDraftTotal(dataMap.get(ContentStatus.DRAFT));
				stat.setToPublishTotal(dataMap.get(ContentStatus.TO_PUBLISHED));
				stat.setPublishedTotal(dataMap.get(ContentStatus.PUBLISHED));
				stat.setOfflineTotal(dataMap.get(ContentStatus.OFFLINE));
				stat.setEditingTotal(dataMap.get(ContentStatus.EDITING));
				catalogContentStatMapper.updateById(stat);
			}
			logger.info("Stat catalog content by status cost: " + (System.currentTimeMillis() - s) + " ms");
		});
	}

	private void updateUserContentStat(Long siteId, SysUser user) {
		asyncTaskManager.execute(() -> {
			long s = System.currentTimeMillis();
			Map<String, Integer> dataMap = userContentStatMapper.statContentByStatus(siteId, user.getUserName())
					.stream().collect(Collectors.toMap(ContentStatusTotal::getStatus, ContentStatusTotal::getTotal));

			String statId = siteId + "-" + user.getUserId();
			CmsUserContentStat stat = userContentStatMapper.selectById(statId);
			if (Objects.isNull(stat)) {
				stat = new CmsUserContentStat();
				stat.setId(statId);
				stat.setUserId(user.getUserId());
				stat.setUserName(user.getUserName());
				stat.setSiteId(siteId);
				stat.setDraftTotal(dataMap.get(ContentStatus.DRAFT));
				stat.setToPublishTotal(dataMap.get(ContentStatus.TO_PUBLISHED));
				stat.setPublishedTotal(dataMap.get(ContentStatus.PUBLISHED));
				stat.setOfflineTotal(dataMap.get(ContentStatus.OFFLINE));
				stat.setEditingTotal(dataMap.get(ContentStatus.EDITING));
				userContentStatMapper.insert(stat);
			} else {
				stat.setDraftTotal(dataMap.get(ContentStatus.DRAFT));
				stat.setToPublishTotal(dataMap.get(ContentStatus.TO_PUBLISHED));
				stat.setPublishedTotal(dataMap.get(ContentStatus.PUBLISHED));
				stat.setOfflineTotal(dataMap.get(ContentStatus.OFFLINE));
				stat.setEditingTotal(dataMap.get(ContentStatus.EDITING));
				userContentStatMapper.updateById(stat);
			}
			logger.info("Stat user content by status cost: " + (System.currentTimeMillis() - s) + " ms");
		});
	}
}
