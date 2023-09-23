package com.chestnut.system.fixed;

import java.util.List;
import java.util.Map;

import com.chestnut.common.utils.SpringUtils;

/**
 * 固定参数项工具类
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class FixedConfigUtils {

	private static final Map<String, FixedConfig> fixedConfigs = SpringUtils.getBeanMap(FixedConfig.class);

	public static FixedConfig getConfig(String configKey) {
		return fixedConfigs.get(FixedConfig.BEAN_PREFIX + configKey);
	}
	
	public static boolean isFixedConfig(String configKey) {
		return fixedConfigs.containsKey(FixedConfig.BEAN_PREFIX + configKey);
	}

	public static List<FixedConfig> allConfigs() {
		return fixedConfigs.values().stream().toList();
	}
}
