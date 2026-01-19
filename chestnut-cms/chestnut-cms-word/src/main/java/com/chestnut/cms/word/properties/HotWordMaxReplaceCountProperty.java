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
package com.chestnut.cms.word.properties;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 热词最大替换数量
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IProperty.BEAN_NAME_PREFIX + HotWordMaxReplaceCountProperty.ID)
public class HotWordMaxReplaceCountProperty implements IProperty {

	public final static String ID = "HotWordMaxReplaceCount";
	
	static UseType[] UseTypes = new UseType[] { UseType.Site, UseType.Catalog };
	
	private static final int DEFAULT_VALUE = 0;

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
		return "热词最大替换数量";
	}
	
	@Override
	public Integer defaultValue() {
		return DEFAULT_VALUE;
	}
	
	@Override
	public Integer getPropValue(Map<String, String> configProps) {
        return MapUtils.getInteger(configProps, ID, DEFAULT_VALUE);
	}
	
	public static int getHotWordMaxReplaceCount(Map<String, String> firstProps, Map<String, String> secondProps) {
        int v = ConfigPropertyUtils.getIntValue(ID, firstProps);
        if (v > 0) {
            return v;
        }
        return ConfigPropertyUtils.getIntValue(ID, secondProps);
	}
}
