package com.chestnut.common.redis;

public interface IMonitoredCache {

	/**
	 * 緩存Key（前缀）
	 */
	public String getCacheKey();

	/**
	 * 缓存名称
	 */
	public String getCacheName();
}
