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
package com.chestnut.customform;

import com.chestnut.common.utils.ReflectASMUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.fixed.config.SiteApiUrl;
import com.chestnut.contentcore.properties.SiteApiUrlProperty;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.customform.domain.CmsCustomForm;

import java.util.Map;

/**
 * 自定义表单常量
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class CustomFormConsts {

    public static final String STATICIZE_DIRECTORY = "include/customform/";

    public static final String TemplateVariable_CustomForm = "CustomForm";

    public static String getCustomFormActionUrl(CmsSite site, String publishPipeCode) {
        return SiteApiUrlProperty.getValue(site, publishPipeCode) + "api/customform/submit";
    }

    public static Map<String, Object> getCustomFormVariables(CmsCustomForm form, CmsSite site, String publishPipeCode) {
        Map<String, Object> map = ReflectASMUtils.beanToMap(form);
        map.put("action", getCustomFormActionUrl(site, publishPipeCode));
        return map;
    }
}
