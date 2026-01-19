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

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysDictData;
import com.chestnut.system.service.ISysDictTypeService;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Freemarker模板自定义函数：获取字典指定值数据
 */
@Component
@RequiredArgsConstructor
public class DictFunction extends AbstractFunc {

	private static final String FUNC_NAME = "dict";

	private static final String DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".DESC}";

	private static final String ARG1_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Name}";

	private static final String ARG2_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg2.Name}";

	private static final String ARG3_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg3.Name}";

	private final ISysDictTypeService dictTypeService;

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
			return List.of();
		}
		String dictType = args[0].toString();
		if (StringUtils.isBlank(dictType)) {
			return List.of();
		}
		String dictValue = (args.length > 1 && Objects.nonNull(args[1])) ? args[1].toString() : StringUtils.EMPTY;
		String localeKey = (args.length > 2 && Objects.nonNull(args[2])) ? args[2].toString() : StringUtils.EMPTY;
		List<SysDictData> datas = dictTypeService.selectDictDatasByType(dictType);
		if (StringUtils.isNotEmpty(localeKey)) {
			I18nUtils.replaceI18nFields(datas, Locale.forLanguageTag(localeKey));
		} else {
			I18nUtils.replaceI18nFields(datas);
		}
		I18nUtils.replaceI18nFields(datas, Locale.getDefault());
		if (StringUtils.isNotBlank(dictValue)) {
			Optional<SysDictData> first = datas.stream().filter(data -> StringUtils.equals(data.getDictValue(), dictValue)).findFirst();
			if (first.isEmpty()) {
				return List.of();
			}
			return List.of(first.get());
		}
		return datas;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg(ARG1_NAME, FuncArgType.String, true, null),
				new FuncArg(ARG2_NAME, FuncArgType.String, false, null),
				new FuncArg(ARG3_NAME, FuncArgType.String, false, null)
		);
	}
}
