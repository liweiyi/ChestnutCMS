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
import com.chestnut.common.utils.StringUtils;
import freemarker.template.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Freemarker模板自定义函数：最小值
 */
@Component
@RequiredArgsConstructor
public class MinFunction extends AbstractFunc  {

	static final String FUNC_NAME = "min";

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
		List<Number> numbers = readNumbers(args);
		return Collections.min(numbers, Comparator.comparing(Number::doubleValue));
	}

	static List<Number> readNumbers(Object... args) throws TemplateModelException {
		ArrayList<Number> numbers = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof SimpleNumber simpleNumber) {
                numbers.add(simpleNumber.getAsNumber());
            } else if (arg instanceof SimpleSequence simpleSequence) {
                for (int j = 0; j < simpleSequence.size(); j++) {
                    if (simpleSequence.get(j) instanceof SimpleNumber simpleNumber) {
                        numbers.add(simpleNumber.getAsNumber());
                    }
                }
            } else if (arg instanceof SimpleCollection simpleCollection) {
                TemplateModelIterator iterator = simpleCollection.iterator();
                while (iterator.hasNext()) {
                    TemplateModel next = iterator.next();
                    if (next instanceof SimpleNumber simpleNumber) {
                        numbers.add(simpleNumber.getAsNumber());
                    }
                }
            }
        }
		return numbers;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of();
	}
}
