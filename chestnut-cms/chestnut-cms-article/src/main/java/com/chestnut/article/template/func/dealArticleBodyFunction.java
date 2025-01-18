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
package com.chestnut.article.template.func;

import com.chestnut.article.IArticleBodyFormat;
import com.chestnut.article.format.ArticleBodyFormat_RichText;
import com.chestnut.article.service.IArticleService;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Freemarker模板自定义函数：处理Html文本内容中的内部链接
 */
@Component
@RequiredArgsConstructor
public class dealArticleBodyFunction extends AbstractFunc  {

	static final String FUNC_NAME = "dealArticleBody";
	
	private static final String DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".DESC}";

	private static final String ARG1_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Name}";

	private static final String ARG2_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg2.Name}";

	private final IArticleService articleService;

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
		String format;
		if (args.length > 1) {
			String _format = ((SimpleScalar) args[1]).getAsString();
			if (StringUtils.isEmpty(_format)) {
				_format = ArticleBodyFormat_RichText.ID;
			}
			format = _format;
		} else {
			format = ArticleBodyFormat_RichText.ID;
		}
		TemplateContext context = FreeMarkerUtils.getTemplateContext(Environment.getCurrentEnvironment());
		SimpleScalar simpleScalar = (SimpleScalar) args[0];
		String contentHtml = simpleScalar.getAsString();
		IArticleBodyFormat articleBodyFormat = articleService.getArticleBodyFormat(format);
		Assert.notNull(articleBodyFormat, () -> new TemplateModelException("Unsupported article body format: " + format));
		if (Objects.isNull(articleBodyFormat)) {
			articleBodyFormat = articleService.getArticleBodyFormat(ArticleBodyFormat_RichText.ID);
		}
		contentHtml = articleBodyFormat.deal(contentHtml, context.getPublishPipeCode(), context.isPreview());
		return contentHtml;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg(ARG1_NAME, FuncArgType.String, true, null),
				new FuncArg(ARG2_NAME, FuncArgType.String, false, null, ArticleBodyFormat_RichText.ID)
		);
	}
}
