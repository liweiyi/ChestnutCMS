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
package com.chestnut.cms.member.publishpipe;

import com.chestnut.contentcore.core.IPublishPipeProp;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component(IPublishPipeProp.BEAN_PREFIX + PublishPipeProp_AccountCentreTemplate.KEY)
public class PublishPipeProp_AccountCentreTemplate implements IPublishPipeProp {

	public static final String KEY = "accountCentreTemplate";

	static final String DEFAULT_VALUE = "account/account_centre.template.html";
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "个人中心页模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Site);
	}

	@Override
	public String getDefaultValue() {
		return DEFAULT_VALUE;
	}

	public static String getValue(String publishPipeCode, Map<String, Map<String, Object>> publishPipeProps) {
		if (Objects.nonNull(publishPipeProps)) {
			return MapUtils.getString(publishPipeProps.get(publishPipeCode), KEY, DEFAULT_VALUE);
		}
		return DEFAULT_VALUE;
	}
}
