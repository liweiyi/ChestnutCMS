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
package com.chestnut.system.permission;

import cn.dev33.satoken.annotation.SaMode;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(IPermissionType.BEAN_PREFIX + MenuPermissionType.ID)
public class MenuPermissionType implements IPermissionType {

	public static final String ID = "Menu";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "菜单权限";
	}

	@Override
	public Set<String> deserialize(String json) {
		if (StringUtils.isEmpty(json)) {
			return new HashSet<>();
		}
		return JacksonUtils.fromSet(json, String.class);
	}

	@Override
	public String serialize(Set<String> permissionKeys) {
		return JacksonUtils.to(permissionKeys);
	}

	@Override
	public boolean hasPermission(List<String> permissionKeys, String json, SaMode mode) {
		Set<String> perms = deserialize(json);
		if (mode == SaMode.AND) {
			for (String key : permissionKeys) {
				if(!perms.contains(key)) {
					return false;
				}
			}
			return true;
		} else {
			for (String key : permissionKeys) {
				if(perms.contains(key)) {
					return true;
				}
			}
			return false;
		}
	}
}
