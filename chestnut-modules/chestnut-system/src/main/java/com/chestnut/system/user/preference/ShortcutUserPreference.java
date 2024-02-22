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
package com.chestnut.system.user.preference;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 一键导航配置
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class ShortcutUserPreference implements IUserPreference {
	
	public static final String ID = "Shortcut";
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "快捷导航";
	}

	@Override
	public boolean validate(Object config) {
		return Objects.nonNull(config) && config instanceof List;
	}
	
	@Override
	public Object getDefaultValue() {
		return List.of();
	}

	public static List<Long> getValue(Map<String, Object> preferences) {
		if (preferences != null) {
			Object value = preferences.get(ID);
			if (value instanceof List<?> menuIds) {
				return menuIds.stream().map(o -> Long.valueOf(o.toString())).toList();
			}
		}
		return List.of();
	}
}
