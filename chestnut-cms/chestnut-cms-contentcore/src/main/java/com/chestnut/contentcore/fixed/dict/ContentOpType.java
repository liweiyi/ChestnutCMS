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
package com.chestnut.contentcore.fixed.dict;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 内容操作类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(FixedDictType.BEAN_PREFIX + ContentOpType.TYPE)
public class ContentOpType extends FixedDictType {

	public static final String TYPE = "CMSContentOpType";

	public static final String ADD = "ADD";

	public static final String UPDATE = "UPDATE";

	public static final String DELETE = "DELETE";

	public static final String LOCK = "LOCK";

	public static final String UNLOCK = "UNLOCK";

	public static final String TO_PUBLISH = "TO_PUBLISH";

	public static final String PUBLISH = "PUBLISH";

	public static final String OFFLINE = "OFFLINE";

	public static final String SORT = "SORT";

	public static final String TOP = "TOP";

	public static final String CANCEL_TOP = "CANCEL_TOP";

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public ContentOpType() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + ADD + "}", ADD, 1);
		super.addDictData("{DICT." + TYPE + "." + UPDATE + "}", UPDATE, 1);
		super.addDictData("{DICT." + TYPE + "." + DELETE + "}", DELETE, 1);
		super.addDictData("{DICT." + TYPE + "." + LOCK + "}", LOCK, 1);
		super.addDictData("{DICT." + TYPE + "." + UNLOCK + "}", UNLOCK, 1);
		super.addDictData("{DICT." + TYPE + "." + TO_PUBLISH + "}", TO_PUBLISH, 1);
		super.addDictData("{DICT." + TYPE + "." + PUBLISH + "}", PUBLISH, 1);
		super.addDictData("{DICT." + TYPE + "." + OFFLINE + "}", OFFLINE, 1);
		super.addDictData("{DICT." + TYPE + "." + SORT + "}", SORT, 1);
		super.addDictData("{DICT." + TYPE + "." + TOP + "}", TOP, 1);
		super.addDictData("{DICT." + TYPE + "." + CANCEL_TOP + "}", CANCEL_TOP, 1);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
