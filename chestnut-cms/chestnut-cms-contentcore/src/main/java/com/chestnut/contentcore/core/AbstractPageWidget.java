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
package com.chestnut.contentcore.core;

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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
@Getter
@Setter
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
			log.error("Delete file failed: {}", this.getStaticFilePath());
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

	public ICatalogService getCatalogService() {
		if (this.catalogService == null) {
			this.catalogService = SpringUtils.getBean(ICatalogService.class);
		}
		return catalogService;
	}

	public IPageWidgetService getPageWidgetService() {
		if (this.pageWidgetService == null) {
			this.pageWidgetService = SpringUtils.getBean(IPageWidgetService.class);
		}
		return pageWidgetService;
	}

	public IPublishService getPublishService() {
		if (this.publishService == null) {
			this.publishService = SpringUtils.getBean(IPublishService.class);
		}
		return publishService;
	}
}
