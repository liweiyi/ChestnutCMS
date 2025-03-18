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
package com.chestnut.common.staticize.func.impl;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.StringUtils;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Freemarker模板自定义函数：列表分组
 */
@Component
@RequiredArgsConstructor
public class GroupByFunction extends AbstractFunc  {

	static final String FUNC_NAME = "groupBy";

	private static final String DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".DESC}";

	private static final String ARG1_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Name}";

	private static final String ARG2_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg2.Name}";

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
		String groupBy = args[1].toString();
		if (args[0] instanceof TemplateSequenceModel dataList) {
			return groupBy(dataList, groupBy);
		}
		return Map.of();
	}

	public static Map<String, List<TemplateModel>> groupBy(TemplateSequenceModel list, String groupBy) throws TemplateModelException {
		Map<String, List<TemplateModel>> map = new HashMap<>();
		for (int i = 0; i < list.size(); i++) {
			TemplateModel itemModel = list.get(i);
			if (itemModel instanceof TemplateHashModel model) {
				String key = model.get(groupBy).toString();
				List<TemplateModel> groupList = map.get(key);
				if (Objects.isNull(groupList)) {
					groupList = new ArrayList<>();
					map.put(key, groupList);
				}
				groupList.add(itemModel);
			}
		}
		return map;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg(ARG1_NAME, FuncArgType.Array, true),
				new FuncArg(ARG2_NAME, FuncArgType.String, true)
		);
	}
}
