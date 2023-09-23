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
