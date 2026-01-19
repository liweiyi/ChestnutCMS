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
package com.chestnut.common.staticize.func.impl;

import com.chestnut.common.staticize.exception.InvalidFunctionArgumentException;
import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.NumberUtils;
import com.chestnut.common.utils.StringUtils;
import freemarker.template.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Freemarker模板自定义函数：JAVA对象转JSON字符串
 */
@Component
@RequiredArgsConstructor
public class DisplayFileSizeFunction extends AbstractFunc  {

	static final String FUNC_NAME = "displayFileSize";

	private static final String DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".DESC}";

	private static final String ARG1_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Name}";

	private static final String ARG1_DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Desc}";

	@Override
	public String getFuncName() {
		return FUNC_NAME;
	}

	@Override
	public String getDesc() {
		return DESC;
	}

	@Override
	public String supportVersion() {
		return "V1.5.10+";
	}

	@Override
	public Object exec0(Object... args) throws TemplateModelException {
		if (StringUtils.isEmpty(args)) {
			return StringUtils.EMPTY;
		}
        Number fileSize;
        if (args[0] instanceof SimpleNumber simpleNumber) {
            fileSize = simpleNumber.getAsNumber();
        } else if (args[0] instanceof SimpleScalar simpleScalar) {
            fileSize = NumberUtils.createNumber(simpleScalar.getAsString());
        } else {
            throw new InvalidFunctionArgumentException(FUNC_NAME, 0, args[0].toString());
        }
        return FileUtils.byteCountToDisplaySize(fileSize);
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg(ARG1_NAME, FuncArgType.Array, true, ARG1_DESC)
		);
	}
}
