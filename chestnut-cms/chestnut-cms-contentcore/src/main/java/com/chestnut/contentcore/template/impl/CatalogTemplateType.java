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
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.CatalogPageSizeProperty;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.ISiteService;
import org.springframework.stereotype.Component;

import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.util.TemplateUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(ITemplateType.BEAN_NAME_PREFIX + CatalogTemplateType.TypeId)
public class CatalogTemplateType implements ITemplateType {
	
	public final static String TypeId = "CatalogIndex";

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	@Override
	public String getId() {
		return TypeId;
	}

	@Override
	public void initTemplateData(Object dataId, TemplateContext context) {
		CmsCatalog catalog = this.catalogService.getCatalog(ConvertUtils.toLong(dataId));
		CmsSite site = this.siteService.getSite(catalog.getSiteId());
		TemplateUtils.addCatalogVariables(site, catalog, context);

		int pageSize = CatalogPageSizeProperty.getValue(catalog.getConfigProps(), site.getConfigProps());
		context.setPageSize(pageSize);
	}
}
