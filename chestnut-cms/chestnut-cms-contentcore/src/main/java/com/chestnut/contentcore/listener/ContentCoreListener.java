package com.chestnut.contentcore.listener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.core.impl.InternalDataType_Content;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.CmsSiteProperty;
import com.chestnut.contentcore.domain.CmsTemplate;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.listener.event.AfterCatalogSaveEvent;
import com.chestnut.contentcore.listener.event.AfterContentOfflineEvent;
import com.chestnut.contentcore.listener.event.AfterSiteDeleteEvent;
import com.chestnut.contentcore.listener.event.BeforeCatalogDeleteEvent;
import com.chestnut.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.chestnut.contentcore.mapper.CmsContentMapper;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISitePropertyService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.system.fixed.dict.YesOrNo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContentCoreListener {

	private final ISiteService siteService;
	
	private final ISitePropertyService sitePropertyService;
	
	private final ICatalogService catalogService;

	private final IPublishPipeService publishPipeService;

	private final IContentService contentService;
	
	private final IResourceService resourceService;
	
	private final CmsContentMapper contentMapper;
	
	private final IPageWidgetService pageWidgetService;
	
	private final ITemplateService templateService;
	
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
			long total = this.contentMapper.selectCountBySiteIdIgnoreLogicDel(site.getSiteId());
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除内容备份数据：" + (i * pageSize) + "/" + total);
				this.contentMapper.deleteBySiteIdIgnoreLogicDel(site.getSiteId(), pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除内容错误：" + e.getMessage());
		}
		// 删除资源数据
		try {
			long total = this.resourceService
					.count(new LambdaQueryWrapper<CmsResource>().eq(CmsResource::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除资源数据：" + (i * pageSize) + "/" + total);
				this.resourceService.remove(new LambdaQueryWrapper<CmsResource>()
						.eq(CmsResource::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除资源数据错误：" + e.getMessage());
		}
		// 删除栏目
		try {
			long total = this.catalogService
					.count(new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除栏目数据：" + (i * pageSize) + "/" + total);
				this.catalogService.remove(new LambdaQueryWrapper<CmsCatalog>()
						.eq(CmsCatalog::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除资源数据错误：" + e.getMessage());
		}
		// 删除站点扩展属性
		try {
			long total = this.sitePropertyService
					.count(new LambdaQueryWrapper<CmsSiteProperty>().eq(CmsSiteProperty::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除站点扩展属性数据：" + (i * pageSize) + "/" + total);
				this.sitePropertyService.remove(new LambdaQueryWrapper<CmsSiteProperty>()
						.eq(CmsSiteProperty::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除站点扩展属性错误：" + e.getMessage());
		}
		// 删除模板数据
		try {
			long total = this.templateService
					.count(new LambdaQueryWrapper<CmsTemplate>().eq(CmsTemplate::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除模板数据：" + (i * pageSize) + "/" + total);
				this.templateService.remove(new LambdaQueryWrapper<CmsTemplate>()
						.eq(CmsTemplate::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除模板数据错误：" + e.getMessage());
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
			if (publishPipes.size() > 0) {
				CmsSite site = this.siteService.getSite(catalog.getSiteId());
				for (CmsPublishPipe publishPipe : publishPipes) {
					String siteRoot = SiteUtils.getSiteRoot(site, publishPipe.getCode());
					try {
						Files.move(Paths.get(siteRoot + event.getOldPath()), Paths.get(siteRoot + catalog.getPath()),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						e.printStackTrace();
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
		final String operator = event.getContent().getOperator().getUsername();
		AsyncTask task = new AsyncTask() {

			@Override
			public void run0() throws Exception {
				// 映射关联内容同步下线
				List<CmsContent> mappingList = contentService.lambdaQuery()
						.gt(CmsContent::getCopyType, ContentCopyType.Mapping)
						.eq(CmsContent::getCopyId, contentId).list();
				for (CmsContent c : mappingList) {
					if (ContentStatus.PUBLISHED == c.getStatus()) {
						try {
							contentService.deleteStaticFiles(c);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					c.setStatus(ContentStatus.OFFLINE);
					c.updateBy(operator);
				}
				contentService.updateBatchById(mappingList);
				// 标题内容同步下线
				String internalUrl = InternalUrlUtils.getInternalUrl(InternalDataType_Content.ID, contentId);
				List<CmsContent> linkList = contentService.lambdaQuery().eq(CmsContent::getLinkFlag, YesOrNo.YES)
						.eq(CmsContent::getRedirectUrl, internalUrl).list();
				for (CmsContent c : linkList) {
					c.setStatus(ContentStatus.OFFLINE);
					c.updateBy(operator);
				}
				mappingList.addAll(linkList);
				contentService.updateBatchById(mappingList);
			}
		};
		this.asyncTaskManager.execute(task);
	}
}
