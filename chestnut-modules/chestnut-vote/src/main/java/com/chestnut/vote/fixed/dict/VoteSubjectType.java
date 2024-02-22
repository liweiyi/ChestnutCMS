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
package com.chestnut.vote.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 问卷调查主题类型
 */
@Component(FixedDictType.BEAN_PREFIX + VoteSubjectType.TYPE)
public class VoteSubjectType extends FixedDictType {

	public static final String TYPE = "VoteSubjectType";

	public static final String RADIO = "radio"; // 单选

	public static final String CHECKBOX = "checkbox"; // 多选

	public static final String INPUT = "input"; // 输入
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public VoteSubjectType() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + RADIO + "}", RADIO, 1);
		super.addDictData("{DICT." + TYPE + "." + CHECKBOX + "}", CHECKBOX, 2);
		super.addDictData("{DICT." + TYPE + "." + INPUT + "}", INPUT, 3);
	}
	
	public static boolean isInput(String type) {
		return INPUT.equals(type);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
