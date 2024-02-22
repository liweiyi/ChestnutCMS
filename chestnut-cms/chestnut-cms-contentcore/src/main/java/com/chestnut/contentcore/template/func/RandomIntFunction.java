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

import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.springframework.stereotype.Component;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.StringUtils;

import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;

/**
 * Freemarker模板自定义函数：随机数
 */
@Component
@RequiredArgsConstructor
public class RandomIntFunction extends AbstractFunc {

	static final String FUNC_NAME = "randomInt";
	
	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	private static final RandomGenerator random = RandomGeneratorFactory.getDefault().create();

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
		int origin = ((SimpleNumber) args[0]).getAsNumber().intValue();
		if (args.length == 1) {
			return random.nextInt(origin);
		} else if (args.length == 2) {
			int bound = ((SimpleNumber) args[1]).getAsNumber().intValue();
			return random.nextInt(origin, bound);
		}
		return StringUtils.EMPTY;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg("随机数范围最（大/小）值", FuncArgType.Int, true, "只有1个参数时：[0, arg[0])，2个参数时：[arg[0], arg[1])"),
				new FuncArg("随机数范围最大值", FuncArgType.Int, false, null));
	}
}
