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
