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

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.IPublishService;

import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

/**
 * 内部数据类型：内容
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IInternalDataType.BEAN_NAME_PREFIX + InternalDataType_Content.ID)
public class InternalDataType_Content implements IInternalDataType {

    public final static String ID = "content";

    private final IContentService contentService;

    private final IPublishService publishService;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getPageData(RequestData requestData) throws IOException, TemplateException {
        CmsContent content = contentService.getById(requestData.getDataId());
        Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", requestData.getDataId()));

        return this.publishService.getContentPageData(content, requestData.getPageIndex(), requestData.getPublishPipeCode(), requestData.isPreview());
    }

    @Override
    public String getLink(InternalURL internalUrl, int pageIndex, String publishPipeCode, boolean isPreview) {
        CmsContent content = contentService.getById(internalUrl.getId());
        Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", internalUrl.getId()));

        return this.contentService.getContentLink(content, 1, publishPipeCode, isPreview);
    }
}
