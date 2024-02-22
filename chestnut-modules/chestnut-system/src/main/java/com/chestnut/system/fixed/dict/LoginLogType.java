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
