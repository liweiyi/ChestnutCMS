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
package com.chestnut.cms.member.utils;

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.StringUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class CmsMemberUtils {

    public static String getAccountUrl(Long memberId, String type, Environment env, boolean includeBaseArgs) throws TemplateModelException {
        String url;
        Long siteId = FreeMarkerUtils.evalLongVariable(env, "Site.siteId");
        TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
        if (context.isPreview()) {
            url = FreeMarkerUtils.evalStringVariable(env, "ApiPrefix")
                    + "account/" + memberId + "?sid=" + siteId + "&pp=" + context.getPublishPipeCode() + "&preview=true";
        } else {
            url = FreeMarkerUtils.evalStringVariable(env, "Prefix") + "account/" + memberId;
            if (includeBaseArgs) {
                url += "?sid=" + siteId + "&pp=" + context.getPublishPipeCode();
            }
        }
        if (StringUtils.isNotEmpty(type)) {
            url += (url.indexOf("?") > -1 ? "&" : "?") + "type=" + type;
        }
        return url;
    }
}
