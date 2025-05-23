/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
 * 内容状态
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(FixedDictType.BEAN_PREFIX + ContentStatus.TYPE)
public class ContentStatus extends FixedDictType {

	public static final String TYPE = "CMSContentStatus";

	public static final String DRAFT = "0"; // 初稿

	public static final String TO_PUBLISHED = "20"; // 待发布

	public static final String PUBLISHED = "30"; // 已发布

	public static final String OFFLINE = "40"; // 已下线

	public static final String EDITING = "60"; // 重新编辑

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public ContentStatus() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + DRAFT + "}", DRAFT, 1);
		super.addDictData("{DICT." + TYPE + "." + TO_PUBLISHED + "}", TO_PUBLISHED, 2);
		super.addDictData("{DICT." + TYPE + "." + PUBLISHED + "}", PUBLISHED, 3);
		super.addDictData("{DICT." + TYPE + "." + OFFLINE + "}", OFFLINE, 4);
		super.addDictData("{DICT." + TYPE + "." + EDITING + "}", EDITING, 5);
	}

	public static boolean isDraft(String status) {
		return DRAFT.equals(status);
	}

	public static boolean isPublished(String v) {
		return PUBLISHED.equals(v);
	}

	public static boolean isToPublish(String v) {
		return TO_PUBLISHED.equals(v);
	}

	public static boolean isToPublishOrPublished(String v) {
		return PUBLISHED.equals(v) || TO_PUBLISHED.equals(v);
	}

	public static boolean isOffline(String v) {
		return OFFLINE.equals(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}

	public static List<String> all() {
		return List.of(DRAFT, TO_PUBLISHED, PUBLISHED, OFFLINE, EDITING);
	}
}
