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
package com.chestnut.advertisement;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.chestnut.advertisement.pojo.AdSpaceProps;
import com.chestnut.advertisement.pojo.vo.AdSpaceVO;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.core.IPageWidget;
import com.chestnut.contentcore.core.IPageWidgetType;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.vo.PageWidgetVO;

/**
 * 广告位页面部件
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IPageWidgetType.BEAN_NAME_PREFIX + AdSpacePageWidgetType.ID)
public class AdSpacePageWidgetType implements IPageWidgetType {

	public final static String ID = "ads";
	public final static String NAME = "{CMS.CONTENCORE.PAGEWIDGET." + ID + "}";
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getIcon() {
		return "el-icon-list";
	}
	
	@Override
	public String getRoute() {
		return "/cms/adspace/editor";
	}

	@Override
	public IPageWidget loadPageWidget(CmsPageWidget cmsPageWdiget) {
		AdSpacePageWidget pw = new AdSpacePageWidget();
		pw.setPageWidgetEntity(cmsPageWdiget);
		return pw;
	}
	
	@Override
	public IPageWidget newInstance() {
		return new AdSpacePageWidget();
	}
	
	@Override
	public PageWidgetVO getPageWidgetVO(CmsPageWidget pageWidget) {
		AdSpaceVO vo = new AdSpaceVO();
		BeanUtils.copyProperties(pageWidget, vo);
		vo.setContent(this.parseContent(pageWidget, null, true));
		return vo;
	}

	@Override
	public AdSpaceProps parseContent(CmsPageWidget pageWidget, String publishPipeCode, boolean isPreview) {
		return JacksonUtils.from(pageWidget.getContent(), AdSpaceProps.class);
	}
}
