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
