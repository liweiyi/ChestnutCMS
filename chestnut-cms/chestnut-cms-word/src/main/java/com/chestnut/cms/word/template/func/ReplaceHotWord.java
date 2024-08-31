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

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.contentcore.util.TemplateUtils;
import freemarker.core.Environment;
import org.springframework.stereotype.Component;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.common.utils.ObjectUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.WordConstants;
import com.chestnut.word.service.IHotWordService;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;

/**
 * Freemarker模板自定义函数，添加热词链接
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class ReplaceHotWord extends AbstractFunc  {

	private final IHotWordService hotWordService;
	
	static final String FUNC_NAME = "replaceHotWord";
	
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
		if (args.length < 2 || ObjectUtils.isAnyNull(args[0], args[1])) {
			return StringUtils.EMPTY;
		}
		String text = ((SimpleScalar) args[0]).getAsString();
		if (StringUtils.isEmpty(text)) {
			return StringUtils.EMPTY;
		}
		String groupCode = ((SimpleScalar) args[1]).getAsString();
		if (StringUtils.isEmpty(groupCode)) {
			return text;
		}
		String[] codes = StringUtils.split(groupCode, StringUtils.COMMA);
		Long siteId = TemplateUtils.evalSiteId(Environment.getCurrentEnvironment());

		String replacementTemplate = null;
		if (args.length == 3) {
			replacementTemplate = ConvertUtils.toStr(args[2]);
		}
		return this.hotWordService.replaceHotWords(text, siteId.toString(), codes, null, replacementTemplate);
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("待处理字符串", FuncArgType.String, true, null),
				new FuncArg("热词分组编码", FuncArgType.String, true, "多个热词分组用因为逗号分隔"),
				new FuncArg("自定义替换模板", FuncArgType.String, false, "默认：" + WordConstants.HOT_WORD_REPLACEMENT));
	}
}
