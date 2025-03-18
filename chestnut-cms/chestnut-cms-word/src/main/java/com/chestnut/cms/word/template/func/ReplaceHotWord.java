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
package com.chestnut.cms.word.template.func;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.common.utils.ObjectUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import com.chestnut.word.WordConstants;
import com.chestnut.word.service.IHotWordService;
import freemarker.core.Environment;
import freemarker.template.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	
	private static final String DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".DESC}";

	private static final String ARG1_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Name}";

	private static final String ARG2_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg2.Name}";

	private static final String ARG3_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg3.Name}";
	
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
		ArrayList<String> hotGroupCodes = getHotGroupCodes(args[1]);
		Long siteId = TemplateUtils.evalSiteId(Environment.getCurrentEnvironment());

		String replacementTemplate = null;
		if (args.length == 3) {
			replacementTemplate = ConvertUtils.toStr(args[2]);
		}
		return this.hotWordService.replaceHotWords(text, siteId.toString(),
				hotGroupCodes.toArray(String[]::new), null, replacementTemplate);
	}

	private ArrayList<String> getHotGroupCodes(Object arg) throws TemplateModelException {
		ArrayList<String> groupCodes = new ArrayList<>();
		if (arg instanceof SimpleScalar simpleScalar) {
			String[] split = StringUtils.split(simpleScalar.getAsString(), StringUtils.COMMA);
            groupCodes.addAll(Arrays.asList(split));
		} else if (arg instanceof SimpleSequence simpleSequence) {
			for (int j = 0; j < simpleSequence.size(); j++) {
				if (simpleSequence.get(j) instanceof SimpleScalar simpleScalar) {
					groupCodes.add(simpleScalar.getAsString());
				}
			}
		} else if (arg instanceof SimpleCollection simpleCollection) {
			TemplateModelIterator iterator = simpleCollection.iterator();
			while (iterator.hasNext()) {
				TemplateModel next = iterator.next();
				if (next instanceof SimpleScalar simpleScalar) {
					groupCodes.add(simpleScalar.getAsString());
				}
			}
		}
		return groupCodes;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg(ARG1_NAME, FuncArgType.String, true, null),
				new FuncArg(ARG2_NAME, FuncArgType.String, true),
				new FuncArg(ARG3_NAME, FuncArgType.String, false, null, WordConstants.HOT_WORD_REPLACEMENT));
	}
}
