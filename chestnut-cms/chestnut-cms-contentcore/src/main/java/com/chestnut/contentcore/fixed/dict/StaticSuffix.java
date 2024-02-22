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
package com.chestnut.contentcore.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 静态化文件名后缀
 */
@Component(FixedDictType.BEAN_PREFIX + StaticSuffix.TYPE)
public class StaticSuffix extends FixedDictType {

	public static final String TYPE = "CMSStaticSuffix";

	public static final String SHTML = "shtml";

	public static final String HTML = "html";

	public static final String XML = "xml";

	public static final String JSON = "json";

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public StaticSuffix() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + SHTML + "}", SHTML, 1);
		super.addDictData("{DICT." + TYPE + "." + HTML + "}", HTML, 2);
		super.addDictData("{DICT." + TYPE + "." + XML + "}", XML, 3);
		super.addDictData("{DICT." + TYPE + "." + JSON + "}", JSON, 4);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}

}
