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
package com.chestnut.contentcore.core.impl;

import com.chestnut.contentcore.core.IPublishPipeProp;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 发布通道属性：内容自定义模板
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IPublishPipeProp.BEAN_PREFIX + PublishPipeProp_ContentTemplate.KEY)
public class PublishPipeProp_ContentTemplate implements IPublishPipeProp {

	public static final String KEY = "template";
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "内容自定义模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Content);
	}

	public static String getValue(String publishPipeCode, Map<String, Map<String, Object>> publishPipeProps) {
		if (Objects.nonNull(publishPipeProps)) {
			return MapUtils.getString(publishPipeProps.get(publishPipeCode), KEY);
		}
		return null;
	}
}
