/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 内容副标题Label自定义配置
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IProperty.BEAN_NAME_PREFIX + SubTitleLabelProperty.ID)
public class SubTitleLabelProperty implements IProperty {

	public final static String ID = "SubTitleLabel";

	static UseType[] UseTypes = new UseType[] { UseType.Site, UseType.Catalog };
	
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
		return "内容副标题Label自定义配置";
	}
	
	public static String getValue(Map<String, String> catalogProps, Map<String, String> siteProps) {
		String label = ConfigPropertyUtils.getStringValue(ID, catalogProps);
		if (StringUtils.isBlank(label)) {
			label = ConfigPropertyUtils.getStringValue(ID, siteProps);
		}
		return label;
	}
}