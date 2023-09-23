package com.chestnut.system.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 成功/失败
 */
@Component(FixedDictType.BEAN_PREFIX + SuccessOrFail.TYPE)
public class SuccessOrFail extends FixedDictType {

	public static final String TYPE = "SuccessOrFail";
	
	public static final String SUCCESS = "0";

	public static final String FAIL = "1";

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public SuccessOrFail() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + SUCCESS + "}", SUCCESS, 1);
		super.addDictData("{DICT." + TYPE + "." + FAIL + "}", FAIL, 2);
	}
	
	public static boolean isSuccess(String v) {
		return SUCCESS.equals(v);
	}
	
	public static boolean isFail(String v) {
		return !isSuccess(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
