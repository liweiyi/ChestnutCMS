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
package com.chestnut.contentcore.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.listener.event.*;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.util.CatalogUtils;
import com.chestnut.contentcore.util.SiteUtils;
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

	private final IPublishService publishService;

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
			long lastId = 0;
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除内容数据：" + (i * pageSize) + "/" + total);
				Page<CmsContent> page = this.contentService.dao().lambdaQuery()
						.select(CmsContent::getContentId)
						.eq(CmsContent::getSiteId, site.getSiteId())
						.gt(CmsContent::getContentId, lastId)
						.orderByAsc(CmsContent::getContentId)
						.page(Page.of(0, pageSize, false));
				if (!page.getRecords().isEmpty()) {
					List<Long> contentIds = page.getRecords().stream().map(CmsContent::getContentId).toList();
					this.contentService.dao().removeBatchByIds(contentIds);
					lastId = contentIds.get(contentIds.size() - 1);
				}
			}
			// 删除备份内容数据
			total = this.contentService.dao().countBackupBySiteId(site.getSiteId());
			lastId = 0;
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除内容备份数据：" + (i * pageSize) + "/" + total);
				Page<BCmsContent> page = this.contentService.dao().getBackupMapper()
						.selectPage(Page.of(0, pageSize, false), new LambdaQueryWrapper<BCmsContent>()
								.select(BCmsContent::getBackupId)
								.eq(BCmsContent::getSiteId, site.getSiteId())
								.gt(BCmsContent::getBackupId, lastId)
								.orderByAsc(BCmsContent::getBackupId));
				if (!page.getRecords().isEmpty()) {
					List<Long> backupIds = page.getRecords().stream().map(BCmsContent::getBackupId).toList();
					this.contentService.dao().removeBackupBatchByIds(backupIds);
					lastId = backupIds.get(backupIds.size() - 1);
				}
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除内容错误：" + e.getMessage());
			log.error("Delete cms_content failed on site[{}] delete.", site.getSiteId(), e);
		}
		// 删除资源数据
		try {
			long total = this.resourceService
					.count(new LambdaQueryWrapper<CmsResource>().eq(CmsResource::getSiteId, site.getSiteId()));
			long lastId = 0;
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除资源数据：" + (i * pageSize) + "/" + total);
				Page<CmsResource> resources = this.resourceService.lambdaQuery()
						.select(CmsResource::getResourceId)
						.eq(CmsResource::getSiteId, site.getSiteId())
						.gt(CmsResource::getResourceId, lastId)
						.orderByAsc(CmsResource::getResourceId)
						.page(Page.of(0, pageSize, false));
				if (!resources.getRecords().isEmpty()) {
					List<Long> resourceIds = resources.getRecords().stream().map(CmsResource::getResourceId).toList();
					this.resourceService.removeBatchByIds(resourceIds);
					lastId = resourceIds.get(resourceIds.size() - 1);
				}
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除资源数据错误：" + e.getMessage());
			log.error("Delete cms_resource failed on site[{}] delete.", site.getSiteId(), e);
		}
		// 删除栏目
		try {
			long total = this.catalogService
					.count(new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId, site.getSiteId()));
			long lastId = 0;
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除栏目数据：" + (i * pageSize) + "/" + total);
				Page<CmsCatalog> catalogs = this.catalogService.lambdaQuery()
						.select(CmsCatalog::getCatalogId)
						.eq(CmsCatalog::getSiteId, site.getSiteId())
						.gt(CmsCatalog::getCatalogId, lastId)
						.orderByAsc(CmsCatalog::getCatalogId)
						.page(Page.of(0, pageSize, false));
				if (!catalogs.getRecords().isEmpty()) {
					List<Long> catalogIds = catalogs.getRecords().stream().map(CmsCatalog::getCatalogId).toList();
					this.catalogService.removeBatchByIds(catalogIds);
					lastId = catalogIds.get(catalogIds.size() - 1);
				}
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除栏目数据错误：" + e.getMessage());
			log.error("Delete cms_resource failed on site[{}] delete.", site.getSiteId(), e);
		}
		// 删除站点扩展属性
		try {
			long total = this.sitePropertyService
					.count(new LambdaQueryWrapper<CmsSiteProperty>().eq(CmsSiteProperty::getSiteId, site.getSiteId()));
			long lastId = 0;
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除站点扩展属性数据：" + (i * pageSize) + "/" + total);
				Page<CmsSiteProperty> siteProperties = this.sitePropertyService.lambdaQuery()
						.select(CmsSiteProperty::getPropertyId)
						.eq(CmsSiteProperty::getSiteId, site.getSiteId())
						.gt(CmsSiteProperty::getPropertyId, lastId)
						.orderByAsc(CmsSiteProperty::getPropertyId)
						.page(Page.of(0, pageSize, false));
				if (!siteProperties.getRecords().isEmpty()) {
					List<Long> ids = siteProperties.getRecords().stream().map(CmsSiteProperty::getPropertyId).toList();
					this.sitePropertyService.removeBatchByIds(ids);
					lastId = ids.get(ids.size() - 1);
				}
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除站点扩展属性错误：" + e.getMessage());
			log.error("Delete site properties failed on site[{}] delete.", site.getSiteId(), e);
		}
		// 删除模板数据
		try {
			long total = this.templateService
					.count(new LambdaQueryWrapper<CmsTemplate>().eq(CmsTemplate::getSiteId, site.getSiteId()));
			long lastId = 0;
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除模板数据：" + (i * pageSize) + "/" + total);
				Page<CmsTemplate> templates = this.templateService.lambdaQuery()
						.select(CmsTemplate::getTemplateId)
						.eq(CmsTemplate::getSiteId, site.getSiteId())
						.gt(CmsTemplate::getTemplateId, lastId)
						.orderByAsc(CmsTemplate::getTemplateId)
						.page(Page.of(0, pageSize, false));
				if (!templates.getRecords().isEmpty()) {
					List<Long> ids = templates.getRecords().stream().map(CmsTemplate::getTemplateId).toList();
					this.templateService.removeBatchByIds(ids);
					lastId = ids.get(ids.size() - 1);
				}
			}
		} catch (Exception e) {
			AsyncTaskManager.addErrMessage("删除模板数据错误：" + e.getMessage());
			log.error("Delete templates failed on site[{}] delete.", site.getSiteId(), e);
		}
		// 删除内容关联表数据
		try {
			long total = this.contentRelaService
					.count(new LambdaQueryWrapper<CmsContentRela>().eq(CmsContentRela::getSiteId, site.getSiteId()));
			long lastId = 0;
			for (long i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total),
						"正在内容关联表数据：" + (i * pageSize) + "/" + total);
				Page<CmsContentRela> templates = this.contentRelaService.lambdaQuery()
						.select(CmsContentRela::getRelaContentId)
						.eq(CmsContentRela::getSiteId, site.getSiteId())
						.gt(CmsContentRela::getRelaContentId, lastId)
						.orderByAsc(CmsContentRela::getRelaContentId)
						.page(Page.of(0, pageSize, false));
				if (!templates.getRecords().isEmpty()) {
					List<Long> ids = templates.getRecords().stream().map(CmsContentRela::getRelaContentId).toList();
					this.contentRelaService.removeBatchByIds(ids);
					lastId = ids.get(ids.size() - 1);
				}
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
		this.contentService.deleteContentsByCatalog(catalog, false, event.getOperator());
		// 删除页面部件
		this.pageWidgetService.deletePageWidgetsByCatalog(catalog);
	}

	@EventListener
	public void afterContentOffline(AfterContentOfflineEvent event) {
		// 重新发布内容所在栏目和父级栏目
		String[] catalogIds = event.getContent().getContentEntity().getCatalogAncestors()
				.split(CatalogUtils.ANCESTORS_SPLITER);
		for (String catalogId : catalogIds) {
			this.publishService.publishCatalog(this.catalogService.getCatalog(Long.valueOf(catalogId)),
					false, false, null, event.getContent().getOperator());
		}
	}

	@EventListener
	public void onCatalogClear(OnCatalogClearEvent event) {
		CmsCatalog catalog = event.getCatalog();
		// 删除栏目内容
		this.contentService.deleteContentsByCatalog(catalog, false, event.getOperator());
		// 删除页面部件
		this.pageWidgetService.deletePageWidgetsByCatalog(catalog);
	}

	@EventListener
	public void onCatalogMergeEvent(OnCatalogMergeEvent event) {
		CmsCatalog targetCatalog = event.getTargetCatalog();
		int pageSize = 200;
		for (CmsCatalog mergeCatalog : event.getMergeCatalogs()) {
			long lastContentId = 0;
			long count = 0;
			long total = contentService.dao().lambdaQuery()
					.eq(CmsContent::getCatalogId, mergeCatalog.getCatalogId())
					.count();
			if (total == 0) {
				continue;
			}
			while (true) {
				LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
						.eq(CmsContent::getCatalogId, mergeCatalog.getCatalogId())
						.gt(CmsContent::getContentId, lastContentId)
						.orderByAsc(CmsContent::getContentId);
				Page<CmsContent> page = contentService.dao().page(new Page<>(0, pageSize, false), q);
				if (!page.getRecords().isEmpty()) {
					for (CmsContent content : page.getRecords()) {
						AsyncTaskManager.setTaskProgressInfo((int) (count * 100 / total),
								"正在合并内容：" + mergeCatalog.getName() + "[" + count + " / " + total + "]");
						this.contentService.moveContent(content, targetCatalog, event.getOperator());
						count++;
					}
					lastContentId = page.getRecords().get(page.getRecords().size() - 1).getContentId();
				}
				if (page.getRecords().size() < pageSize) {
					break;
				}
			}
		}
	}

	@EventListener
	public void afterContentPublish(AfterContentPublishEvent event) {
		// 静态化
		this.publishService.asyncStaticizeContent(event.getContent());
	}

	@EventListener
	public void afterContentSetTopEvent(AfterContentTopSetEvent event) {
		if (ContentStatus.isPublished(event.getContent().getContentEntity().getStatus())) {
			this.publishService.publishContent(event.getContent().getContentEntity(), event.getContent().getOperator());
		}
	}

	@EventListener
	public void afterContentTopCancelEvent(AfterContentTopCancelEvent event) {
		if (ContentStatus.isPublished(event.getContent().getContentEntity().getStatus())) {
			this.publishService.publishContent(event.getContent().getContentEntity(), event.getContent().getOperator());
		}
	}
}
