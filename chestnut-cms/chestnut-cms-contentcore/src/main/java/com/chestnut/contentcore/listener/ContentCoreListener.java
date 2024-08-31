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
package com.chestnut.contentcore.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.core.impl.InternalDataType_Content;
import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.listener.event.*;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.system.fixed.dict.YesOrNo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentCoreListener {

	private final ISiteService siteService;
	
	private final ISitePropertyService sitePropertyService;
	
	private final ICatalogService catalogService;

	private final IPublishPipeService publishPipeService;

	private final IContentService contentService;
	
	private final IResourceService resourceService;
	
	private final IPageWidgetService pageWidgetService;
	
	private final ITemplateService templateService;
	
	private final IContentRelaService contentRelaService;

	private final AsyncTaskManager asyncTaskManager;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		// 删除发布通道
		this.publishPipeService.deletePublishPipeBySite(site);
		// 删除页面部件
		AsyncTaskManager.setTaskProgressInfo(90, "正在删除页面部件数据");
		this.pageWidgetService.remove(new LambdaQueryWrapper<CmsPageWidget>().eq(CmsPageWidget::getSiteId, site.getSiteId()));
		// 删除内容数据
		try {
			long total = this.contentService.dao().countBySiteId(site.getSiteId());
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除内容数据：" + (i * pageSize) + "/" + total);
				List<Long> contentIds = this.contentService.dao()
						.pageBySiteId(
								new Page<>(0, pageSize, false),
								site.getSiteId(),
								List.of(CmsContent::getContentId)
						).getRecords().stream().map(CmsContent::getContentId).toList();
				this.contentService.dao().removeBatchByIds(contentIds);
			}
			// 删除备份内容数据
			total = this.contentService.dao().countBackupBySiteId(site.getSiteId());
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除内容备份数据：" + (i * pageSize) + "/" + total);
				List<Long> backupIds = this.contentService.dao()
						.pageBackupBySiteId(
								new Page<>(0, pageSize, false),
								site.getSiteId(),
								List.of(BCmsContent::getContentId)
						).getRecords().stream().map(BCmsContent::getBackupId).toList();
				this.contentService.dao().removeBackupBatchByIds(backupIds);
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除内容错误：" + e.getMessage());
			log.error("Delete cms_content failed on site[{}] delete.", site.getSiteId(), e);
		}
		// 删除资源数据
		try {
			long total = this.resourceService
					.count(new LambdaQueryWrapper<CmsResource>().eq(CmsResource::getSiteId, site.getSiteId()));
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除资源数据：" + (i * pageSize) + "/" + total);
				this.resourceService.remove(new LambdaQueryWrapper<CmsResource>()
						.eq(CmsResource::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除资源数据错误：" + e.getMessage());
			log.error("Delete cms_resource failed on site[{}] delete.", site.getSiteId(), e);
		}
		// 删除栏目
		try {
			long total = this.catalogService
					.count(new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId, site.getSiteId()));
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除栏目数据：" + (i * pageSize) + "/" + total);
				this.catalogService.remove(new LambdaQueryWrapper<CmsCatalog>()
						.eq(CmsCatalog::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除栏目数据错误：" + e.getMessage());
			log.error("Delete cms_resource failed on site[{}] delete.", site.getSiteId(), e);
		}
		// 删除站点扩展属性
		try {
			long total = this.sitePropertyService
					.count(new LambdaQueryWrapper<CmsSiteProperty>().eq(CmsSiteProperty::getSiteId, site.getSiteId()));
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除站点扩展属性数据：" + (i * pageSize) + "/" + total);
				this.sitePropertyService.remove(new LambdaQueryWrapper<CmsSiteProperty>()
						.eq(CmsSiteProperty::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除站点扩展属性错误：" + e.getMessage());
			log.error("Delete site properties failed on site[{}] delete.", site.getSiteId(), e);
		}
		// 删除模板数据
		try {
			long total = this.templateService
					.count(new LambdaQueryWrapper<CmsTemplate>().eq(CmsTemplate::getSiteId, site.getSiteId()));
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除模板数据：" + (i * pageSize) + "/" + total);
				this.templateService.remove(new LambdaQueryWrapper<CmsTemplate>()
						.eq(CmsTemplate::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除模板数据错误：" + e.getMessage());
			log.error("Delete templates failed on site[{}] delete.", site.getSiteId(), e);
		}
		// 删除内容关联表数据
		try {
			long total = this.contentRelaService
					.count(new LambdaQueryWrapper<CmsContentRela>().eq(CmsContentRela::getSiteId, site.getSiteId()));
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在内容关联表数据：" + (i * pageSize) + "/" + total);
				this.contentRelaService.remove(new LambdaQueryWrapper<CmsContentRela>()
						.eq(CmsContentRela::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除内容关联表数据错误：" + e.getMessage());
			log.error("Delete content relatives failed on site[{}] delete.", site.getSiteId(), e);
		}
	}

	@EventListener
	public void afterSiteDelete(AfterSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		this.publishPipeService.deletePublishPipeBySite(site);
	}

	@EventListener
	public void afterCatalogSave(AfterCatalogSaveEvent event) {
		CmsCatalog catalog = event.getCatalog();
		// 栏目路径变更，各发布通道路径变更
		if (!event.getOldPath().equals(catalog.getPath())) {
			List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
			if (!publishPipes.isEmpty()) {
				CmsSite site = this.siteService.getSite(catalog.getSiteId());
				for (CmsPublishPipe publishPipe : publishPipes) {
					String siteRoot = SiteUtils.getSiteRoot(site, publishPipe.getCode());
					String from = siteRoot + event.getOldPath();
					String to = siteRoot + catalog.getPath();
					try {
						Files.move(Paths.get(from), Paths.get(to), StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						log.error("Move {} to {} failed!", from, to);
					}
				}
			}
		}
	}

	@EventListener
	public void beforeCatalogDelete(BeforeCatalogDeleteEvent event) {
		CmsCatalog catalog = event.getCatalog();
		// 删除栏目内容
		this.contentService.deleteContentsByCatalog(catalog, event.getOperator());
		// 删除页面部件
		this.pageWidgetService.deletePageWidgetsByCatalog(catalog);
	}

	@EventListener
	public void afterContentOffline(AfterContentOfflineEvent event) {
		final Long contentId = event.getContent().getContentEntity().getContentId();
		final String operator = event.getContent().getOperatorUName();
		asyncTaskManager.execute(() -> {
			// 映射关联内容同步下线
			List<CmsContent> mappingList = contentService.dao().lambdaQuery()
					.gt(CmsContent::getCopyType, ContentCopyType.Mapping)
					.eq(CmsContent::getCopyId, contentId).list();
			for (CmsContent c : mappingList) {
				if (ContentStatus.PUBLISHED.equals(c.getStatus())) {
					contentService.deleteStaticFiles(c);
				}
				c.setStatus(ContentStatus.OFFLINE);
				c.updateBy(operator);
			}
			contentService.dao().updateBatchById(mappingList);
			// 标题内容同步下线
			String internalUrl = InternalUrlUtils.getInternalUrl(InternalDataType_Content.ID, contentId);
			List<CmsContent> linkList = contentService.dao().lambdaQuery().eq(CmsContent::getLinkFlag, YesOrNo.YES)
					.eq(CmsContent::getRedirectUrl, internalUrl).list();
			for (CmsContent c : linkList) {
				c.setStatus(ContentStatus.OFFLINE);
				c.updateBy(operator);
			}
			mappingList.addAll(linkList);
			contentService.dao().updateBatchById(mappingList);
		});
	}
}
