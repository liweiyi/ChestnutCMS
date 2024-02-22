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
package com.chestnut.system.domain;

import com.chestnut.common.utils.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 缓存信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class SysCache {
	
	/** 缓存名称 */
	private String cacheName = "";

	/** 缓存键名 */
	private String cacheKey = "";

	/** 缓存内容 */
	private String cacheValue = "";
	
	/**
	 * 过期时间
	 */
	private Long expireTime = -1L;

	/** 备注 */
	private String remark = "";

	public SysCache() {

	}

	public SysCache(String cacheName, String remark) {
		this.cacheName = cacheName;
		this.remark = remark;
	}

	public SysCache(String cacheName, String cacheKey, String cacheValue) {
		this.cacheName = StringUtils.replace(cacheName, ":", "");
		this.cacheKey = StringUtils.replace(cacheKey, cacheName, "");
		this.cacheValue = cacheValue;
	}
}
