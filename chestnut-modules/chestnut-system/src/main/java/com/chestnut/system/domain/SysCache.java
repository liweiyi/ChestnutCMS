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
package com.chestnut.system.domain;

import com.chestnut.common.annotation.XComment;
import lombok.Getter;
import lombok.Setter;

/**
 * 缓存信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{ENT.SYS.CACHE}")
@Getter
@Setter
public class SysCache {

	@XComment("{ENT.SYS.CACHE.MONITORED_ID}")
	private String monitoredId;

	/**
	 * 缓存名称
	 */
	@XComment("{ENT.SYS.CACHE.NAME}")
	private String cacheName = "";

	/**
	 * 缓存键名
	 */
	@XComment("{ENT.SYS.CACHE.KEY}")
	private String cacheKey = "";

	/**
	 * 缓存内容
	 */
	@XComment("{ENT.SYS.CACHE.VALUE}")
	private String cacheValue = "";
	
	/**
	 * 过期时间
	 */
	@XComment("{ENT.SYS.CACHE.EXPIRE_TIME}")
	private Long expireTime = -1L;

	public SysCache() {

	}

	public SysCache(String monitoredId, String cacheName, String cacheKey) {
		this.monitoredId = monitoredId;
		this.cacheName = cacheName;
		this.cacheKey = cacheKey;
	}

	public SysCache(String monitoredId, String cacheName, String cacheKey, String cacheValue) {
		this.monitoredId = monitoredId;
		this.cacheName = cacheName;
		this.cacheKey = cacheKey;
		this.cacheValue = cacheValue;
	}
}
