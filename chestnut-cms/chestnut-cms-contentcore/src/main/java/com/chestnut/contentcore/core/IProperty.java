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
package com.chestnut.contentcore.core;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.chestnut.common.utils.StringUtils;

/**
 * 扩展属性
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IProperty {
	
	/**
	 * Bean名称前缀
	 */
	String BEAN_NAME_PREFIX = "CMSProperty_";
	
	/**
	 * 属性适用范围定义：站点、栏目
	 *
	 */
	enum UseType {
		Site, Catalog
	}

	/**
	 * 属性适用范围
	 * @return
	 */
	UseType[] getUseTypes();

	/**
	 * 属性值ID
	 */
	String getId();
	
	/**
	 * 属性值名称
	 */
	String getName();
	
	/**
	 * 校验使用类型
	 */
	default boolean checkUseType(UseType useType) {
		return ArrayUtils.contains(this.getUseTypes(), useType);
	}
	
	/**
	 * 属性值校验
	 */
	default boolean validate(String value) {
		return true;
	}
	
	/**
	 * 是否敏感数据
	 */
	default boolean isSensitive() {
		return false;
	}
	
	/**
	 * 属性默认值
	 */
	default Object defaultValue() {
		return StringUtils.EMPTY;
	}

	default Object getPropValue(Map<String, String> configProps) {
		return MapUtils.getString(configProps, getId(), defaultValue().toString());
	}
}
