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
package com.chestnut.contentcore.core.impl;

import com.chestnut.contentcore.core.IPublishPipeProp;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 发布通道属性：内容自定义扩展模板
 *
 * <p>此模板会在内容发布时同时发布，可内容独立配置，也可通过栏目配置。</p>
 *
 * 应用场景：
 * 文章内容需要插入轮播图时，可将图集内容设置扩展模板发布成指定格式内容
 * 当不支持ssi时可发布成<script>引用json发布通道的图集数据来实现动态更新
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IPublishPipeProp.BEAN_PREFIX + PublishPipeProp_ContentExTemplate.KEY)
public class PublishPipeProp_ContentExTemplate implements IPublishPipeProp {

	public static final String KEY = "contentExTemplate";
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "内容自定义扩展模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Catalog, PublishPipePropUseType.Content);
	}

	public static String getValue(String publishPipeCode, Map<String, Map<String, Object>> publishPipeProps) {
		if (Objects.nonNull(publishPipeProps)) {
			return MapUtils.getString(publishPipeProps.get(publishPipeCode), KEY);
		}
		return null;
	}
}
