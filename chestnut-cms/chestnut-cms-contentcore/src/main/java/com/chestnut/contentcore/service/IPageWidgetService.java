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
package com.chestnut.contentcore.service;

import java.io.IOException;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.core.IPageWidget;
import com.chestnut.contentcore.core.IPageWidgetType;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsPageWidget;

import freemarker.template.TemplateException;

public interface IPageWidgetService extends IService<CmsPageWidget> {

	/**
	 * 获取指定页面部件类型
	 */
	IPageWidgetType getPageWidgetType(String type);

	/**
	 * 获取页面部件类型列表
	 */
	List<IPageWidgetType> getPageWidgetTypes();
	
	/**
	 * 添加页面部件数据
	 */
	void addPageWidget(IPageWidget pw);
	
	/**
	 * 修改页面部件数据
	 */
	void savePageWidget(IPageWidget pw);
	
	/**
	 * 删除页面部件数据
	 */
	void deletePageWidgets(List<Long> pageWidgetIds, LoginUser operator);

	/**
	 * 删除栏目相关页面部件数据
	 * 
	 * @param catalog
	 */
	void deletePageWidgetsByCatalog(CmsCatalog catalog);

	/**
	 * 发布页面部件
	 */
	void publishPageWidgets(List<Long> pageWidgetId, LoginUser operator) throws TemplateException, IOException;

	/**
	 * 是否存在重复编码
	 */
	boolean checkCodeUnique(Long siteId, String code, Long pageWidgetId);

	/**
	 * 获取页面部件缓存数据
	 *
	 * @param code
	 * @return
	 */
    CmsPageWidget getPageWidget(Long siteId, String code);
}
