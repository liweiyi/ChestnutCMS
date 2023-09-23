package com.chestnut.system.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 用户登录/登出/注册日志
 */
@Component(FixedDictType.BEAN_PREFIX + LoginLogType.TYPE)
public class LoginLogType extends FixedDictType {

	public static final String TYPE = "LoginLogType";

	public static final String LOGIN = "0"; // 登录
	
	public static final String LOGOUT = "1"; // 登出
	
	public static final String REGIST = "2"; // 注册
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);
	
	public LoginLogType() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + LOGIN + "}", LOGIN, 1);
		super.addDictData("{DICT." + TYPE + "." + LOGOUT + "}", LOGOUT, 2);
		super.addDictData("{DICT." + TYPE + "." + REGIST + "}", REGIST, 3);
	}
	
	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
