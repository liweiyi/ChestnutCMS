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
package com.chestnut.contentcore.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;

import com.chestnut.common.utils.NumberUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.core.IProperty.UseType;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;

public class ConfigPropertyUtils {
	
	/**
	 * 敏感数据替换占位符
	 */
	public static final String SensitivePlaceholder = "******";
	
	private static final Map<String, IProperty> ConfigProperties = SpringUtils.getBeanMap(IProperty.class);

	/**
	 * 获取配置属性声明类
	 * 
	 * @param propertyKey
	 * @return
	 */
	public static IProperty getConfigProperty(String propertyKey) {
		return ConfigProperties.get(IProperty.BEAN_NAME_PREFIX + propertyKey);
	}

	/**
	 * 获取指定使用场景的配置属性声明类列表
	 * 
	 * @param useType
	 * @return
	 */
	public static List<IProperty> getConfigPropertiesByUseType(UseType useType) {
		return ConfigProperties.values().stream().filter(p -> p.checkUseType(useType)).collect(Collectors.toList());
	}
	
	public static Map<String, Object> parseConfigProps(Map<String, String> configProps, UseType useType) {
		Map<String, Object> map = new HashMap<>();
		List<IProperty> props = ConfigPropertyUtils.getConfigPropertiesByUseType(useType);
		for (IProperty prop : props) {
			map.put(prop.getId(), prop.getPropValue(configProps));
			if (prop.isSensitive() && StringUtils.isNotEmpty(configProps.get(prop.getId()))) {
				map.put(prop.getId(), SensitivePlaceholder);
			}
		}
		return map;
	}

	/**
	 * 过滤掉map中不符合条件的key-value.
	 * 
	 * @param configProps
	 * @param useType
	 */
	public static void filterConfigProps(Map<String, String> configProps, Map<String, String> oldConfigProps, UseType useType) {
		for (Iterator<Entry<String, String>> iterator = configProps.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, String> e = iterator.next();
			IProperty property = getConfigProperty(e.getKey());
			if (property == null || !property.checkUseType(useType)) {
				iterator.remove();
				continue;
			}
			if (!property.validate(e.getValue())) {
				throw ContentCoreErrorCode.INVALID_PROPERTY.exception(property.getId(), e.getValue());
			}
			if (property.isSensitive() && SensitivePlaceholder.equals(e.getValue())) {
				e.setValue(oldConfigProps.get(e.getKey())); // 敏感数据未改变
			}
		}
	}

	/**
	 * 获取字符串类型配置属性值
	 * 
	 * @param propertyKey
	 * @param props
	 * @return
	 */
	public static String getStringValue(String propertyKey, Map<String, String> props) {
		return getStringValue(propertyKey, props, null);
	}

	/**
	 * 获取字符串类型配置属性值，优先firstProps，firstProps没有则查找secondProps
	 * 
	 * @param propertyKey
	 * @param firstProps
	 * @param secondProps
	 * @return
	 */
	public static String getStringValue(String propertyKey, Map<String, String> firstProps,
			Map<String, String> secondProps) {
		IProperty prop = getConfigProperty(propertyKey);
		String v = null;
		if (prop != null) {
			v = MapUtils.getString(firstProps, prop.getId());
			if (StringUtils.isEmpty(v)) {
				v = MapUtils.getString(secondProps, prop.getId());
			}
		}
		return v;
	}

	/**
	 * 获取整数类型配置属性值
	 * 
	 * @param propertyKey
	 * @param props
	 * @return
	 */
	public static int getIntValue(String propertyKey, Map<String, String> props) {
		return getIntValue(propertyKey, props, null);
	}

	/**
	 * 获取整数类型配置属性值，优先firstProps，firstProps没有则查找secondProps
	 * 
	 * @param propertyKey
	 * @param firstProps
	 * @param secondProps
	 * @return
	 */
	public static int getIntValue(String propertyKey, Map<String, String> firstProps, Map<String, String> secondProps) {
		IProperty prop = getConfigProperty(propertyKey);
		int intV = 0;
		if (prop != null) {
			String v = MapUtils.getString(firstProps, prop.getId());
			if (NumberUtils.isCreatable(v)) {
				intV = NumberUtils.toInt(v);
			} else {
				v = MapUtils.getString(secondProps, prop.getId());
				if (NumberUtils.isCreatable(v)) {
					intV = NumberUtils.toInt(v);
				}
			}
		}
		return intV;
	}
}
