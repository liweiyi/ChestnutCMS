package com.chestnut.contentcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.staticize.StaticizeService;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.config.CMSPublishConfig;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.IPageWidget;
import com.chestnut.contentcore.core.IPublishPipeProp;
import com.chestnut.contentcore.core.impl.*;
import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.listener.event.AfterContentPublishEvent;
import com.chestnut.contentcore.properties.MaxPageOnContentPublishProperty;
import com.chestnut.contentcore.publish.CatalogPublishTask;
import com.chestnut.contentcore.publish.ContentPublishTask;
import com.chestnut.contentcore.publish.SitePublishTask;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.template.impl.CatalogTemplateType;
import com.chestnut.contentcore.template.impl.ContentTemplateType;
import com.chestnut.contentcore.template.impl.SiteTemplateType;
import com.chestnut.contentcore.util.*;
import com.chestnut.system.fixed.dict.YesOrNo;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PublishServiceImpl implements IPublishService, ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(PublishServiceImpl.class);

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IContentService contentService;

	private final ITemplateService templateService;

	private final IPublishPipeService publishPipeService;

	private final StaticizeService staticizeService;

	private final AsyncTaskManager asyncTaskManager;

	private ApplicationContext applicationContext;

	@Override
	public String getSitePageData(CmsSite site, String publishPipeCode, boolean isPreview)
			throws IOException, TemplateException {
		TemplateContext context = this.generateSiteTemplateContext(site, publishPipeCode, isPreview);

		long s = System.currentTimeMillis();
		try (StringWriter writer = new StringWriter()) {
			this.staticizeService.process(context, writer);
			return writer.toString();
		} finally {
			logger.debug("[{}]首页模板解析：{}\t耗时：{}ms", publishPipeCode, site.getName(), System.currentTimeMillis() - s);
		}
	}

	@Override
	public void publishSiteIndex(CmsSite site) throws IOException, TemplateException {
		// 发布所有通道页面
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(site.getSiteId());
		Assert.isTrue(!publishPipes.isEmpty(), ContentCoreErrorCode.NO_PUBLISHPIPE::exception);

		Map<String, String> data = Map.of("type", SitePublishTask.Type, "id", site.getSiteId().toString());
		redisTemplate.opsForStream().add(MapRecord.create(CMSPublishConfig.PublishStreamName, data));

//		this.siteStaticize(site);
	}

	@Override
	public AsyncTask publishAll(CmsSite site, final String contentStatus, final LoginUser operator) {
		AsyncTask asyncTask = new AsyncTask() {

			@Override
			public void run0() throws InterruptedException {
				List<CmsPublishPipe> publishPipes = publishPipeService.getPublishPipes(site.getSiteId());
				publishPipes.forEach(pp -> {
					try {
						String siteRoot = SiteUtils.getSiteRoot(site, pp.getCode());
						FileUtils.deleteDirectory(new File(siteRoot + "include/"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				});

				List<CmsCatalog> catalogList = catalogService
						.list(new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId, site.getSiteId()));
				for (CmsCatalog catalog : catalogList) {
					// 先发布内容
					int pageSize = 100;
					long lastContentId = 0L;
					long total = contentService.lambdaQuery().eq(CmsContent::getCatalogId, catalog.getCatalogId())
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
						Page<CmsContent> page = contentService.page(new Page<>(0, pageSize, false), q);
						for (CmsContent xContent : page.getRecords()) {
							this.setProgressInfo((int) (count * 100 / total),
									"正在发布内容：" + catalog.getName() + "[" + count + " / " + total + "]");
							lastContentId = xContent.getContentId();
							IContentType contentType = ContentCoreUtils.getContentType(xContent.getContentType());
							IContent<?> content = contentType.newContent();
							content.setContentEntity(xContent);
							content.setOperator(operator);
							content.publish();
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
					this.setProgressInfo((i * 100) / catalogList.size(), "正在发布栏目：" + catalog.getName());
					asyncPublishCatalog(catalog);
					this.checkInterrupt(); // 允许中断
				}
				// 发布站点
				this.setProgressInfo(99, "正在发布首页：" + site.getName());
				asyncPublishSite(site);
				this.setProgressInfo(100, "发布完成");
			}
		};
		asyncTask.setType("Publish");
		asyncTask.setTaskId("Publish-Site-" + site.getSiteId());
		asyncTask.setInterruptible(true);
		this.asyncTaskManager.execute(asyncTask);
		return asyncTask;
	}

	private final StringRedisTemplate redisTemplate;

	private void asyncPublishSite(CmsSite site) {
		Map<String, String> data = Map.of("type", SitePublishTask.Type, "id", site.getSiteId().toString());
		redisTemplate.opsForStream().add(MapRecord.create(CMSPublishConfig.PublishStreamName, data));
	}

	@Override
	public void siteStaticize(CmsSite site) {
		this.publishPipeService.getPublishPipes(site.getSiteId())
				.forEach(pp -> doSiteStaticize(site, pp.getCode()));
	}

	private void doSiteStaticize(CmsSite site, String publishPipeCode) {
		try {
			AsyncTaskManager
					.setTaskMessage(StringUtils.messageFormat("[{0}]正在发布站点首页：{1}", publishPipeCode, site.getName()));
			TemplateContext templateContext = this.generateSiteTemplateContext(site, publishPipeCode, false);

			long s = System.currentTimeMillis();
			templateContext.setDirectory(SiteUtils.getSiteRoot(site, publishPipeCode));
			templateContext.setFirstFileName("index" + StringUtils.DOT + site.getStaticSuffix(publishPipeCode));
			this.staticizeService.process(templateContext);
			logger.debug("[{}]首页模板解析：{}，耗时：{}ms", publishPipeCode, site.getName(), (System.currentTimeMillis() - s));
		} catch (Exception e) {
			logger.error(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}][{1}]站点首页解析失败：{2}",
					publishPipeCode, site.getName(), e.getMessage())), e);
		}
	}

	private TemplateContext generateSiteTemplateContext(CmsSite site, String publishPipeCode, boolean isPreview) {
		String indexTemplate = site.getIndexTemplate(publishPipeCode);
		File templateFile = this.templateService.findTemplateFile(site, indexTemplate, publishPipeCode);
		Assert.notNull(templateFile,
				() -> ContentCoreErrorCode.TEMPLATE_EMPTY.exception(publishPipeCode, indexTemplate));

		// 模板ID = 通道:站点目录:模板文件名
		String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, indexTemplate);
		TemplateContext templateContext = new TemplateContext(templateKey, isPreview, publishPipeCode);
		// init template datamode
		TemplateUtils.initGlobalVariables(site, templateContext);
		// init templateType data to datamode
		ITemplateType templateType = templateService.getTemplateType(SiteTemplateType.TypeId);
		templateType.initTemplateData(site.getSiteId(), templateContext);
		return templateContext;
	}

	@Override
	public String getCatalogPageData(CmsCatalog catalog, int pageIndex, boolean listFlag, String publishPipeCode, boolean isPreview)
			throws IOException, TemplateException {
		if (CatalogType_Link.ID.equals(catalog.getCatalogType())) {
			throw new RuntimeException("链接类型栏目无独立页面：" + catalog.getName());
		}
		String templateFilename = catalog.getListTemplate(publishPipeCode);
		if (!listFlag && pageIndex == 1) {
			// 获取首页模板
			String indexTemplate = catalog.getIndexTemplate(publishPipeCode);
			if (StringUtils.isNotEmpty(indexTemplate)) {
				templateFilename = indexTemplate;
			}
		}
		CmsSite site = this.siteService.getById(catalog.getSiteId());
		if (StringUtils.isEmpty(templateFilename)) {
			// 站点默认模板
			templateFilename = PublishPipeProp_DefaultListTemplate.getValue(publishPipeCode,
					site.getPublishPipeProps());
		}
		final String template = templateFilename;
		File templateFile = this.templateService.findTemplateFile(site, template, publishPipeCode);
		Assert.notNull(templateFile, () -> ContentCoreErrorCode.TEMPLATE_EMPTY.exception(publishPipeCode, template));

		long s = System.currentTimeMillis();
		// 生成静态页面
		String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, template);
		TemplateContext templateContext = new TemplateContext(templateKey, isPreview, publishPipeCode);
		templateContext.setPageIndex(pageIndex);
		// init template variables
		TemplateUtils.initGlobalVariables(site, templateContext);
		// init templateType variables
		ITemplateType templateType = templateService.getTemplateType(CatalogTemplateType.TypeId);
		templateType.initTemplateData(catalog.getCatalogId(), templateContext);
		// 分页链接
		if (listFlag) {
			String catalogLink = this.catalogService.getCatalogListLink(catalog, 1, publishPipeCode, isPreview);
			templateContext.setFirstFileName(catalogLink);
			templateContext.setOtherFileName(catalogLink + "&pi=" + TemplateContext.PlaceHolder_PageNo);
		}
		try (StringWriter writer = new StringWriter()) {
			this.staticizeService.process(templateContext, writer);
			return writer.toString();
		} finally {
			logger.debug("[{}]栏目页模板解析：{}，耗时：{}ms", publishPipeCode, catalog.getName(),
					(System.currentTimeMillis() - s));
		}
	}

	@Override
	public AsyncTask publishCatalog(CmsCatalog catalog, boolean publishChild, boolean publishDetail,
			final String publishStatus, final LoginUser operator) {
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
							.ne(CmsCatalog::getCatalogType, CatalogType_Link.ID) // 普通栏目
							.likeRight(CmsCatalog::getAncestors, catalog.getAncestors());
					catalogs.addAll(catalogService.list(q));
				}
				// 先发布内容
				if (publishDetail) {
					for (CmsCatalog catalog : catalogs) {
						int pageSize = 100;
						long lastContentId = 0L;
						long total = contentService.lambdaQuery().eq(CmsContent::getCatalogId, catalog.getCatalogId())
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
							Page<CmsContent> page = contentService.page(new Page<>(0, pageSize, false), q);
							for (CmsContent xContent : page.getRecords()) {
								this.setProgressInfo((int) (count * 100 / total),
										"正在发布内容：" + catalog.getName() + "[" + count + " / " + total + "]");
								lastContentId = xContent.getContentId();
								IContentType contentType = ContentCoreUtils.getContentType(xContent.getContentType());
								IContent<?> content = contentType.newContent();
								content.setContentEntity(xContent);
								content.setOperator(operator);
								content.publish();
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
					this.setProgressInfo((i * 100) / catalogs.size(), "正在发布栏目：" + catalog.getName());
					asyncPublishCatalog(catalog);
					this.checkInterrupt(); // 允许中断
				}
				// 发布站点
				this.setPercent(99);
				asyncPublishSite(siteService.getSite(catalog.getSiteId()));
				this.setProgressInfo(100, "发布完成");
			}
		};
		asyncTask.setType("Publish");
		asyncTask.setTaskId("Publish-Catalog-" + catalog.getCatalogId());
		asyncTask.setInterruptible(true);
		this.asyncTaskManager.execute(asyncTask);
		return asyncTask;
	}

	public void asyncPublishCatalog(final CmsCatalog catalog) {
		Map<String, String> data = Map.of("type", CatalogPublishTask.Type, "id", catalog.getCatalogId().toString());
		redisTemplate.opsForStream().add(MapRecord.create(CMSPublishConfig.PublishStreamName, data));
	}

	@Override
	public void catalogStaticize(CmsCatalog catalog) {
		CmsSite site = this.siteService.getSite(catalog.getSiteId());
		int maxPage = MaxPageOnContentPublishProperty.getValue(site.getConfigProps());
		this.catalogStaticize(catalog, maxPage);
	}

	@Override
	public void catalogStaticize(CmsCatalog catalog, int pageMax) {
		if (!catalog.isStaticize() || !catalog.isVisible() || CatalogType_Link.ID.equals(catalog.getCatalogType())) {
			return;
		}
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
		for (CmsPublishPipe pp : publishPipes) {
			this.doCatalogStaticize(catalog, pp.getCode(), pageMax);
		}
	}

	private void doCatalogStaticize(CmsCatalog catalog, String publishPipeCode, int pageMax) {
		CmsSite site = this.siteService.getSite(catalog.getSiteId());
		if (!catalog.isStaticize()) {
			logger.warn("【{}】未启用静态化的栏目跳过静态化：{}", publishPipeCode, catalog.getName());
			return;
		}
		if (!catalog.isVisible()) {
			logger.warn("【{}】不可见状态的栏目跳过静态化：{}", publishPipeCode, catalog.getName());
			return;
		}
		if (CatalogType_Link.ID.equals(catalog.getCatalogType())) {
			logger.warn("【{}】链接类型栏目跳过静态化：{}", publishPipeCode, catalog.getName());
			return;
		}
		String indexTemplate = PublishPipeProp_IndexTemplate.getValue(publishPipeCode, catalog.getPublishPipeProps());
		String listTemplate = PublishPipeProp_ListTemplate.getValue(publishPipeCode, catalog.getPublishPipeProps());
		if (StringUtils.isEmpty(listTemplate)) {
			listTemplate = PublishPipeProp_DefaultListTemplate.getValue(publishPipeCode, site.getPublishPipeProps()); // 取站点默认模板
		}
		File indexTemplateFile = this.templateService.findTemplateFile(site, indexTemplate, publishPipeCode);
		File listTemplateFile = this.templateService.findTemplateFile(site, listTemplate, publishPipeCode);
		if (indexTemplateFile == null && listTemplateFile == null) {
			logger.warn(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]栏目首页模板和列表页模板未配置或不存在：{1}",
					publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName())));
			return;
		}
		String siteRoot = SiteUtils.getSiteRoot(site, publishPipeCode);
		String dirPath = siteRoot + catalog.getPath();
		FileExUtils.mkdirs(dirPath);
		String staticSuffix = site.getStaticSuffix(publishPipeCode); // 静态化文件类型

		// 发布栏目首页
		long s = System.currentTimeMillis();
		if (Objects.nonNull(indexTemplateFile)) {
			try {
				String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, indexTemplate);
				TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
				templateContext.setDirectory(dirPath);
				templateContext.setFirstFileName("index" + StringUtils.DOT + staticSuffix);
				// init template variables
				TemplateUtils.initGlobalVariables(site, templateContext);
				// init templateType variables
				ITemplateType templateType = templateService.getTemplateType(CatalogTemplateType.TypeId);
				templateType.initTemplateData(catalog.getCatalogId(), templateContext);
				// staticize
				this.staticizeService.process(templateContext);
				logger.debug("[{}]栏目首页模板解析：{}，耗时：{}ms", publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName(), (System.currentTimeMillis() - s));
			} catch (IOException | TemplateException e) {
				logger.error(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]栏目首页解析失败：{1}",
						publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName())), e);
			}
		}
		// 发布栏目列表页
		if (Objects.nonNull(listTemplateFile)) {
			s = System.currentTimeMillis();
			try {
				String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, listTemplate);
				TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
				templateContext.setMaxPageNo(pageMax);
				templateContext.setDirectory(dirPath);
				String name = Objects.nonNull(indexTemplateFile) ? "list" : "index";
				templateContext.setFirstFileName(name + StringUtils.DOT + staticSuffix);
				templateContext.setOtherFileName(
						name + "_" + TemplateContext.PlaceHolder_PageNo + StringUtils.DOT + staticSuffix);
				// init template variables
				TemplateUtils.initGlobalVariables(site, templateContext);
				// init templateType variables
				ITemplateType templateType = templateService.getTemplateType(CatalogTemplateType.TypeId);
				templateType.initTemplateData(catalog.getCatalogId(), templateContext);
				// staticize
				this.staticizeService.process(templateContext);
				logger.debug("[{}]栏目列表模板解析：{}，耗时：{}ms", publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName(), (System.currentTimeMillis() - s));
			} catch (Exception e1) {
				logger.error(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]栏目列表页解析失败：{1}",
						publishPipeCode, catalog.getCatalogId() + "#" + catalog.getName())), e1);
			}
		}
	}

	private String getDetailTemplate(CmsSite site, CmsCatalog catalog, CmsContent content, String publishPipeCode) {
		String detailTemplate = PublishPipeProp_ContentTemplate.getValue(publishPipeCode,
				content.getPublishPipeProps());
		if (StringUtils.isEmpty(detailTemplate)) {
			// 无内容独立模板取栏目配置
			detailTemplate = this.publishPipeService.getPublishPipePropValue(
					IPublishPipeProp.DetailTemplatePropPrefix + content.getContentType(), publishPipeCode,
					catalog.getPublishPipeProps());
			if (StringUtils.isEmpty(detailTemplate)) {
				// 无栏目配置去站点默认模板配置
				detailTemplate = this.publishPipeService.getPublishPipePropValue(
						IPublishPipeProp.DefaultDetailTemplatePropPrefix + content.getContentType(), publishPipeCode,
						site.getPublishPipeProps());
			}
		}
		return detailTemplate;
	}

	@Override
	public String getContentPageData(CmsContent content, int pageIndex, String publishPipeCode, boolean isPreview)
			throws IOException, TemplateException {
		CmsSite site = this.siteService.getById(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (content.isLinkContent()) {
			throw new RuntimeException("标题内容：" + content.getTitle() + "，跳转链接：" + content.getRedirectUrl());
		}
		// 查找模板
		final String detailTemplate = getDetailTemplate(site, catalog, content, publishPipeCode);
		File templateFile = this.templateService.findTemplateFile(site, detailTemplate, publishPipeCode);
		Assert.notNull(templateFile,
				() -> ContentCoreErrorCode.TEMPLATE_EMPTY.exception(publishPipeCode, detailTemplate));

		long s = System.currentTimeMillis();
		// 生成静态页面
		try (StringWriter writer = new StringWriter()) {
			IContentType contentType = ContentCoreUtils.getContentType(content.getContentType());
			// 模板ID = 通道:站点目录:模板文件名
			String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, detailTemplate);
			TemplateContext templateContext = new TemplateContext(templateKey, isPreview, publishPipeCode);
			templateContext.setPageIndex(pageIndex);
			// init template datamode
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(ContentTemplateType.TypeId);
			templateType.initTemplateData(content.getContentId(), templateContext);
			// 分页链接
			String contentLink = this.contentService.getContentLink(content, 1, publishPipeCode, isPreview);
			templateContext.setFirstFileName(contentLink);
			templateContext.setOtherFileName(contentLink + "&pi=" + TemplateContext.PlaceHolder_PageNo);
			// staticize
			this.staticizeService.process(templateContext, writer);
			logger.debug("[{}][{}]内容模板解析：{}，耗时：{}", publishPipeCode, contentType.getName(), content.getTitle(),
					System.currentTimeMillis() - s);
			return writer.toString();
		}
	}

	/**
	 * 内容发布
	 */
	@Override
	public void publishContent(List<Long> contentIds, LoginUser operator) {
		List<CmsContent> list = this.contentService.listByIds(contentIds);
		if (list.isEmpty()) {
			return;
		}
		asyncTaskManager.execute(() -> {
			// 发布内容
			Set<Long> catalogIds = new HashSet<>();
			for (CmsContent cmsContent : list) {
				IContentType contentType = ContentCoreUtils.getContentType(cmsContent.getContentType());
				IContent<?> content = contentType.loadContent(cmsContent);
				content.setOperator(operator);

				catalogIds.add(cmsContent.getCatalogId());
				if (content.publish()) {
					applicationContext.publishEvent(new AfterContentPublishEvent(contentType, content));
				}
			}
			// 发布关联栏目：内容所属栏目及其所有父级栏目
			Map<Long, CmsCatalog> catalogMap = new HashMap<>();
			catalogIds.forEach(catalogId -> {
				CmsCatalog catalog = catalogService.getCatalog(catalogId);
				catalogMap.put(catalog.getCatalogId(), catalog);
				long parentId = catalog.getParentId();
				while (parentId > 0) {
					CmsCatalog parent = catalogService.getCatalog(parentId);
					if (parent == null) {
						break;
					}
					catalogMap.put(parent.getCatalogId(), parent);
					parentId = parent.getParentId();
				}
			});
			CmsSite site = siteService.getSite(list.get(0).getSiteId());
			catalogMap.values().forEach(this::asyncPublishCatalog);
			// 发布站点首页
			asyncPublishSite(site);
		});
	}

	@Override
	public void asyncPublishContent(IContent<?> content) {
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (!catalog.isStaticize()) {
			return;
		}
		List<CmsPublishPipe> publishPipeCodes = this.publishPipeService.getPublishPipes(content.getSiteId());
		if (publishPipeCodes.isEmpty()) {
			return;
		}
		{
			Map<String, String> data = Map.of("type", ContentPublishTask.Type, "id", content.getContentEntity().getContentId().toString());
			redisTemplate.opsForStream().add(MapRecord.create(CMSPublishConfig.PublishStreamName, data));
		}
		// 关联内容静态化，映射的引用内容
		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
				.eq(CmsContent::getCopyId, content.getContentEntity().getContentId())
				.eq(CmsContent::getCopyType, ContentCopyType.Mapping);
		List<CmsContent> mappingContents = contentService.list(q);
		for (CmsContent mappingContent : mappingContents) {
			Map<String, String> data = Map.of("type", ContentPublishTask.Type, "id", mappingContent.getContentId().toString());
			redisTemplate.opsForStream().add(MapRecord.create(CMSPublishConfig.PublishStreamName, data));
		}
	}

	@Override
	public void contentStaticize(CmsContent cmsContent) {
		List<CmsPublishPipe> publishPipes = publishPipeService.getPublishPipes(cmsContent.getSiteId());
		// 发布内容
		for (CmsPublishPipe pp : publishPipes) {
			doContentStaticize(cmsContent, pp.getCode());
			// 内容扩展模板静态化
			doContentExStaticize(cmsContent, pp.getCode());
		}
	}

	private void doContentStaticize(CmsContent content, String publishPipeCode) {
		CmsSite site = this.siteService.getSite(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (!catalog.isStaticize()) {
			logger.warn("【{}】栏目设置不静态化：{} - {}", publishPipeCode, catalog.getName(), content.getTitle());
			return; // 不静态化直接跳过
		}
		if (content.isLinkContent()) {
			return; // 标题内容不需要静态化
		}
		final String detailTemplate = getDetailTemplate(site, catalog, content, publishPipeCode);
		File templateFile = this.templateService.findTemplateFile(site, detailTemplate, publishPipeCode);
		if (templateFile == null) {
			logger.warn(AsyncTaskManager.addErrMessage(
					StringUtils.messageFormat("[{0}]栏目[{1}]详情页模板未设置或文件不存在", publishPipeCode, catalog.getName())));
			return;
		}
		try {
			long s = System.currentTimeMillis();
			// 自定义模板上下文
			String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, detailTemplate);
			TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
			// init template datamode
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(ContentTemplateType.TypeId);
			templateType.initTemplateData(content.getContentId(), templateContext);
			// 静态化文件地址
			this.setContentStaticPath(site, catalog, content, templateContext);
			// 静态化
			this.staticizeService.process(templateContext);
			logger.debug("[{}]内容详情页模板解析：[{}]{}，耗时：{}ms", publishPipeCode, catalog.getName(), content.getTitle(), (System.currentTimeMillis() - s));
		} catch (TemplateException | IOException e) {
			logger.error(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]内容详情页解析失败：[{1}]{2}",
					publishPipeCode, catalog.getName(), content.getTitle())), e);
		}
	}

	private void setContentStaticPath(CmsSite site, CmsCatalog catalog, CmsContent content, TemplateContext context) {
		String siteRoot = SiteUtils.getSiteRoot(site, context.getPublishPipeCode());
		if (StringUtils.isNotBlank(content.getStaticPath())) {
			String dir = "";
			String filename = content.getStaticPath();
			if (filename.indexOf("/") > 0) {
				dir = filename.substring(0, filename.lastIndexOf("/") + 1);
				filename = filename.substring(filename.lastIndexOf("/") + 1);
			}
			context.setDirectory(siteRoot + dir);
			context.setFirstFileName(filename);
			String name = filename.substring(0, filename.lastIndexOf("."));
			String suffix = filename.substring(filename.lastIndexOf("."));
			context.setOtherFileName(name + "_" + TemplateContext.PlaceHolder_PageNo + suffix);
		} else {
			context.setDirectory(siteRoot + catalog.getPath());
			String suffix = site.getStaticSuffix(context.getPublishPipeCode());
			context.setFirstFileName(content.getContentId() + StringUtils.DOT + suffix);
			context.setOtherFileName(
					content.getContentId() + "_" + TemplateContext.PlaceHolder_PageNo + StringUtils.DOT + suffix);
		}
	}

	@Override
	public String getContentExPageData(CmsContent content, String publishPipeCode, boolean isPreview)
			throws IOException, TemplateException {
		CmsSite site = this.siteService.getById(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (!catalog.isStaticize() ) {
			throw new RuntimeException("栏目设置不静态化：" + content.getTitle());
		}
		if (content.isLinkContent()) {
			throw new RuntimeException("标题内容：" + content.getTitle() + "，跳转链接：" + content.getRedirectUrl());
		}
		String exTemplate = ContentUtils.getContentExTemplate(content, catalog, publishPipeCode);
		// 查找模板
		File templateFile = this.templateService.findTemplateFile(site, exTemplate, publishPipeCode);
		Assert.notNull(templateFile,
				() -> ContentCoreErrorCode.TEMPLATE_EMPTY.exception(publishPipeCode, exTemplate));

		long s = System.currentTimeMillis();
		// 生成静态页面
		try (StringWriter writer = new StringWriter()) {
			IContentType contentType = ContentCoreUtils.getContentType(content.getContentType());
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
			logger.debug("[{}][{}]内容扩展模板解析：{}，耗时：{}", publishPipeCode, contentType.getName(), content.getTitle(),
					System.currentTimeMillis() - s);
			return writer.toString();
		}
	}

	private void doContentExStaticize(CmsContent content, String publishPipeCode) {
		CmsSite site = this.siteService.getSite(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (!catalog.isStaticize()) {
			logger.info("【{}】栏目设置不静态化：{} - {}", publishPipeCode, catalog.getName(), content.getTitle());
			return; // 不静态化直接跳过
		}
		if (content.isLinkContent()) {
			return; // 标题内容不需要静态化
		}
		String exTemplate = ContentUtils.getContentExTemplate(content, catalog, publishPipeCode);
		if (StringUtils.isEmpty(exTemplate)) {
			return; // 未设置扩展模板直接跳过
		}
		File templateFile = this.templateService.findTemplateFile(site, exTemplate, publishPipeCode);
		if (templateFile == null) {
			logger.warn("[{}]栏目[{}#{}]扩展模板未设置或文件不存在", publishPipeCode, catalog.getName(), content.getTitle());
			return;
		}
		try {
			long s = System.currentTimeMillis();
			// 自定义模板上下文
			String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, exTemplate);
			TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
			// init template datamode
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(ContentTemplateType.TypeId);
			templateType.initTemplateData(content.getContentId(), templateContext);
			// 静态化文件地址
			String siteRoot = SiteUtils.getSiteRoot(site, publishPipeCode);
			templateContext.setDirectory(siteRoot + catalog.getPath());
			String fileName = ContentUtils.getContextExFileName(content.getContentId(), site.getStaticSuffix(publishPipeCode));
			templateContext.setFirstFileName(fileName);
			// 静态化
			this.staticizeService.process(templateContext);
			logger.debug("[{}]内容扩展模板解析：[{}]{}，耗时：{}ms", publishPipeCode, catalog.getName(), content.getTitle(), (System.currentTimeMillis() - s));
		} catch (TemplateException | IOException e) {
			logger.warn(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]内容扩展模板解析失败：[{1}]{2}",
					publishPipeCode, catalog.getName(), content.getTitle())));
			e.printStackTrace();
		}
	}

	@Override
	public String getPageWidgetPageData(CmsPageWidget pageWidget, boolean isPreview)
			throws IOException, TemplateException {
		CmsSite site = this.siteService.getById(pageWidget.getSiteId());
		File templateFile = this.templateService.findTemplateFile(site, pageWidget.getTemplate(),
				pageWidget.getPublishPipeCode());
		Assert.notNull(templateFile,
				() -> ContentCoreErrorCode.TEMPLATE_EMPTY.exception(pageWidget.getName(), pageWidget.getCode()));

		// 生成静态页面
		try (StringWriter writer = new StringWriter()) {
			long s = System.currentTimeMillis();
			// 模板ID = 通道:站点目录:模板文件名
			String templateKey = SiteUtils.getTemplateKey(site, pageWidget.getPublishPipeCode(),
					pageWidget.getTemplate());
			TemplateContext templateContext = new TemplateContext(templateKey, isPreview,
					pageWidget.getPublishPipeCode());
			// init template global variables
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(SiteTemplateType.TypeId);
			templateType.initTemplateData(site.getSiteId(), templateContext);
			// staticize
			this.staticizeService.process(templateContext, writer);
			logger.debug("[{}]页面部件【{}#{}】模板解析耗时：{}ms", pageWidget.getPublishPipeCode(), pageWidget.getName(),
					pageWidget.getCode(), System.currentTimeMillis() - s);
			return writer.toString();
		}
	}

	@Override
	public void pageWidgetStaticize(IPageWidget pageWidget) {
		long s = System.currentTimeMillis();
		CmsPageWidget pw = pageWidget.getPageWidgetEntity();
		CmsSite site = this.siteService.getSite(pw.getSiteId());
		File templateFile = this.templateService.findTemplateFile(site, pw.getTemplate(), pw.getPublishPipeCode());
		Assert.notNull(templateFile, () -> new RuntimeException(
				StringUtils.messageFormat("页面部件【{0}%s#{1}%s】模板未配置或文件不存在", pw.getName(), pw.getCode())));
		try {
			// 静态化目录
			String dirPath = SiteUtils.getSiteRoot(site, pw.getPublishPipeCode()) + pw.getPath();
			FileExUtils.mkdirs(dirPath);
			// 自定义模板上下文
			String templateKey = SiteUtils.getTemplateKey(site, pw.getPublishPipeCode(), pw.getTemplate());
			TemplateContext templateContext = new TemplateContext(templateKey, false, pw.getPublishPipeCode());
			templateContext.setDirectory(dirPath);
			String staticFileName = PageWidgetUtils.getStaticFileName(pw, site.getStaticSuffix(pw.getPublishPipeCode()));
			templateContext.setFirstFileName(staticFileName);
			// init template datamode
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = templateService.getTemplateType(SiteTemplateType.TypeId);
			templateType.initTemplateData(site.getSiteId(), templateContext);
			// staticize
			this.staticizeService.process(templateContext);
			logger.debug("[{}]页面部件模板解析：{}，耗时：{}ms", pw.getPublishPipeCode(), pw.getCode(), System.currentTimeMillis() - s);
		} catch (TemplateException | IOException e) {
			logger.error(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]页面部件模板解析失败：{1}#{2}",
					pw.getPublishPipeCode(), pw.getName(), pw.getCode())), e);
		}
	}

	@Override
	public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
