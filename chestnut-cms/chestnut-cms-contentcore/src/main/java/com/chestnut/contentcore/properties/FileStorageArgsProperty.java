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

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * 资源存储策略参数
 */
@Component(IProperty.BEAN_NAME_PREFIX + FileStorageArgsProperty.ID)
public class FileStorageArgsProperty implements IProperty {

	public final static String ID = "FileStorageArgs";
	
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
		return "资源存储策略参数";
	}
	
	@Override
	public FileStorageArgs defaultValue() {
		return new FileStorageArgs();
	}
	
	@Override
	public FileStorageArgs getPropValue(Map<String, String> configProps) {
		String v = MapUtils.getString(configProps, getId());
		if (StringUtils.isNotEmpty(v)) {
			return JacksonUtils.from(v, FileStorageArgs.class);
		}
		return defaultValue();
	}	
	
	public static FileStorageArgs getValue(Map<String, String> props) {
		String v = MapUtils.getString(props, ID);
		if (StringUtils.isNotEmpty(v)) {
			return JacksonUtils.from(v, FileStorageArgs.class);
		}
		return new FileStorageArgs();
	}
	
	@Getter
	@Setter
	public static class FileStorageArgs {
		
		private String accessKey;
		
		private String accessSecret;
		
		private String endpoint;
		
		private String bucket;
		
		private String pipeline;
		
		private String domain;
	}
}
