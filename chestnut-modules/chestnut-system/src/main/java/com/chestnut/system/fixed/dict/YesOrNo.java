package com.chestnut.system.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 是/否
 */
@Component(FixedDictType.BEAN_PREFIX + YesOrNo.TYPE)
public class YesOrNo extends FixedDictType {

	public static final String TYPE = "YesOrNo";
	
	public static final String YES = "Y";
	
	public static final String NO = "N";

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);
	
	public YesOrNo() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + YES + "}", YES, 1);
		super.addDictData("{DICT." + TYPE + "." + NO + "}", NO, 2);
	}
	
	public static boolean isYes(String v) {
		return YES.equals(v);
	}
	
	public static boolean isNo(String v) {
		return !isYes(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
