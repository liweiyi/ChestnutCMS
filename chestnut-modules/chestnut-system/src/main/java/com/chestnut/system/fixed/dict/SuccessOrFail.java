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
package com.chestnut.system.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 成功/失败
 */
@Component(FixedDictType.BEAN_PREFIX + SuccessOrFail.TYPE)
public class SuccessOrFail extends FixedDictType {

	public static final String TYPE = "SuccessOrFail";
	
	public static final String SUCCESS = "0";

	public static final String FAIL = "1";

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public SuccessOrFail() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + SUCCESS + "}", SUCCESS, 1);
		super.addDictData("{DICT." + TYPE + "." + FAIL + "}", FAIL, 2);
	}
	
	public static boolean isSuccess(String v) {
		return SUCCESS.equals(v);
	}
	
	public static boolean isFail(String v) {
		return !isSuccess(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
