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
package com.chestnut.contentcore.util;

import java.util.Objects;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.impl.CatalogType_Link;
import com.chestnut.contentcore.core.impl.InternalDataType_Catalog;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.system.fixed.config.BackendContext;

public class CatalogUtils {
	
	/**
	 * 栏目父级ID分隔符
	 */
	public static final String ANCESTORS_SPLITER = ":";

	public static String formatCatalogPath(String path) {
		path = FileExUtils.normalizePath(path);

		if (!path.endsWith("/")) {
			path += "/";
		}
		return path;
	}
	
	/**
	 * 生成指定栏目的子栏目用的祖级字符串
	 * 
	 * @param parentAncestors
	 * @param catalogId
	 * @return
	 */
	public static String getCatalogAncestors(String parentAncestors, Long catalogId) {
		if (StringUtils.isNotEmpty(parentAncestors)) {
			return parentAncestors + ANCESTORS_SPLITER + catalogId;
		}
		return catalogId.toString();
	}

	public static String getCatalogAncestors(CmsCatalog parent, Long catalogId) {
		return getCatalogAncestors(Objects.isNull(parent) ? null : parent.getAncestors(), catalogId);
	}
	
	/**
	 * 获取指定栏目所属顶级栏目ID
	 * 
	 * @param catalog
	 * @return
	 */
	public static Long getTopCatalog(CmsCatalog catalog) {
		if (catalog.getCatalogId().toString().equals(catalog.getAncestors())) {
			return catalog.getCatalogId();
		}
		return Long.valueOf(catalog.getAncestors().substring(0, catalog.getAncestors().indexOf(ANCESTORS_SPLITER)));
	}

	/**
	 * 获取栏目访问链接
	 *
	 * @param site
	 * @param catalog
	 * @param pageIndex
	 * @param publishPipeCode
	 * @param isPreview
	 * @return
	 */
	public static String getCatalogLink(CmsSite site, CmsCatalog catalog, int pageIndex, String publishPipeCode, boolean isPreview) {
		if (catalog.getCatalogType().equals(CatalogType_Link.ID)) {
			return InternalUrlUtils.getActualUrl(catalog.getRedirectUrl(), publishPipeCode, isPreview);
		}
		if (isPreview) {
			String catalogPath = IInternalDataType.getPreviewPath(InternalDataType_Catalog.ID, catalog.getCatalogId(),
					publishPipeCode, pageIndex);
			return BackendContext.getValue() + catalogPath;
		}
		if (catalog.isStaticize()) {
			return site.getUrl(publishPipeCode) + catalog.getPath();
		} else {
			String catalogPath = IInternalDataType.getViewPath(InternalDataType_Catalog.ID, catalog.getCatalogId(),
					publishPipeCode, pageIndex);
			return site.getUrl(publishPipeCode) + catalogPath;
		}
	}

	public static String getCatalogListLink(CmsSite site, CmsCatalog catalog, int pageIndex, String publishPipeCode, boolean isPreview) {
		if (catalog.getCatalogType().equals(CatalogType_Link.ID)) {
			return InternalUrlUtils.getActualUrl(catalog.getRedirectUrl(), publishPipeCode, isPreview);
		}
		if (isPreview) {
			String catalogPath = IInternalDataType.getPreviewPath(InternalDataType_Catalog.ID, catalog.getCatalogId(),
					publishPipeCode, pageIndex);
			return BackendContext.getValue() + catalogPath + "&list=Y";
		}
		if (catalog.isStaticize()) {
			String link = site.getUrl(publishPipeCode) + catalog.getPath();
			if (StringUtils.isNotEmpty(catalog.getIndexTemplate(publishPipeCode))) {
				link = link + (pageIndex > 1 ? "list_" + pageIndex : "list") + StringUtils.DOT + site.getStaticSuffix(publishPipeCode);
			}
			return link;
		} else {
			String catalogPath = IInternalDataType.getViewPath(InternalDataType_Catalog.ID, catalog.getCatalogId(),
					publishPipeCode, pageIndex);
			return site.getUrl(publishPipeCode) + catalogPath + "&list=Y";
		}
	}
}
