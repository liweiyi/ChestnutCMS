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
package com.chestnut.cms.stat.template.tag;

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import com.ulisesbocchio.jasyptspringboot.annotation.ConditionalOnMissingBean;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Component
@ConditionalOnMissingClass("com.chestnut.cms.statpro.template.tag.CmsStatTag")
public class CmsStatTag extends AbstractTag {

	public final static String TAG_NAME = "cms_stat";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";

	@Override
	public String getTagName() {
		return TAG_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESC;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		String apiPrefix = FreeMarkerUtils.getStringVariable(env, TemplateUtils.TemplateVariable_ApiPrefix);
		Long siteId = TemplateUtils.evalSiteId(env);
		String statApi = apiPrefix + "api/stat/visit?sid=" + siteId;
		if (Objects.nonNull(env.getVariable(TemplateUtils.TemplateVariable_Catalog))) {
			Long catalogId = TemplateUtils.evalCatalogId(env);
			if (IdUtils.validate(catalogId)) {
				statApi += "&cid=" + catalogId;
			}
		}
		if (Objects.nonNull(env.getVariable(TemplateUtils.TemplateVariable_Content))) {
			Long contentId = TemplateUtils.evalContentId(env);
			if (IdUtils.validate(contentId)) {
				statApi += "&id=" + contentId;
			}
		}
		String script = "<script type=\"text/javascript\">(function(){var s=document.createElement(\"script\");s.src=\"%s\";(document.getElementsByTagName(\"head\")[0] || document.getElementsByTagName(\"body\")[0]).appendChild(s);})();</script>".formatted(statApi);
		env.getOut().write(script);
		return null;
	}
}
