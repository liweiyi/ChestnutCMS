/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.cms.dynamic.template.func;

import com.chestnut.cms.dynamic.service.impl.DynamicPageHelper;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Freemarker模板自定义函数：获取自定义动态模板链接
 */
@Component
@RequiredArgsConstructor
public class CustomDynamicPageLinkFunction extends AbstractFunc  {

	static final String FUNC_NAME = "customDynamicPageLink";

	private static final String DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".DESC}";

	private static final String ARG1_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Name}";

	private static final String ARG2_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg2.Name}";

	private static final String ARG3_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg3.Name}";

	private final DynamicPageHelper dynamicPageHelper;

	@Override
	public String getFuncName() {
		return FUNC_NAME;
	}

	@Override
	public String getDesc() {
		return DESC;
	}

	@Override
	public Object exec0(Object... args) throws TemplateModelException {
		if (args.length < 1) {
			return StringUtils.EMPTY;
		}
		String code = args[0].toString(); // 自动动态模板编码

		Environment env = Environment.getCurrentEnvironment();
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		Long siteId = TemplateUtils.evalSiteId(env);
		String path = this.dynamicPageHelper.getDynamicPagePath(siteId, code);
		if (StringUtils.isEmpty(path)) {
			throw new TemplateModelException("Unknown dynamic page code: " + code);
		}
		if (context.isPreview()) {
			path += "?preview=true";
			path = TemplateUtils.appendTokenParameter(path, Environment.getCurrentEnvironment());
		}
		if (args.length > 1) {
			String queryString = args[1].toString();
			path += (path.contains("?") ? "&" : "?") + queryString;
		}
		boolean ignoreBaseArg = true;
		if (args.length > 2) {
			ignoreBaseArg = ((TemplateBooleanModel) args[2]).getAsBoolean();
		}
		if (context.isPreview() || !ignoreBaseArg) {
			path += (path.contains("?") ? "&" : "?") + "sid=" + siteId + "&pp=" + context.getPublishPipeCode();
		}
		return path;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg(ARG1_NAME, FuncArgType.String, true),
				new FuncArg(ARG2_NAME, FuncArgType.String, false),
				new FuncArg(ARG3_NAME, FuncArgType.String, false, null, Boolean.TRUE.toString())
		);
	}
}
