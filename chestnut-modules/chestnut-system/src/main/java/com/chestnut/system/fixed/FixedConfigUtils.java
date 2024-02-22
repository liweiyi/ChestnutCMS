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
