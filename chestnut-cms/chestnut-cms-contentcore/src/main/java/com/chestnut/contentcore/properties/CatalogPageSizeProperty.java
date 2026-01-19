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
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 内容列表页的每页默认条数
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IProperty.BEAN_NAME_PREFIX + CatalogPageSizeProperty.ID)
public class CatalogPageSizeProperty implements IProperty {

	public final static String ID = "CatalogPageSize";

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
		return "内容列表页的每页默认条数";
	}
	
	@Override
	public boolean validate(String value) {
		return StringUtils.isEmpty(value) || NumberUtils.isCreatable(value);
	}
	
	@Override
	public Integer defaultValue() {
		return -1;
	}
	
	@Override
	public Integer getPropValue(Map<String, String> configProps) {
		String string = MapUtils.getString(configProps, getId());
		if (NumberUtils.isDigits(string)) {
			return NumberUtils.toInt(string);
		}
		return defaultValue();
	}
	
	public static int getValue(Map<String, String> catalogProps, Map<String, String> siteProps) {
		int pageSize = ConfigPropertyUtils.getIntValue(ID, catalogProps);
		if (pageSize < 1) {
			pageSize = ConfigPropertyUtils.getIntValue(ID, siteProps);
		}
		return pageSize;
	}
}