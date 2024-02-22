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

import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.dto.PageWidgetAddDTO;
import com.chestnut.contentcore.domain.dto.PageWidgetEditDTO;
import com.chestnut.contentcore.domain.vo.PageWidgetVO;

/**
 * 页面部件类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IPageWidgetType {
	
	/**
	 * Bean名称前缀
	 */
	String BEAN_NAME_PREFIX = "PageWidgetType_";

	/**
	 * 唯一标识：类型Id
	 */
	String getId();
	
	/**
	 * 类型名
	 */
	String getName();
	
	/**
	 * 类型图标
	 */
	String getIcon();
	
	/**
	 * 获取路由地址
	 */
	String getRoute();

	/**
	 * 加载部件数据详情
	 * 
	 * @param pageWidget
	 * @return
	 */
	IPageWidget loadPageWidget(CmsPageWidget pageWidget);

	
	default Class<?> getAddDTOClass() {
		return PageWidgetAddDTO.class;
	}

	default Class<?> getEditDTOClass() {
		return PageWidgetEditDTO.class;
	}

	/**
	 * 页面部件数据转VO
	 * 
	 * @param pageWidget
	 * @return
	 */
	PageWidgetVO getPageWidgetVO(CmsPageWidget pageWidget);

	/**
	 * 创建页面部件实例
	 * 
	 * @return
	 */
	IPageWidget newInstance();

	/**
	 * 获取模板页面部件内容
	 * 
	 * @param pageWidget
	 * @param isPreview
	 * @return
	 */
	Object parseContent(CmsPageWidget pageWidget, String publishPipeCode, boolean isPreview);
}
