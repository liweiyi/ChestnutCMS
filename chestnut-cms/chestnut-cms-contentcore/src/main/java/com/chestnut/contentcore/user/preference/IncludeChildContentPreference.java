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
package com.chestnut.contentcore.user.preference;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.user.preference.IUserPreference;

import lombok.RequiredArgsConstructor;

/**
 * 内容列表是否显示子栏目内容
 */
@Component
@RequiredArgsConstructor
public class IncludeChildContentPreference implements IUserPreference {
	
	public static final String ID = "IncludeChildContent";
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "内容列表是否显示子栏目内容";
	}
	
	@Override
	public boolean validate(Object config) {
		return YesOrNo.YES.equals(config) || YesOrNo.NO.equals(config);
	}
	
	@Override
	public Object getDefaultValue() {
		return YesOrNo.NO;
	}

	public static boolean getValue(LoginUser loginUser) {
		SysUser user = (SysUser) loginUser.getUser();
		return YesOrNo.isYes(MapUtils.getString(user.getPreferences(), ID, YesOrNo.NO));
	}
}
