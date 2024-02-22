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

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;

/**
 * 校验重复标题策略
 */
@Component(IProperty.BEAN_NAME_PREFIX + RepeatTitleCheckProperty.ID)
public class RepeatTitleCheckProperty implements IProperty {

	public final static String ID = "RepeatTitleCheck";

	static UseType[] UseTypes = new UseType[] { UseType.Site };

	public static final String CheckType_None = "0"; // 不校验
	public static final String CheckType_Site = "1"; // 全站校验
	public static final String CheckType_Catalog = "2"; // 栏目校验

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
		return "校验重复标题策略";
	}

	@Override
	public boolean validate(String value) {
		return StringUtils.isEmpty(value) || StringUtils.equalsAny(value.toString(), StringUtils.EMPTY, CheckType_None,
				CheckType_Site, CheckType_Catalog);
	}

	@Override
	public String defaultValue() {
		return CheckType_None;
	}

	public static String getValue(Map<String, String> firstConfigProps) {
		String value = ConfigPropertyUtils.getStringValue(ID, firstConfigProps);
		return StringUtils.isEmpty(value) ? CheckType_None : value;
	}
}
