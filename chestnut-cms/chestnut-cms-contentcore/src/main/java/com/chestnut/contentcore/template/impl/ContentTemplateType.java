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
package com.chestnut.contentcore.template.impl;

import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.common.utils.ReflectASMUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.util.TemplateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component(ITemplateType.BEAN_NAME_PREFIX + ContentTemplateType.TypeId)
public class ContentTemplateType implements ITemplateType {

	public final static String TypeId = "Content";

	private final IContentService contentService;

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	@Override
	public String getId() {
		return TypeId;
	}

	@Override
	public void initTemplateData(Object dataId, TemplateContext context) {
		CmsContent content = this.contentService.dao().getById(ConvertUtils.toLong(dataId));
		if (StringUtils.isNotEmpty(content.getImages())) {
			content.setLogo(content.getImages().get(0));
		}
		Map<String, Object> contentMap = ReflectASMUtils.beanToMap(content);
		String link = this.contentService.getContentLink(content, 1,
				context.getPublishPipeCode(), context.isPreview());
		contentMap.put(TemplateUtils.TemplateVariable_OBJ_Link, link);
		contentMap.put("attributes", ContentAttribute.convertStr(content.getAttributes()));
		context.getVariables().put(TemplateUtils.TemplateVariable_Content, contentMap);

		CmsSite site = this.siteService.getSite(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		TemplateUtils.addCatalogVariables(site, catalog, context);
	}
}
