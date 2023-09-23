package com.chestnut.member.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 用户状态
 */
@Component(FixedDictType.BEAN_PREFIX + MemberStatus.TYPE)
public class MemberStatus extends FixedDictType {

	public static final String TYPE = "MemberStatus";

	public static final String ENABLE = "0"; // 正常
	
	public static final String DISABLE = "1"; // 禁用
	
	public static final String LOCK = "2"; // 锁定
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);
	
	public MemberStatus() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + ENABLE + "}", ENABLE, 1);
		super.addDictData("{DICT." + TYPE + "." + DISABLE + "}", DISABLE, 2);
		super.addDictData("{DICT." + TYPE + "." + LOCK + "}", LOCK, 3);
	}
	
	public static boolean isEnable(String v) {
		return ENABLE.equals(v);
	}
	
	public static boolean isDisbale(String v) {
		return DISABLE.equals(v);
	}
	
	public static boolean isLocked(String v) {
		return LOCK.equals(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
