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
 * 国际化语言标签
 */
@Component(FixedDictType.BEAN_PREFIX + I18nDictType.TYPE)
public class I18nDictType extends FixedDictType {

	public static final String TYPE = "I18nDictType";
	
	public static final String ZH_CN = "zh-CN";
	
	public static final String EN = "en";

	public static final String ZH_TW = "zh-TW";
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);
	
	public I18nDictType() {
		super(TYPE, "{DICT." + TYPE + "}", true);
		super.addDictData("{DICT." + TYPE + "." + ZH_CN + "}", ZH_CN, 1);
		super.addDictData("{DICT." + TYPE + "." + EN + "}", EN, 2);
		super.addDictData("{DICT." + TYPE + "." + ZH_TW + "}", ZH_TW, 3);
	}
	
	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
