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
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.service.IContentService;
import freemarker.core.Environment;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Freemarker模板自定义函数：处理发布通道内容URL
 */
@Component
@RequiredArgsConstructor
public class ContentUrlFunction extends AbstractFunc {

	private static final String FUNC_NAME = "contentUrl";
	
	private static final String DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".DESC}";

	private static final String ARG1_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Name}";

	private static final String ARG2_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg2.Name}";

	private final IContentService contentService;

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
		if (args.length != 2) {
			return StringUtils.EMPTY;
		}
		long contentId = ((SimpleNumber) args[0]).getAsNumber().longValue();
		CmsContent content = this.contentService.dao().getById(contentId);
		if (Objects.isNull(content)) {
			return StringUtils.EMPTY;
		}
		String publishPipeCode = ((SimpleScalar) args[1]).getAsString();
		TemplateContext context = FreeMarkerUtils.getTemplateContext(Environment.getCurrentEnvironment());
		return contentService.getContentLink(content, 1, publishPipeCode, context.isPreview());
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg(ARG1_NAME, FuncArgType.Long, true),
				new FuncArg(ARG2_NAME, FuncArgType.String, true));
	}
}
