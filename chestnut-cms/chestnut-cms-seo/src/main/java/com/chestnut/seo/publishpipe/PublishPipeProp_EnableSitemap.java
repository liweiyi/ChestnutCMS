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
package com.chestnut.seo.publishpipe;

import com.chestnut.contentcore.core.IPublishPipeProp;
import com.chestnut.system.fixed.dict.YesOrNo;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 发布通道属性：是否生成站点地图
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IPublishPipeProp.BEAN_PREFIX + PublishPipeProp_EnableSitemap.KEY)
public class PublishPipeProp_EnableSitemap implements IPublishPipeProp {
	
	public static final String KEY = "enableSitemap";

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "是否生成站点地图";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Site);
	}

	public static boolean getValue(String publishPipeCode, Map<String, Map<String, Object>> publishPipeProps) {
		if (Objects.nonNull(publishPipeProps)) {
			return YesOrNo.isYes(MapUtils.getString(publishPipeProps.get(publishPipeCode), KEY));
		}
		return false;
	}
}
