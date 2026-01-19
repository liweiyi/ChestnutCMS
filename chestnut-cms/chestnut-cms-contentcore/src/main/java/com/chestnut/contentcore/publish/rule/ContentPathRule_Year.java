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
package com.chestnut.contentcore.publish.rule;

import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.publish.IContentPathRule;
import org.springframework.stereotype.Component;

/**
 * 内容静态化目录规则：{catalog.path}/yyyy/
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IContentPathRule.BEAN_PREFIX + ContentPathRule_Year.ID)
public class ContentPathRule_Year implements IContentPathRule {

    public static final String ID = "Year";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "{CONTENT_PATH_RULE." + ID + "}";
    }

    @Override
    public String getDirectory(CmsSite site, CmsCatalog catalog, CmsContent content) {
        return catalog.getPath() + content.getPublishDate().getYear() + "/";
    }
}
