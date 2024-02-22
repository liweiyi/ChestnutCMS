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
 * 问卷调查状态
 */
@Component(FixedDictType.BEAN_PREFIX + VoteStatus.TYPE)
public class VoteStatus extends FixedDictType {

	public static final String TYPE = "VoteStatus";

	public static final String NORMAL = "0"; // 正常

	public static final String STOP = "1"; // 停用

	public static final String CLOSE = "2"; // 关闭，关闭的问卷调查表示归档不可再变更
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public VoteStatus() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + NORMAL + "}", NORMAL, 1);
		super.addDictData("{DICT." + TYPE + "." + STOP + "}", STOP, 2);
		super.addDictData("{DICT." + TYPE + "." + CLOSE + "}", CLOSE, 3);
	}
	
	public static boolean isClosed(String status) {
		return CLOSE.equals(status);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
