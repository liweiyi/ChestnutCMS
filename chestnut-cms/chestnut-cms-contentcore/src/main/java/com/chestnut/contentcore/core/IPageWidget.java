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

import java.io.IOException;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.contentcore.domain.CmsPageWidget;

import freemarker.template.TemplateException;

/**
 * 页面部件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IPageWidget {

	/**
	 * 页面部件类型ID
	 */
	default String getPageWidgetType() {
		return this.getPageWidgetEntity().getType();
	}
	
	/**
	 * 页面部件基础数据实例
	 */
	CmsPageWidget getPageWidgetEntity();

	void setPageWidgetEntity(CmsPageWidget cmsPageWdiget);

	/**
	 * 操作人
	 * 
	 * @param loginUser
	 */
	void setOperator(LoginUser loginUser);

	LoginUser getOperator();

	void add();

	void save();

	void delete();

	void publish() throws TemplateException, IOException;
}
