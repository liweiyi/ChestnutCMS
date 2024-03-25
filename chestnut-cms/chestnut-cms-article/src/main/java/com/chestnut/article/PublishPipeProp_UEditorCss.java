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
package com.chestnut.article;

import com.alibaba.excel.util.StringUtils;
import com.chestnut.contentcore.core.IPublishPipeProp;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 发布通道属性：文章编辑器自定义样式文件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IPublishPipeProp.BEAN_PREFIX + PublishPipeProp_UEditorCss.KEY)
public class PublishPipeProp_UEditorCss implements IPublishPipeProp {

	public static final String KEY = "ueditorCss";
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "文章编辑器自定义样式";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Site, PublishPipePropUseType.Catalog);
	}

	public static String getValue(
		String publishPipeCode,
	 	Map<String, Map<String, Object>> firstPublishPipeProps,
		Map<String, Map<String, Object>> secondPublishPipeProps
	) {
		String v = StringUtils.EMPTY;
		if (Objects.nonNull(firstPublishPipeProps)) {
			v = MapUtils.getString(firstPublishPipeProps.get(publishPipeCode), KEY, StringUtils.EMPTY);
			if (StringUtils.isEmpty(v) && Objects.nonNull(secondPublishPipeProps)) {
				v = MapUtils.getString(secondPublishPipeProps.get(publishPipeCode), KEY, StringUtils.EMPTY);
			}
		}
		return v;
	}
}
