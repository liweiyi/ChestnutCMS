/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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

import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPublishService;
import com.chestnut.system.fixed.dict.YesOrNo;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * 内部数据类型：栏目
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IInternalDataType.BEAN_NAME_PREFIX + InternalDataType_Catalog.ID)
public class InternalDataType_Catalog implements IInternalDataType {

	public final static String ID = "catalog";
	
	private final ICatalogService catalogService;
	
	private final IPublishService publishService;
	
	@Override
	public String getId() {
		return ID;
	}

    @Override
    public boolean supportSlot() {
        return true;
    }

    @Override
	public String getPageData(RequestData requestData) throws IOException, TemplateException {
		CmsCatalog catalog = catalogService.getCatalog(requestData.getDataId());
		boolean listFlag = YesOrNo.isYes(requestData.getParams().get("list"));
		return this.publishService.getCatalogPageData(catalog, requestData, listFlag);
	}

	@Override
	public void processPageData(RequestData requestData, Writer writer) throws TemplateException, IOException {
		CmsCatalog catalog = catalogService.getCatalog(requestData.getDataId());
		boolean listFlag = YesOrNo.isYes(requestData.getParams().get("list"));
        this.publishService.processCatalogPage(catalog, requestData, listFlag, writer);

    }

    @Override
    public String getStaticPath(Long dataId, String publishPipeCode) {
        CmsCatalog catalog = this.catalogService.getCatalog(dataId);
        Assert.notNull(catalog,  () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(dataId));
        if (CatalogType_Link.ID.equals(catalog.getCatalogType())) {
            return catalog.getLink(); // 链接栏目无静态内容返回空
        }
        return catalog.getPath();
    }

    @Override
	public String getLink(InternalURL internalUrl, int pageIndex, String publishPipeCode, boolean isPreview) {
		CmsCatalog catalog = catalogService.getCatalog(internalUrl.getId());
		return this.catalogService.getCatalogLink(catalog, pageIndex, publishPipeCode, isPreview);
	}

	@Override
	public boolean isLinkData(Long id) {
		CmsCatalog catalog = catalogService.getCatalog(id);
		if (Objects.isNull(catalog)) {
			return false;
		}
		return CatalogType_Link.ID.equals(catalog.getCatalogType());
	}
}
