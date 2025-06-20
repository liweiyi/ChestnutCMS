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
package com.chestnut.block;

import com.chestnut.block.domain.vo.ManualPageWidgetVO;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IPageWidget;
import com.chestnut.contentcore.core.IPageWidgetType;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.vo.PageWidgetVO;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 手动控制区块页面部件<br/>
 * 此区块内容支持多行多列自定义控制
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IPageWidgetType.BEAN_NAME_PREFIX + ManualPageWidgetType.ID)
public class ManualPageWidgetType implements IPageWidgetType {

	public final static String ID = "manual";
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
		return "/cms/block/manual/editor";
	}

	@Override
	public IPageWidget loadPageWidget(CmsPageWidget cmsPageWidget) {
		ManualPageWidget pw = new ManualPageWidget();
		pw.setPageWidgetEntity(cmsPageWidget);
		return pw;
	}
	
	@Override
	public IPageWidget newInstance() {
		return new ManualPageWidget();
	}
	
	@Override
	public PageWidgetVO getPageWidgetVO(CmsPageWidget pageWidget) {
		ManualPageWidgetVO vo = new ManualPageWidgetVO();
		BeanUtils.copyProperties(pageWidget, vo);
		vo.setContent(this.parseContent(pageWidget, null, true));
		return vo;
	}
	
	@Override
	public List<RowData> parseContent(CmsPageWidget pageWidget, String publishPipeCode, boolean isPreview) {
		List<RowData> list = null;
		if (StringUtils.isNotEmpty(pageWidget.getContent())) {
			list = JacksonUtils.fromList(pageWidget.getContent(), RowData.class);
		}
		if (list == null) {
			list = List.of();
		}
		list.forEach(rd -> rd.getItems().forEach(item -> {
			item.setLogoSrc(InternalUrlUtils.getActualUrl(item.logo, publishPipeCode, isPreview));
			item.setLink(item.getUrl());
		}));
		return list;
	}
	

	@Getter
	@Setter
	public static class RowData {
		
		private List<ItemData> items;
	}

	@Getter
	@Setter
	public static class ItemData {
		
		private String title;
		
		private String titleStyle;
		
		private String summary;

		private String url;

		@XComment("与url字段同值，仅为习惯添加")
		private String link;
		
		private String logo;

		// TODO 下个大版本移除，在模板使用${iurl(logo)}
		@Deprecated(forRemoval = true)
		private String logoSrc;
		
		private LocalDateTime publishDate;
	}
}
