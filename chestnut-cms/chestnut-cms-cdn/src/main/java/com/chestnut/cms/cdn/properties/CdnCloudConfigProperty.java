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
package com.chestnut.cms.cdn.properties;

import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CDN云服务配置ID
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IProperty.BEAN_NAME_PREFIX + CdnCloudConfigProperty.ID)
public class CdnCloudConfigProperty implements IProperty {

	public final static String ID = "CdnCloudConfig";
	
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
		return "CDN云服务配置ID";
	}
	
	@Override
	public Long getPropValue(Map<String, String> configProps) {
		return MapUtils.getLong(configProps, getId());
	}
	
	public static Long getValue(CmsSite site) {
		return ConfigPropertyUtils.getLongValue(ID, site.getConfigProps());
	}
}
