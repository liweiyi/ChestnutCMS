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
package com.chestnut.common.staticize.config.properties;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "chestnut.freemarker")
public class FreeMarkerProperties {
	
	/**
	 * 模板默认根目录
	 */
	private String templateLoaderPath;
	
	/**
	 * 模板文件编码，默认：UTF-8
	 */
	private String defaultEncoding = StandardCharsets.UTF_8.displayName();

	
	/**
	 * MRU缓存配置
	 */
	private MRUCache mruCache = new MRUCache();
	
	/**
	 * 配置
	 */
	private Map<String, String> settings = new HashMap<>();

	@Getter
	@Setter
	public static class MRUCache {
		
		private int strongSizeLimit = 50;
		
		private int softSizeLimit = 200;
	}
}
