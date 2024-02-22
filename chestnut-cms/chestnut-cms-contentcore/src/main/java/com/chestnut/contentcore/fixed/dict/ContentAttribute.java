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

import com.chestnut.common.utils.NumberUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.domain.SysDictData;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 启用/禁用
 */
@Component(FixedDictType.BEAN_PREFIX + ContentAttribute.TYPE)
public class ContentAttribute extends FixedDictType {

	public static final String TYPE = "CMSContentAttribute";

	public static final String IMAGE = "image"; // 图片

	public static final String VIDEO = "video"; // 视频

	public static final String ATTACH = "attach"; // 附件

	public static final String HOT = "hot"; // 热点

	public static final String RECOMMEND = "recommend"; // 推荐

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public ContentAttribute() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + IMAGE + "}", IMAGE, 1, "1");
		super.addDictData("{DICT." + TYPE + "." + VIDEO + "}", VIDEO, 2, "2");
		super.addDictData("{DICT." + TYPE + "." + ATTACH + "}", ATTACH, 3, "4");
		super.addDictData("{DICT." + TYPE + "." + HOT + "}", HOT, 4, "8");
		super.addDictData("{DICT." + TYPE + "." + RECOMMEND + "}", RECOMMEND, 5, "16");
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}

	/**
	 * 附加指定属性
	 * 
	 * @param sourceAttr
	 * @param appcenAttr
	 * @return
	 */
	public static int append(int sourceAttr, int appcenAttr) {
		if (sourceAttr == 0) {
			return appcenAttr;
		} else {
			if ((sourceAttr & appcenAttr) == appcenAttr) {
				return sourceAttr; // 已经包含
			}
			return (sourceAttr & ~appcenAttr) + appcenAttr;
		}
	}

	/**
	 * 移除指定属性
	 * 
	 * @param sourceAttr
	 * @param removeAttr
	 * @return
	 */
	public static int remove(int sourceAttr, int removeAttr) {
		if (sourceAttr != 0) {
			if ((sourceAttr & removeAttr) == 0) {
				return sourceAttr; // 并不包含
			}
			return sourceAttr & ~removeAttr;
		}
		return sourceAttr;
	}

	/**
	 * 字符串数组转数字
	 * 
	 * @param attributes
	 * @return
	 */
	public static int convertInt(String... attributes) {
		if (attributes == null || attributes.length == 0) {
			return 0;
		}
		Map<String, String> map = dictTypeService.selectDictDatasByType(TYPE).stream()
				.collect(Collectors.toMap(SysDictData::getDictValue, SysDictData::getRemark));
		int v = 0;
		for (String attr : attributes) {
			String bit = map.get(attr);
			if (NumberUtils.isDigits(bit)) {
				v += Integer.parseInt(bit);
			}
		}
		return v;
	}

	/**
	 * 转属性数组
	 * 
	 * @param attributes
	 * @return
	 */
	public static String[] convertStr(Integer attributes) {
		if (attributes == null || attributes == 0) {
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
		Map<Integer, String> map = dictTypeService.selectDictDatasByType(TYPE).stream()
				.collect(Collectors.toMap(d -> Integer.parseInt(d.getRemark()), SysDictData::getDictValue));
		ArrayList<Integer> binaryList = NumberUtils.getBinaryList(attributes);
		return binaryList.stream().map(map::get).toArray(String[]::new);
	}

	/**
	 * 获取指定内容属性对应的位码
	 * 
	 * @param attr
	 * @return
	 */
	public static int bit(String attr) {
		Map<String, Integer> map = dictTypeService.selectDictDatasByType(TYPE).stream()
				.collect(Collectors.toMap(SysDictData::getDictValue, d -> Integer.valueOf(d.getRemark())));
		return map.get(attr);
	}

    public static boolean hasAttribute(int attributes, String attrStr) {
		int bit = bit(attrStr);
		return (attributes & bit) == bit;
    }
}
