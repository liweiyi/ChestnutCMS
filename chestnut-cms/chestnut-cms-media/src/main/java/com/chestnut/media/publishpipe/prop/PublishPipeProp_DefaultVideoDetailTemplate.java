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
package com.chestnut.media.publishpipe.prop;

import com.chestnut.contentcore.core.IPublishPipeProp;
import com.chestnut.media.VideoContentType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component(IPublishPipeProp.BEAN_PREFIX + PublishPipeProp_DefaultVideoDetailTemplate.KEY)
public class PublishPipeProp_DefaultVideoDetailTemplate implements IPublishPipeProp {

	public static final String KEY = DefaultDetailTemplatePropPrefix + VideoContentType.ID;
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "视频集详情页默认模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Site);
	}
}
