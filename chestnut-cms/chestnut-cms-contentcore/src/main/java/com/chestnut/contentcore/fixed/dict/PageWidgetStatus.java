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
 * 页面部件状态
 */
@Component(FixedDictType.BEAN_PREFIX + PageWidgetStatus.TYPE)
public class PageWidgetStatus extends FixedDictType {

	public static final String TYPE = "CMSPageWidgetStatus";

	public static final String DRAFT = "0"; // 初稿

	public static final String PUBLISHED = "30"; // 已发布

	public static final String OFFLINE = "40"; // 已下线

	public static final String EDITING = "60"; // 重新编辑

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public PageWidgetStatus() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + DRAFT + "}", DRAFT, 1);
		super.addDictData("{DICT." + TYPE + "." + PUBLISHED + "}", PUBLISHED, 2);
		super.addDictData("{DICT." + TYPE + "." + OFFLINE + "}", OFFLINE, 3);
		super.addDictData("{DICT." + TYPE + "." + EDITING + "}", EDITING, 4);
	}

	public static boolean isPublished(String v) {
		return PUBLISHED.equals(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}

}
