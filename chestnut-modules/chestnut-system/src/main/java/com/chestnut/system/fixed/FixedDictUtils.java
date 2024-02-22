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
package com.chestnut.system.fixed;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.chestnut.common.utils.SpringUtils;

/**
 * 固定字典项工具类
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class FixedDictUtils {

	private static final Map<String, FixedDictType> fixedDictTypes = SpringUtils.getBeanMap(FixedDictType.class);

	public static FixedDictType getFixedDictType(String dictType) {
		return fixedDictTypes.get(FixedDictType.BEAN_PREFIX + dictType);
	}
	
	public static boolean isFixedDictType(String dictType) {
		return fixedDictTypes.containsKey(FixedDictType.BEAN_PREFIX + dictType);
	}
	
	public static boolean isFixedDictData(String dictType, String dictValue) {
		FixedDictType dt = getFixedDictType(dictType);
		if (Objects.nonNull(dt)) {
			return dt.getDataList().stream().anyMatch(d -> d.getValue().equals(dictValue));
		}
		return false;
	}

	public static List<FixedDictType> allFixedDicts() {
		return fixedDictTypes.values().stream().toList();
	}
}
