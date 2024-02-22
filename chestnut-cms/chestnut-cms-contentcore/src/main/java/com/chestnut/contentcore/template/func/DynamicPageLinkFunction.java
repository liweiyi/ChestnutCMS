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
package com.chestnut.contentcore.template.func;

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IDynamicPageType;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.util.TemplateUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Freemarker模板自定义函数：处理内部链接
 */
@Component
@RequiredArgsConstructor
public class DynamicPageLinkFunction extends AbstractFunc  {

	static final String FUNC_NAME = "dynamicPageLink";

	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	private final Map<String, IDynamicPageType> dynamicPageTypeMap;

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
		boolean ignoreBaseArg = true;
		if (args.length == 2) {
			ignoreBaseArg = ((TemplateBooleanModel) args[1]).getAsBoolean();
		}
		TemplateContext context = FreeMarkerUtils.getTemplateContext(Environment.getCurrentEnvironment());
		IDynamicPageType dynamicPageType = this.dynamicPageTypeMap.get(IDynamicPageType.BEAN_PREFIX + args[0].toString());
		Assert.notNull(dynamicPageType, () -> ContentCoreErrorCode.UNSUPPORTED_DYNAMIC_PAGE_TYPE.exception(args[0].toString()));

		String path = dynamicPageType.getRequestPath();
		if (context.isPreview()) {
			path += "?preview=true";
			path = TemplateUtils.appendTokenParameter(path, Environment.getCurrentEnvironment());
		}
		if (!ignoreBaseArg) {
			path += (path.contains("?") ? "&" : "?") + "sid=" + FreeMarkerUtils.evalLongVariable(Environment.getCurrentEnvironment(), "Site.siteId")
					+ "&pp=" + context.getPublishPipeCode();
		}
		return path;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg("动态页面类型", FuncArgType.String, true, null),
				new FuncArg("忽略sid/pp参数", FuncArgType.String, false, "默认：true")
		);
	}
}
