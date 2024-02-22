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
package com.chestnut.cms.word.template.func;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.WordConstants;
import com.chestnut.word.service.ISensitiveWordService;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;

/**
 * Freemarker模板自定义函数，替换敏感词
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class ReplaceSensitiveWord extends AbstractFunc {

	private final ISensitiveWordService sensitiveWordService;

	static final String FUNC_NAME = "replaceSensitiveWord";
	
	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

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
		if (args.length < 1 || Objects.isNull(args[0])) {
			return StringUtils.EMPTY;
		}
		String text = ((SimpleScalar) args[0]).getAsString();
		String replacement = WordConstants.SENSITIVE_WORD_REPLACEMENT;
		if (args.length > 1) {
			replacement = ConvertUtils.toStr(args[1]);
		}
		return this.sensitiveWordService.replaceSensitiveWords(text, replacement);
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("待处理字符串", FuncArgType.String, true, null),
				new FuncArg("替换后的字符串", FuncArgType.String, false, "默认：" + WordConstants.SENSITIVE_WORD_REPLACEMENT));
	}
}
