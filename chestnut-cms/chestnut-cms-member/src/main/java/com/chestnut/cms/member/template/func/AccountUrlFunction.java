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
package com.chestnut.cms.member.template.func;

import com.chestnut.cms.member.utils.CmsMemberUtils;
import com.chestnut.common.staticize.exception.InvalidFunctionArgumentException;
import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ObjectUtils;
import com.chestnut.common.utils.StringUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Freemarker模板自定义函数：清理html标签
 */
@Component
@RequiredArgsConstructor
public class AccountUrlFunction extends AbstractFunc  {

	private static final String FUNC_NAME = "accountUrl";

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
		if (args.length < 2 || ObjectUtils.isAnyNull(args)) {
			return StringUtils.EMPTY;
		}
		boolean includeBaseArgs = true;
		if (args.length > 2) {
			includeBaseArgs = ((TemplateBooleanModel) args[2]).getAsBoolean();
		}
		long memberId;
		if (args[0] instanceof  TemplateNumberModel tnm) {
			memberId = tnm.getAsNumber().longValue();
		} else {
			memberId = Long.parseLong(args[0].toString());
		}
		String type = args[1].toString();
		if (!IdUtils.validate(memberId)) {
			throw  new InvalidFunctionArgumentException(FUNC_NAME, 0, String.valueOf(memberId));
		}
		return CmsMemberUtils.getAccountUrl(memberId, type, Environment.getCurrentEnvironment(), includeBaseArgs);
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg(ARG1_NAME, FuncArgType.Long, true),
				new FuncArg(ARG2_NAME, FuncArgType.String, true),
				new FuncArg(ARG3_NAME, FuncArgType.String, false, null, Boolean.TRUE.toString()));
	}
}
