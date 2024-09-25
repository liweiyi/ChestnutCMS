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
package com.chestnut.common.staticize.func.impl;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.HtmlUtils;
import com.chestnut.common.utils.StringUtils;
import freemarker.ext.beans.BooleanModel;
import freemarker.template.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Freemarker模板自定义函数：Html特殊字符串反转义
 */
@Component
@RequiredArgsConstructor
public class HtmlUnescapeFunction extends AbstractFunc  {

	static final String FUNC_NAME = "htmlUnescape";

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
		if (StringUtils.isEmpty(args)) {
			return StringUtils.EMPTY;
		}
		String str = args[0].toString();
		return HtmlUtils.htmlUnescape(str);
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg("Text", FuncArgType.String, true, null)
		);
	}
}
