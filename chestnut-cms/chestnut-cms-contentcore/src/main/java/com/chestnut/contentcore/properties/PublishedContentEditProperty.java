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
package com.chestnut.contentcore.properties;

import java.util.Map;

import com.chestnut.common.utils.StringUtils;
import org.springframework.stereotype.Component;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import com.chestnut.system.fixed.dict.YesOrNo;

@Component(IProperty.BEAN_NAME_PREFIX + PublishedContentEditProperty.ID)
public class PublishedContentEditProperty implements IProperty {

	public final static String ID = "PublishedContentEdit";
	
	static UseType[] UseTypes = new UseType[] { UseType.Site };
	
	@Override
	public UseType[] getUseTypes() {
		return UseTypes;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "是否允许编辑已发布内容";
	}
	
	@Override
	public String defaultValue() {
		return YesOrNo.YES;
	}
	
	public static boolean getValue(Map<String, String> props) {
		String value = ConfigPropertyUtils.getStringValue(ID, props);
		if (StringUtils.isEmpty(value)) {
			return true;
		}
		return YesOrNo.isYes(value);
	}
}
