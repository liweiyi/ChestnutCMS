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
package com.chestnut.system.fixed.dict;

import com.chestnut.common.utils.ArrayUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * TRUE/FALSE
 */
@Component(FixedDictType.BEAN_PREFIX + TrueOrFalse.TYPE)
public class TrueOrFalse extends FixedDictType {

	public static final String TYPE = "TrueOrFalse";

	public static final String TRUE = "1";

	public static final String FALSE = "0";

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public TrueOrFalse() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + TRUE + "}", TRUE, 1);
		super.addDictData("{DICT." + TYPE + "." + FALSE + "}", FALSE, 2);
	}
	
	public static boolean isTrue(String v) {
		return TRUE.equals(v);
	}
	
	public static boolean isFalse(String v) {
		return !isTrue(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}

	public static boolean valid(String v) {
		return ArrayUtils.contains(v, TRUE, FALSE);
	}
}
