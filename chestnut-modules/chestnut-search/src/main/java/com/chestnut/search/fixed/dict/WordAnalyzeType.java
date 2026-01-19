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
package com.chestnut.search.fixed.dict;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;
import org.springframework.stereotype.Component;

/**
 * 分词方式
 */
@Component(FixedDictType.BEAN_PREFIX + WordAnalyzeType.TYPE)
public class WordAnalyzeType extends FixedDictType {

	public static final String TYPE = "WordAnalyzeType";

	public static final String IKSmart = "ik_smart";

	public static final String IKMaxWord = "ik_max_word";

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public WordAnalyzeType() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + IKSmart + "}", IKSmart, 1);
		super.addDictData("{DICT." + TYPE + "." + IKMaxWord + "}", IKMaxWord, 2);
	}
}
