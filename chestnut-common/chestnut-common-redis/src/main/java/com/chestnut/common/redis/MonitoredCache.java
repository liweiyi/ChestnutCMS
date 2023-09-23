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
