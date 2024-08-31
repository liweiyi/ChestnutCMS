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
package com.chestnut.contentcore.core.impl;

import com.chestnut.common.storage.local.LocalFileStorageType;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.ResourceUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

/**
 * 内部数据类型：资源
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IInternalDataType.BEAN_NAME_PREFIX + InternalDataType_Resource.ID)
public class InternalDataType_Resource implements IInternalDataType {

	public final static String ID = "resource";

	public static final String InternalUrl_Param_SiteId = "sid"; // 内部链接参数：站点ID

	public static final String InternalUrl_Param_StorageType = "st"; // 内部链接参数：存储方式

	private final ISiteService siteService;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getLink(InternalURL internalUrl, int pageIndex, String publishPipeCode, boolean isPreview) {
		long siteId = MapUtils.getLongValue(internalUrl.getParams(), InternalUrl_Param_SiteId);
		CmsSite site = this.siteService.getSite(siteId);
		String storageType = MapUtils.getString(internalUrl.getParams(), InternalUrl_Param_StorageType, LocalFileStorageType.TYPE);

		String prefix = ResourceUtils.getResourcePrefix(storageType, site, publishPipeCode, isPreview);
		return prefix + internalUrl.getPath();
	}

	/**
	 * 资源文件内部链接比较特殊，很多地方使用，路径不会变化且不缓存，不适合每次解析都从数据库读取资源信息，因此直接将路径放到内部链接上，后续解析仅需添加上站点资源地址前缀即可。
	 *
	 * @param resource 资源数据
	 */
	public static String getInternalUrl(CmsResource resource) {
		InternalURL internalURL = new InternalURL(ID, resource.getResourceId(), resource.getPath());
		internalURL.addParam(InternalUrl_Param_SiteId, resource.getSiteId().toString());
		internalURL.addParam(InternalUrl_Param_StorageType, resource.getStorageType());
		return internalURL.toIUrl();
	}
}
