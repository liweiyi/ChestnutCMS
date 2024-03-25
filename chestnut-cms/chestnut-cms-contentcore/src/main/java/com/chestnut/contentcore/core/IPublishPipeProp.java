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

import java.util.List;

import com.chestnut.common.utils.StringUtils;

/**
 * 发布通道属性
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IPublishPipeProp {

	String BEAN_PREFIX = "PublishPipeProp_";
	
	String DetailTemplatePropPrefix = "detailTemplate_";
	
	String DefaultDetailTemplatePropPrefix = "defaultDetailTemplate_";

	/**
	 * 属性唯一标识键名
	 */
	String getKey();
	
	/**
	 * 属性名称
	 */
	String getName();
	
	/**
	 * 属性应用类型
	 */
	List<PublishPipePropUseType> getUseTypes();
	
	/**
	 * 默认值
	 */
	default String getDefaultValue() {
		return StringUtils.EMPTY;
	}
	
	/**
	 * 应用类型
	 */
	enum PublishPipePropUseType {
		Site, Catalog, Content
	}
}
