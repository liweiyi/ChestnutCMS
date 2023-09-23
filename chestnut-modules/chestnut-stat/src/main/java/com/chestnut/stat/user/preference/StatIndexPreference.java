package com.chestnut.stat.user.preference;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.user.preference.IUserPreference;

import lombok.RequiredArgsConstructor;

/**
 * 统计默认打开菜单配置
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class StatIndexPreference implements IUserPreference {
	
	public static final String ID = "StatIndex";
	
	static final String DEFAULT_VALUE = "BdSiteTrendOverview";
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "统计默认菜单";
	}

	@Override
	public boolean validate(Object config) {
		return Objects.nonNull(config);
	}
	
	@Override
	public String getDefaultValue() {
		return DEFAULT_VALUE;
	}

	public static String getValue(Map<String, Object> preferences) {
		String value = MapUtils.getString(preferences, ID);
		if (StringUtils.isNotEmpty(value)) {
			return value;
		}
		return DEFAULT_VALUE;
	}
}
