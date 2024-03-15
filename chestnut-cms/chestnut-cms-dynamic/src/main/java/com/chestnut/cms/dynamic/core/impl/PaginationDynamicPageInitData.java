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
package com.chestnut.cms.dynamic.core.impl;

import com.chestnut.cms.dynamic.core.IDynamicPageInitData;
import com.chestnut.common.staticize.core.TemplateContext;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * PaginationDynamicPageInitData
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IDynamicPageInitData.BEAN_PREFIX + PaginationDynamicPageInitData.TYPE)
public class PaginationDynamicPageInitData implements IDynamicPageInitData {

    public static final String TYPE = "Pagination";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "{DYNAMIC_PAGE_INIT_DATA." + TYPE + "}";
    }

    @Override
    public void initTemplateData(TemplateContext context, Map<String, String> parameters) {
        int pageIndex = MapUtils.getIntValue(parameters, "pi", 1);
        context.setPageIndex(pageIndex);
    }
}
