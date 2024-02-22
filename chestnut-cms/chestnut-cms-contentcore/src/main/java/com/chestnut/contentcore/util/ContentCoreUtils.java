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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.contentcore.core.*;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.template.ITemplateType;

public class ContentCoreUtils {

	/**
	 * 栏目类型
	 */
	private static final Map<String, ICatalogType> CatalogTypes = SpringUtils.getBeanMap(ICatalogType.class);

	/**
	 * 内容类型
	 */
	private static final Map<String, IContentType> ContentTypes = SpringUtils.getBeanMap(IContentType.class);

	/**
	 * 内部内容类型
	 */
	private static final Map<String, IInternalDataType> InternalDataTypes = SpringUtils
			.getBeanMap(IInternalDataType.class);

	/**
	 * 模板类型
	 */
	private static final Map<String, ITemplateType> TemplateTypes = SpringUtils.getBeanMap(ITemplateType.class);

	/**
	 * 页面组件类型
	 */
	private static final Map<String, IPageWidgetType> PageWidgetTypes = SpringUtils.getBeanMap(IPageWidgetType.class);
	
	/**
	 * 资源类型
	 */
	private static final Map<String, IResourceType> ResourceTypes = SpringUtils.getBeanMap(IResourceType.class);

	/**
	 * 动态模板类型
	 */
	private static final Map<String, IDynamicPageType> DynamicPageTypes = SpringUtils.getBeanMap(IDynamicPageType.class);


	public static IDynamicPageType getDynamicPageType(String typeId) {
		IDynamicPageType dpt = DynamicPageTypes.get(IResourceType.BEAN_NAME_PREFIX + typeId);
		Assert.notNull(dpt, () -> ContentCoreErrorCode.UNSUPPORTED_DYNAMIC_PAGE_TYPE.exception(typeId));
		return dpt;
	}

	public static Collection<IDynamicPageType> getDynamicPageTypes() {
		return DynamicPageTypes.values();
	}

	public static IResourceType getResourceType(String typeId) {
		IResourceType rt = ResourceTypes.get(IResourceType.BEAN_NAME_PREFIX + typeId);
		Assert.notNull(rt, () -> ContentCoreErrorCode.UNSUPPORTED_RESOURCE_TYPE.exception(typeId));
		return rt;
	}
	
	public static List<IResourceType> getResourceTypes() {
		return ResourceTypes.values().stream().toList();
	}

	public static IPageWidgetType getPageWidgetType(String typeId) {
		IPageWidgetType pwt = PageWidgetTypes.get(IPageWidgetType.BEAN_NAME_PREFIX + typeId);
		Assert.notNull(pwt, () -> ContentCoreErrorCode.UNSUPPORTED_PAGE_WIDGET_TYPE.exception(typeId));
		return pwt;
	}

	public static ITemplateType getTemplateType(String typeId) {
		return TemplateTypes.get(typeId);
	}

	public static ICatalogType getCatalogType(String catalogTypeId) {
		ICatalogType ct = CatalogTypes.get(ICatalogType.BEAN_NAME_PREFIX + catalogTypeId);
		Assert.notNull(ct, () -> ContentCoreErrorCode.UNSUPPORTED_CATALOG_TYPE.exception(catalogTypeId));
		return ct;
	}

	public static Map<String, ICatalogType> getCatalogTypes() {
		return CatalogTypes;
	}

	public static IContentType getContentType(String contentTypeId) {
		IContentType ct = ContentTypes.get(IContentType.BEAN_NAME_PREFIX + contentTypeId);
		Assert.notNull(ct, () -> ContentCoreErrorCode.UNSUPPORTED_CONTENT_TYPE.exception(contentTypeId));
		return ct;
	}

	public static Map<String, IContentType> getContentTypes() {
		return ContentTypes;
	}

	public static IInternalDataType getInternalDataType(String typeId) {
		IInternalDataType idt = InternalDataTypes.get(IInternalDataType.BEAN_NAME_PREFIX + typeId);
		Assert.notNull(idt, ContentCoreErrorCode.UNSUPPORTED_INTERNAL_DATA_TYPE::exception);
		return idt;
	}

	public static Map<String, IInternalDataType> getInternalDataTypes() {
		return InternalDataTypes;
	}
}
