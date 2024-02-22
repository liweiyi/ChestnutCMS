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

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.impl.PublishPipeProp_ContentExTemplate;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;

public class ContentUtils {

    /**
     * 获取内容扩展模板静态化相对站点路径
     *
     * @return
     */
    public static String getContentExPath(CmsSite site, CmsCatalog catalog, CmsContent content, String publishPipeCode) {
        String suffix = site.getStaticSuffix(publishPipeCode);
        return catalog.getPath() + getContextExFileName(content.getContentId(), suffix);
    }

    public static String getContextExFileName(Long contentId, String suffix) {
        return contentId + "_ex." + suffix;
    }

    public static String getContentExTemplate(CmsContent content, CmsCatalog catalog, String publishPipeCode) {
        String exTemplate = PublishPipeProp_ContentExTemplate.getValue(publishPipeCode,
                content.getPublishPipeProps());
        if (StringUtils.isEmpty(exTemplate)) {
            // 无内容独立扩展模板取栏目配置
            exTemplate = PublishPipeProp_ContentExTemplate.getValue(publishPipeCode, catalog.getPublishPipeProps());
        }
        return exTemplate;
    }
}
