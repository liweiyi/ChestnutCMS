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
package com.chestnut.contentcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.exception.ErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.domain.Operator;
import com.chestnut.common.staticize.StaticizeService;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.IPageWidget;
import com.chestnut.contentcore.core.impl.CatalogType_Link;
import com.chestnut.contentcore.core.impl.PublishPipeProp_DefaultListTemplate;
import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.enums.ContentTips;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.dict.ContentCopyType;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.listener.event.AfterCatalogPublishEvent;
import com.chestnut.contentcore.listener.event.AfterSitePublishEvent;
import com.chestnut.contentcore.publish.IPublishStrategy;
import com.chestnut.contentcore.publish.staticize.CatalogStaticizeType;
import com.chestnut.contentcore.publish.staticize.ContentStaticizeType;
import com.chestnut.contentcore.publish.staticize.PageWidgetStaticizeType;
import com.chestnut.contentcore.publish.staticize.SiteStaticizeType;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.template.impl.CatalogTemplateType;
import com.chestnut.contentcore.template.impl.ContentTemplateType;
import com.chestnut.contentcore.template.impl.SiteTemplateType;
import com.chestnut.contentcore.util.*;
import com.chestnut.system.fixed.dict.YesOrNo;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PublishServiceImpl implements IPublishService, ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(PublishServiceImpl.class);

	private final TransactionTemplate transactionTemplate;

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IContentService contentService;

	private final ITemplateService templateService;

	private final IPublishPipeService publishPipeService;

	private final StaticizeService staticizeService;

	private final AsyncTaskManager asyncTaskManager;

	private final IPublishStrategy publishStrategy;

	private ApplicationContext applicationContext;

    private void processException(Writer writer, ErrorCode errorCode, Object... args) throws IOException {
        String error = I18nUtils.get(errorCode.value(), args);
        writer.write(error);
    }

	@Override
	public String getSitePageData(CmsSite site, IInternalDataType.RequestData requestData)
			throws IOException, TemplateException {
		try (StringWriter writer = new StringWriter()) {
			processSitePage(site, requestData, writer);
			return writer.toString();
		}
	}

	@Override
	public void processSitePage(CmsSite site, IInternalDataType.RequestData requestData, Writer writer) throws TemplateException, IOException {
		String indexTemplate = site.getIndexTemplate(requestData.getPublishPipeCode());
		File templateFile = this.templateService.findTemplateFile(site, indexTemplate, requestData.getPublishPipeCode());
		if (Objects.isNull(templateFile)) {
            this.processException(writer, ContentCoreErrorCode.TEMPLATE_FILE_NOT_FOUND, indexTemplate);
            return;
		}
		// 模板ID = 通道:站点目录:模板文件名
		String templateKey = SiteUtils.getTemplateKey(site, requestData.getPublishPipeCode(), indexTemplate);
		TemplateContext context = new TemplateContext(templateKey, requestData.isPreview(), requestData.getPublishPipeCode());
		// init template datamode
		TemplateUtils.initGlobalVariables(site, context);
		context.getVariables().put(TemplateUtils.TemplateVariable_Request, Objects.requireNonNullElse(requestData.getParams(), Map.of()));
		// init templateType data to datamode
		ITemplateType templateType = templateService.getTemplateType(SiteTemplateType.TypeId);
		templateType.initTemplateData(site.getSiteId(), context);

		long s = System.currentTimeMillis();
		try {
			this.staticizeService.process(context, writer);
		} finally {
			logger.debug("[{}]The site template parsed: {}\t, cost: {}ms", requestData.getPublishPipeCode(), site.getName(), System.currentTimeMillis() - s);
		}
	}

	@Override
	public void publishSiteIndex(CmsSite site) {
		// 发布所有通道页面
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(site.getSiteId());
		Assert.isTrue(!publishPipes.isEmpty(), ContentCoreErrorCode.NO_PUBLISHPIPE::exception);
		asyncPublishSite(site);
	}

	@Override
	public AsyncTask publishAll(CmsSite site, final String contentStatus, final Operator operator) {
		AsyncTask asyncTask = new AsyncTask() {

			@Override
			public void run0() throws InterruptedException {
				// 发布全站先清理所有模板缓存
				templateService.clearSiteAllTemplateStaticContentCache(site);

				List<CmsCatalog> catalogList = catalogService
						.list(new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId, site.getSiteId()));
				for (CmsCatalog catalog : catalogList) {
					// 先发布内容
					int pageSize = 500;
					long lastContentId = 0L;
					long total = contentService.dao().lambdaQuery().eq(CmsContent::getCatalogId, catalog.getCatalogId())
							.eq(CmsContent::getStatus, contentStatus)
							.ne(CmsContent::getLinkFlag, YesOrNo.YES)
							.count();
					int count = 1;
					while (true) {
						LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
								.eq(CmsContent::getCatalogId, catalog.getCatalogId())
								.eq(CmsContent::getStatus, contentStatus)
								.ne(CmsContent::getLinkFlag, YesOrNo.YES)
								.gt(CmsContent::getContentId, lastContentId)
								.orderByAsc(CmsContent::getContentId);
						Page<CmsContent> page = contentService.dao().page(new Page<>(0, pageSize, false), q);
						for (CmsContent xContent : page.getRecords()) {
							this.setProgressInfo((int) (count * 100 / total),
									ContentTips.BATCH_PUBLISHING_CONTENT, xContent.getTitle(), count, total);
							lastContentId = xContent.getContentId();
							IContentType contentType = ContentCoreUtils.getContentType(xContent.getContentType());
							IContent<?> content = contentType.newContent();
							content.setContentEntity(xContent);
							content.setOperator(operator);
							transactionTemplate.execute(callback -> content.publish());
                            asyncStaticizeContent(content);
							this.checkInterrupt();
							count++;
						}
						if (page.getRecords().size() < pageSize) {
							break;
						}
					}
				}
				// 发布栏目
				for (int i = 0; i < catalogList.size(); i++) {
					CmsCatalog catalog = catalogList.get(i);
					this.setProgressInfo((i * 100) / catalogList.size(), ContentTips.PUBLISHING_CATALOG, catalog.getName());
					asyncPublishCatalog(catalog);
					this.checkInterrupt(); // 允许中断
				}
				// 发布站点
				this.setProgressInfo(99, ContentTips.PUBLISHING_SITE, site.getName());
				asyncPublishSite(site);
				this.setProgressInfo(100, ContentTips.PUBLISH_SUCCESS);
			}
		};
		asyncTask.setType("Publish");
		asyncTask.setTaskId("Publish-Site-" + site.getSiteId());
		asyncTask.setInterruptible(true);
		this.asyncTaskManager.execute(asyncTask);
		return asyncTask;
	}

	private void asyncPublishSite(CmsSite site) {
		publishStrategy.publish(SiteStaticizeType.TYPE, site.getSiteId().toString());
        applicationContext.publishEvent(new AfterSitePublishEvent(this, site));
	}

	@Override
	public String getCatalogPageData(CmsCatalog catalog, IInternalDataType.RequestData requestData, boolean listFlag)
			throws IOException, TemplateException {
		try (StringWriter writer = new StringWriter()) {
			this.processCatalogPage(catalog, requestData, listFlag, writer);
			return writer.toString();
		}
	}

	@Override
	public void processCatalogPage(CmsCatalog catalog, IInternalDataType.RequestData requestData, boolean listFlag, Writer writer)
			throws IOException, TemplateException {
		if (CatalogType_Link.ID.equals(catalog.getCatalogType())) {
            String link = InternalUrlUtils.getActualPreviewUrl(catalog.getRedirectUrl(), requestData.getPublishPipeCode());
            this.processException(writer, ContentCoreErrorCode.PREVIEW_LINK_CATALOG, catalog.getName(), link);
            return;
		}
		String templateFilename = catalog.getListTemplate(requestData.getPublishPipeCode());
		if (!listFlag && requestData.getPageIndex() == 1) {
			// 获取首页模板
			String indexTemplate = catalog.getIndexTemplate(requestData.getPublishPipeCode());
			if (StringUtils.isNotEmpty(indexTemplate)) {
				templateFilename = indexTemplate;
			} else {
				listFlag = true;
			}
		}
		CmsSite site = this.siteService.getById(catalog.getSiteId());
		if (StringUtils.isEmpty(templateFilename)) {
			// 站点默认模板
			templateFilename = PublishPipeProp_DefaultListTemplate.getValue(requestData.getPublishPipeCode(),
					site.getPublishPipeProps());
		}
		final String template = templateFilename;
		File templateFile = this.templateService.findTemplateFile(site, template, requestData.getPublishPipeCode());
        if (Objects.isNull(templateFile)) {
            this.processException(writer, ContentCoreErrorCode.TEMPLATE_FILE_NOT_FOUND, template);
            return;
        }

		long s = System.currentTimeMillis();
		// 生成静态页面
		String templateKey = SiteUtils.getTemplateKey(site, requestData.getPublishPipeCode(), template);
		TemplateContext templateContext = new TemplateContext(templateKey, requestData.isPreview(), requestData.getPublishPipeCode());
		templateContext.setPageIndex(requestData.getPageIndex());
		// init template variables
		TemplateUtils.initGlobalVariables(site, templateContext);
		templateContext.getVariables().put(TemplateUtils.TemplateVariable_Request, Objects.requireNonNullElse(requestData.getParams(), Map.of()));
		// init templateType variables
		ITemplateType templateType = templateService.getTemplateType(CatalogTemplateType.TypeId);
		templateType.initTemplateData(catalog.getCatalogId(), templateContext);
		// 分页链接
		if (listFlag) {
			String catalogLink = this.catalogService.getCatalogListLink(catalog, 1, requestData.getPublishPipeCode(), requestData.isPreview());
			templateContext.setFirstFileName(catalogLink);
			templateContext.setOtherFileName(TemplateUtils.appendPageIndexParam(catalogLink, TemplateContext.PlaceHolder_PageNo));
		}
		try {
			this.staticizeService.process(templateContext, writer);
		} finally {
			logger.debug("[{}]Catalog template parsed: {}, cost: {}ms", requestData.getPublishPipeCode(), catalog.getName(),
					(System.currentTimeMillis() - s));
		}
	}

	@Override
	public AsyncTask publishCatalog(CmsCatalog catalog, boolean publishChild, boolean publishDetail,
			final String publishStatus, final Operator operator) {
		List<CmsPublishPipe> publishPipes = publishPipeService.getPublishPipes(catalog.getSiteId());
		Assert.isTrue(!publishPipes.isEmpty(), ContentCoreErrorCode.NO_PUBLISHPIPE::exception);

		AsyncTask asyncTask = new AsyncTask() {

			@Override
			public void run0() throws InterruptedException {
				List<CmsCatalog> catalogs = new ArrayList<>();
				catalogs.add(catalog);
				// 是否包含子栏目
				if (publishChild) {
					LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<CmsCatalog>()
							.eq(CmsCatalog::getStaticFlag, YesOrNo.YES) // 可静态化
							.eq(CmsCatalog::getVisibleFlag, YesOrNo.YES) // 可见
							.likeRight(CmsCatalog::getAncestors, catalog.getAncestors());
					catalogs.addAll(catalogService.list(q));
				}
				// 先发布内容
				if (publishDetail) {
					for (CmsCatalog catalog : catalogs) {
						int pageSize = 100;
						long lastContentId = 0L;
						long total = contentService.dao().lambdaQuery().eq(CmsContent::getCatalogId, catalog.getCatalogId())
								.eq(CmsContent::getStatus, publishStatus)
								.ne(CmsContent::getLinkFlag, YesOrNo.YES)
								.count();
						int count = 1;
						while (true) {
							LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
									.eq(CmsContent::getCatalogId, catalog.getCatalogId())
									.eq(CmsContent::getStatus, publishStatus)
									.ne(CmsContent::getLinkFlag, YesOrNo.YES)
									.gt(CmsContent::getContentId, lastContentId)
									.orderByAsc(CmsContent::getContentId);
							Page<CmsContent> page = contentService.dao().page(new Page<>(0, pageSize, false), q);
							for (CmsContent xContent : page.getRecords()) {
								this.setProgressInfo((int) (count * 100 / total),
										ContentTips.BATCH_PUBLISHING_CONTENT, xContent.getTitle(), count, total);
								lastContentId = xContent.getContentId();
								IContentType contentType = ContentCoreUtils.getContentType(xContent.getContentType());
								IContent<?> content = contentType.newContent();
								content.setContentEntity(xContent);
								content.setOperator(operator);
								transactionTemplate.execute(callback -> content.publish());
                                asyncStaticizeContent(content);
								this.checkInterrupt();
								count++;
							}
							if (page.getRecords().size() < pageSize) {
								break;
							}
						}
					}
				}
				// 发布栏目
				for (int i = 0; i < catalogs.size(); i++) {
					CmsCatalog catalog = catalogs.get(i);
					this.setProgressInfo((i * 100) / catalogs.size(), ContentTips.PUBLISHING_CATALOG, catalog.getName());
					asyncPublishCatalog(catalog);
					this.checkInterrupt(); // 允许中断
				}
				// 发布站点
				CmsSite site = siteService.getSite(catalog.getSiteId());
				this.setProgressInfo(99, ContentTips.PUBLISHING_SITE, site.getName());
				asyncPublishSite(site);
				this.setProgressInfo(100, ContentTips.PUBLISH_SUCCESS);
			}
		};
		asyncTask.setType("Publish");
		asyncTask.setTaskId("Publish-Catalog-" + catalog.getCatalogId());
		asyncTask.setInterruptible(true);
		this.asyncTaskManager.execute(asyncTask);
		return asyncTask;
	}

	public void asyncPublishCatalog(final CmsCatalog catalog) {
		if (CatalogType_Link.ID.equals(catalog.getCatalogType())) {
			return; // 链接栏目直接跳过
		}
		publishStrategy.publish(CatalogStaticizeType.TYPE, catalog.getCatalogId().toString());
        applicationContext.publishEvent(new AfterCatalogPublishEvent(this, catalog));
	}

    @Override
    public String getContentPageData(CmsContent content, IInternalDataType.RequestData requestData)
			throws IOException, TemplateException {
        try (StringWriter writer = new StringWriter()) {
            this.processContentPage(content, requestData, writer);
            return writer.toString();
        }
	}


	@Override
	public void processContentPage(CmsContent content, IInternalDataType.RequestData requestData, Writer writer)
			throws IOException, TemplateException {
		CmsSite site = this.siteService.getById(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (content.isLinkContent()) {
            String link = InternalUrlUtils.getActualPreviewUrl(content.getRedirectUrl(), requestData.getPublishPipeCode());
            this.processException(writer, ContentCoreErrorCode.PREVIEW_LINK_CONTENT, content.getTitle(), link);
            return;
		}
		// 查找模板
		final String detailTemplate = TemplateUtils.getDetailTemplate(site, catalog, content, requestData.getPublishPipeCode());
        if (StringUtils.isEmpty(detailTemplate)) {
            this.processException(writer, ContentCoreErrorCode.TEMPLATE_EMPTY, "content#" + content.getContentId());
            return;
        }
		File templateFile = this.templateService.findTemplateFile(site, detailTemplate, requestData.getPublishPipeCode());
        if (Objects.isNull(templateFile)) {
            this.processException(writer, ContentCoreErrorCode.TEMPLATE_FILE_NOT_FOUND, templateFile);
            return;
        }

		IContentType contentType = ContentCoreUtils.getContentType(content.getContentType());
		long s = System.currentTimeMillis();
		// 生成静态页面
		try {
			// 模板ID = 通道:站点目录:模板文件名
			String templateKey = SiteUtils.getTemplateKey(site, requestData.getPublishPipeCode(), detailTemplate);
			TemplateContext templateContext = new TemplateContext(templateKey, requestData.isPreview(), requestData.getPublishPipeCode());
			templateContext.setPageIndex(requestData.getPageIndex());
			templateContext.getVariables().put(TemplateUtils.TemplateVariable_Request, Objects.requireNonNullElse(requestData.getParams(), Map.of()));
			// init template datamode
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(ContentTemplateType.TypeId);
			templateType.initTemplateData(content.getContentId(), templateContext);
			// 分页链接
			String contentLink = this.contentService.getContentLink(content, 1, requestData.getPublishPipeCode(), requestData.isPreview());
			templateContext.setFirstFileName(contentLink);
			templateContext.setOtherFileName(TemplateUtils.appendPageIndexParam(contentLink, TemplateContext.PlaceHolder_PageNo));
			// staticize
			this.staticizeService.process(templateContext, writer);
		} finally {
			logger.debug("[{}][{}]Content template parsed: {}, cost: {}", requestData.getPublishPipeCode(), contentType.getId(), content.getTitle(),
					System.currentTimeMillis() - s);
		}
	}

	/**
	 * 内容发布
	 */
	@Override
	public AsyncTask publishContents(List<CmsContent> contents, LoginUser loginUser) {
		Locale locale = LocaleContextHolder.getLocale();
		AsyncTask task = new AsyncTask(locale) {
			@Override
			public void run0() {
				publishContents0(contents, Operator.of(loginUser), this.getLocale());
			}
		};
		task.setType("PublishContents");
		asyncTaskManager.execute(task);
		return task;
	}

	@Override
	public void publishContent(CmsContent content, Operator operator) {
		publishContents0(List.of(content), operator, null);
	}

	private void publishContents0(List<CmsContent> contents, Operator operator, Locale locale) {
		if (contents.isEmpty()) {
			return;
		}
		// 发布内容
		Set<Long> catalogIds = new HashSet<>();
		for (CmsContent cmsContent : contents) {
			AsyncTaskManager.setTaskTenPercentProgressInfo(ContentTips.PUBLISHING_CONTENT.locale(locale, cmsContent.getTitle()));
			IContentType contentType = ContentCoreUtils.getContentType(cmsContent.getContentType());
			IContent<?> content = contentType.loadContent(cmsContent);
			content.setOperator(operator);
			transactionTemplate.execute(callback -> content.publish());
            this.asyncStaticizeContent(content);
			catalogIds.add(cmsContent.getCatalogId());
			String[] catalogAncestorsIds = cmsContent.getCatalogAncestors().split(CatalogUtils.ANCESTORS_SPLITER);
			for (String catalogAncestorsId : catalogAncestorsIds) {
				catalogIds.add(Long.valueOf(catalogAncestorsId));
			}
		}
		// 发布关联栏目：内容所属栏目及其所有父级栏目
		catalogIds.stream().map(catalogService::getCatalog).filter(Objects::nonNull)
			.forEach(catalog -> {
				AsyncTaskManager.setTaskTenPercentProgressInfo(ContentTips.PUBLISHING_CATALOG.locale(locale, catalog.getName()));
				asyncPublishCatalog(catalog);
			});
		CmsSite site = siteService.getSite(contents.get(0).getSiteId());
		// 发布站点首页
		AsyncTaskManager.setTaskTenPercentProgressInfo(ContentTips.PUBLISHING_SITE.locale(locale, site.getName()));
		asyncPublishSite(site);
		AsyncTaskManager.setTaskProgressInfo(100, ContentTips.PUBLISH_SUCCESS.locale(locale));
	}

	/**
	 * 发布指定栏目及其祖级栏目和站点
	 * @param catalogId
	 */
	@Override
	public void publishCatalogAncestorsAndSite(Long catalogId) {
		CmsCatalog self = this.catalogService.getCatalog(catalogId);
		if (Objects.isNull(self)) {
			return;
		}
		String[] catalogIds = self.getAncestors().split(CatalogUtils.ANCESTORS_SPLITER);
		for (String cid : catalogIds) {
			Long _cid = Long.valueOf(cid);
			if (self.getCatalogId().equals(_cid)) {
				AsyncTaskManager.setTaskTenPercentProgressInfo(ContentTips.PUBLISHING_CATALOG, self.getName());
				asyncPublishCatalog(self);
			} else {
				CmsCatalog catalog = this.catalogService.getCatalog(Long.valueOf(cid));
				if (Objects.nonNull(catalog)) {
					AsyncTaskManager.setTaskTenPercentProgressInfo(ContentTips.PUBLISHING_CATALOG, catalog.getName());
					asyncPublishCatalog(catalog);
				}
			}
		}
		// 发布站点首页
		CmsSite site = siteService.getSite(self.getSiteId());
		AsyncTaskManager.setTaskTenPercentProgressInfo(ContentTips.PUBLISHING_SITE, site.getName());
		asyncPublishSite(site);
		AsyncTaskManager.setTaskProgressInfo(100, ContentTips.PUBLISH_SUCCESS);
	}

	@Override
	public void asyncStaticizeContent(IContent<?> content) {
        if (!ContentStatus.isPublished(content.getContentEntity().getStatus())) {
            return;
        }
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (!catalog.isStaticize() || content.getContentEntity().isLinkContent()) {
            return; // 栏目设置不静态化
		}
        if (content.getContentEntity().isLinkContent()) {
            return; // 链接内容不需要静态化
        }
		List<CmsPublishPipe> publishPipeCodes = this.publishPipeService.getPublishPipes(content.getSiteId());
		if (publishPipeCodes.isEmpty()) {
			return;
		}
		publishStrategy.publish(ContentStaticizeType.TYPE, content.getContentEntity().getContentId().toString());
		// 关联内容静态化，映射的引用内容
		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
				.eq(CmsContent::getCopyId, content.getContentEntity().getContentId())
				.eq(CmsContent::getCopyType, ContentCopyType.Mapping);
		List<CmsContent> mappingContents = contentService.dao().list(q);
		for (CmsContent mappingContent : mappingContents) {
			publishStrategy.publish(ContentStaticizeType.TYPE, mappingContent.getContentId().toString());
		}
	}
    @Override
    public String getContentExPageData(CmsContent content, String publishPipeCode, boolean isPreview)
            throws IOException, TemplateException {
        try (StringWriter writer = new StringWriter()) {
            this.processContentExtPageData(content, publishPipeCode, isPreview, writer);
            return writer.toString();
        }
    }

	private void processContentExtPageData(CmsContent content, String publishPipeCode, boolean isPreview, StringWriter writer)
			throws IOException, TemplateException {
		CmsSite site = this.siteService.getById(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (!catalog.isStaticize() ) {
            this.processException(writer, ContentCoreErrorCode.CATALOG_CANNOT_PUBLISH, catalog.getName());
            return;
		}
		if (content.isLinkContent()) {
            String link = InternalUrlUtils.getActualUrl(content.getRedirectUrl(), publishPipeCode, isPreview);
            this.processException(writer, ContentCoreErrorCode.PREVIEW_LINK_CONTENT, content.getTitle(), link);
            return;
		}
		String exTemplate = ContentUtils.getContentExTemplate(content, catalog, publishPipeCode);
        if (StringUtils.isEmpty(exTemplate)) {
            this.processException(writer, ContentCoreErrorCode.TEMPLATE_EMPTY, content.getTitle());
            return;
        }
		// 查找模板
		File templateFile = this.templateService.findTemplateFile(site, exTemplate, publishPipeCode);
        if (Objects.isNull(templateFile)) {
            this.processException(writer, ContentCoreErrorCode.TEMPLATE_FILE_NOT_FOUND, exTemplate);
        }
		// 生成静态页面
		long s = System.currentTimeMillis();
        IContentType contentType = ContentCoreUtils.getContentType(content.getContentType());
        try {
            // 模板ID = 通道:站点目录:模板文件名
            String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, exTemplate);
            TemplateContext templateContext = new TemplateContext(templateKey, isPreview, publishPipeCode);
            // init template data mode
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType data to data mode
            ITemplateType templateType = this.templateService.getTemplateType(ContentTemplateType.TypeId);
            templateType.initTemplateData(content.getContentId(), templateContext);
            // staticize
            this.staticizeService.process(templateContext, writer);
        } finally {
            logger.debug("[{}][{}]Content extend template parsed: {}, cost: {}", publishPipeCode, contentType.getId(), content.getTitle(),
                    System.currentTimeMillis() - s);
        }
	}

	@Override
	public String getPageWidgetPageData(CmsPageWidget pageWidget, IInternalDataType.RequestData data)
			throws IOException, TemplateException {
		try (StringWriter writer = new StringWriter()) {
			processPageWidget(pageWidget, data, writer);
			return writer.toString();
		}
	}

	@Override
	public void processPageWidget(CmsPageWidget pageWidget, IInternalDataType.RequestData data, Writer writer)
			throws IOException, TemplateException {
		CmsSite site = this.siteService.getById(pageWidget.getSiteId());
		String template = pageWidget.getTemplate(data.getPublishPipeCode());
        if (StringUtils.isEmpty(template)) {
            this.processException(writer, ContentCoreErrorCode.TEMPLATE_EMPTY, pageWidget.getName());
            return;
        }
		File templateFile = this.templateService.findTemplateFile(site, template, data.getPublishPipeCode());
        if (Objects.isNull(templateFile)) {
            this.processException(writer, ContentCoreErrorCode.TEMPLATE_FILE_NOT_FOUND, template);
            return;
        }

		// 生成静态页面
		long s = System.currentTimeMillis();
		try {
			// 模板ID = 通道:站点目录:模板文件名
			String templateKey = SiteUtils.getTemplateKey(site, data.getPublishPipeCode(), template);
			TemplateContext templateContext = new TemplateContext(templateKey, data.isPreview(), data.getPublishPipeCode());
			// init template global variables
			TemplateUtils.initGlobalVariables(site, templateContext);
			templateContext.getVariables().put(TemplateUtils.TemplateVariable_PageWidget, pageWidget);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(SiteTemplateType.TypeId);
			templateType.initTemplateData(site.getSiteId(), templateContext);
			// staticize
			this.staticizeService.process(templateContext, writer);
		} finally {
			logger.debug("[{}]The page-widget template parsed: {}#{}, cost: {}ms", data.getPublishPipeCode(), pageWidget.getName(),
					pageWidget.getCode(), System.currentTimeMillis() - s);
		}
	}

	@Override
	public void pageWidgetStaticize(IPageWidget pageWidget) {
		CmsPageWidget pw = pageWidget.getPageWidgetEntity();
		if (StringUtils.isEmpty(pw.getTemplates())) {
			logger.warn("The page-widget template not configured: {}#{}", pw.getName(), pw.getCode());
			return;
		}
		publishStrategy.publish(PageWidgetStaticizeType.TYPE, pageWidget.getPageWidgetEntity().getPageWidgetId().toString());
	}

	@Override
	public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
