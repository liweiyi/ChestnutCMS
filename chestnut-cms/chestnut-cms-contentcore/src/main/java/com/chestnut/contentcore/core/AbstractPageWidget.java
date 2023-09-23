package com.chestnut.contentcore.core;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.fixed.dict.PageWidgetStatus;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.contentcore.service.IPublishService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.SiteUtils;

import freemarker.template.TemplateException;

public abstract class AbstractPageWidget implements IPageWidget {

	private CmsPageWidget entity;

	private ISiteService siteService;

	private ICatalogService catalogService;

	private IPageWidgetService pageWidgetService;

	private IPublishService publishService;

	private LoginUser operator;

	@Override
	public CmsPageWidget getPageWidgetEntity() {
		return this.entity;
	}

	@Override
	public void setPageWidgetEntity(CmsPageWidget cmsPageWdiget) {
		this.entity = cmsPageWdiget;
	}

	@Override
	public void setOperator(LoginUser loginUser) {
		this.operator = loginUser;
	}

	@Override
	public LoginUser getOperator() {
		return operator;
	}

	@Override
	public void add() {
		CmsPageWidget pageWidgetEntity = this.getPageWidgetEntity();
		pageWidgetEntity.setPageWidgetId(IdUtils.getSnowflakeId());
		pageWidgetEntity.setState(PageWidgetStatus.DRAFT);
		pageWidgetEntity.createBy(this.getOperator().getUsername());
		this.getPageWidgetService().save(pageWidgetEntity);
	}

	@Override
	public void save() {
		CmsPageWidget pageWidgetEntity = this.getPageWidgetEntity();
		CmsPageWidget dbPageWidget = this.getPageWidgetService().getById(this.getPageWidgetEntity().getPageWidgetId());
		BeanUtils.copyProperties(this.getPageWidgetEntity(), dbPageWidget);
		pageWidgetEntity.setState(PageWidgetStatus.EDITING);
		pageWidgetEntity.updateBy(this.getOperator().getUsername());
		this.getPageWidgetService().updateById(pageWidgetEntity);
	}

	@Override
	public void delete() {
		this.getPageWidgetService().removeById(this.getPageWidgetEntity());
		try {
			// 删除静态文件
			FileUtils.delete(new File(this.getStaticFilePath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void publish() throws TemplateException, IOException {
		CmsPageWidget pageWidgetEntity = this.getPageWidgetEntity();
		pageWidgetEntity.setState(PageWidgetStatus.PUBLISHED);
		pageWidgetEntity.updateBy(this.getOperator().getUsername());
		this.getPageWidgetService().updateById(pageWidgetEntity);
		
		this.getPublishService().pageWidgetStaticize(this);
	}
	
	public String getStaticFilePath() {
		CmsSite site = this.getSiteService().getSite(this.getPageWidgetEntity().getSiteId());
		String siteRoot = SiteUtils.getSiteRoot(site, this.getPageWidgetEntity().getPublishPipeCode());
		return siteRoot + this.getPageWidgetEntity().getPath() + this.getPageWidgetEntity().getPageWidgetId()
				+ StringUtils.DOT + site.getStaticSuffix(this.getPageWidgetEntity().getPublishPipeCode());
	}
	
	public ISiteService getSiteService() {
		if (this.siteService == null) {
			this.siteService = SpringUtils.getBean(ISiteService.class);
		}
		return siteService;
	}

	public void setSiteService(ISiteService siteService) {
		this.siteService = siteService;
	}

	public ICatalogService getCatalogService() {
		if (this.catalogService == null) {
			this.catalogService = SpringUtils.getBean(ICatalogService.class);
		}
		return catalogService;
	}

	public void setCatalogService(ICatalogService catalogService) {
		this.catalogService = catalogService;
	}

	public IPageWidgetService getPageWidgetService() {
		if (this.pageWidgetService == null) {
			this.pageWidgetService = SpringUtils.getBean(IPageWidgetService.class);
		}
		return pageWidgetService;
	}

	public void setPageWidgetService(IPageWidgetService pageWidgetService) {
		this.pageWidgetService = pageWidgetService;
	}

	public IPublishService getPublishService() {
		if (this.publishService == null) {
			this.publishService = SpringUtils.getBean(IPublishService.class);
		}
		return publishService;
	}

	public void setPublishService(IPublishService publishService) {
		this.publishService = publishService;
	}
}
