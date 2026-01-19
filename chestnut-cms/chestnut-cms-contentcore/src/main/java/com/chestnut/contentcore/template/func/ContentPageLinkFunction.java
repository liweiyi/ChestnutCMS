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
package com.chestnut.contentcore.template.func;

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.service.ICatalogService;
import freemarker.core.Environment;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Freemarker模板自定义函数：获取内容分页链接
 */
@Component
@RequiredArgsConstructor
public class ContentPageLinkFunction extends AbstractFunc {

	private static final String FUNC_NAME = "contentPageLink";
	
	private static final String DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".DESC}";

	private static final String ARG1_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Name}";

	private static final String ARG2_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg2.Name}";

	private final ICatalogService catalogService;

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
		if (args.length == 0) {
			return StringUtils.EMPTY;
		}
		String link = args[0].toString();
		if (args.length == 1) {
			return link;
		}
		int pageNumber = ((SimpleNumber) args[1]).getAsNumber().intValue();
		if(pageNumber <= 1) {
			return link;
		}
		TemplateContext context = FreeMarkerUtils.getTemplateContext(Environment.getCurrentEnvironment());
		if (context.isPreview()) {
			link += "&pi=" + pageNumber;
		} else {
			int dotIndex = link.lastIndexOf(StringUtils.DOT);
			link = link.substring(0, dotIndex) + "_" + pageNumber + link.substring(dotIndex);
		}
		return link;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg(ARG1_NAME, FuncArgType.String, true),
				new FuncArg(ARG2_NAME, FuncArgType.Int, true)
		);
	}
}
