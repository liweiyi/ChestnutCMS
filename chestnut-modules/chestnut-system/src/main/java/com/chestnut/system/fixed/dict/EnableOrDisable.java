package com.chestnut.system.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 启用/禁用
 */
@Component(FixedDictType.BEAN_PREFIX + EnableOrDisable.TYPE)
public class EnableOrDisable extends FixedDictType {

	public static final String TYPE = "EnableOrDisable";

	public static final String ENABLE = "0";

	public static final String DISABLE = "1";
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public EnableOrDisable() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + ENABLE + "}", ENABLE, 1);
		super.addDictData("{DICT." + TYPE + "." + DISABLE + "}", DISABLE, 2);
	}

	public static boolean isEnable(String v) {
		return ENABLE.equals(v);
	}

	public static boolean isDisable(String v) {
		return !isEnable(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
