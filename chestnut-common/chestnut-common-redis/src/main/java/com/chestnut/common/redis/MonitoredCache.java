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
package com.chestnut.common.redis;

import lombok.Getter;
import lombok.Setter;

/**
 * 被监控的Redis缓存数据
 */
@Getter
@Setter
public class MonitoredCache {

	
	/** 
	 * 缓存名称
	 */
	private String cacheName = "";

	/** 
	 * 缓存键名
	 */
	private String cacheKey = "";

	public MonitoredCache(String cacheName, String cacheKey) {
		this.cacheName = cacheName;
		this.cacheKey = cacheKey;
	}
}
