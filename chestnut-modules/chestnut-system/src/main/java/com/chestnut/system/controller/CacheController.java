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
package com.chestnut.system.controller;

import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysCache;
import com.chestnut.system.domain.dto.ClearCacheDTO;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 缓存监控
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/cache")
public class CacheController {

	private final RedisTemplate<String, Object> redisTemplate;
	
	private final Map<String, IMonitoredCache<?>> monitoredCaches;

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheList)
	@GetMapping
	public R<?> getInfo() throws Exception {
		Properties info = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.serverCommands().info());
		Properties commandStats = (Properties) redisTemplate
				.execute((RedisCallback<Object>) connection -> connection.serverCommands().info("commandstats"));
		Object dbSize = redisTemplate.execute((RedisCallback<Object>) connection -> connection.serverCommands().dbSize());

		Map<String, Object> result = new HashMap<>(3);
		result.put("info", info);
		result.put("dbSize", dbSize);

		List<Map<String, String>> pieList = new ArrayList<>();
		Objects.requireNonNull(commandStats).stringPropertyNames().forEach(key -> {
			Map<String, String> data = new HashMap<>(2);
			String property = commandStats.getProperty(key);
			data.put("name", StringUtils.removeStart(key, "cmdstat_"));
			data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
			pieList.add(data);
		});
		result.put("commandStats", pieList);
		return R.ok(result);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheList)
	@GetMapping("/getNames")
	public R<?> cache() {
		List<SysCache> list = this.monitoredCaches.values().stream().map(mc -> {
			return new SysCache(mc.getId(), I18nUtils.get(mc.getCacheName()), mc.getCacheKey());
		}).collect(Collectors.toList());
		return R.ok(list);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheList)
	@GetMapping("/getKeys/{monitoredId}")
	public R<?> getCacheKeys(@PathVariable String monitoredId) {
		IMonitoredCache<?> iMonitoredCache = this.monitoredCaches.get(IMonitoredCache.BEAN_PREFIX + monitoredId);
		Assert.notNull(iMonitoredCache, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(monitoredId));

		if (iMonitoredCache.getCacheKey().endsWith(":")) {
			Set<String> cacheKeys = redisTemplate.keys(iMonitoredCache.getCacheKey() + "*");
			return R.ok(cacheKeys);
		} else {
			return R.ok(Set.of(iMonitoredCache.getCacheKey()));
		}
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheList)
	@GetMapping("/getValue")
	public R<?> getCacheValue(@RequestParam @NotBlank String monitoredId, @RequestParam @NotBlank String cacheKey) {
		IMonitoredCache<?> iMonitoredCache = this.monitoredCaches.get(IMonitoredCache.BEAN_PREFIX + monitoredId);
		Assert.notNull(iMonitoredCache, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(monitoredId));

		boolean validate = cacheKey.startsWith(iMonitoredCache.getCacheKey());
		Assert.isTrue(validate, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("cacheKey"));

		Object cacheValue = iMonitoredCache.getCache(cacheKey);
		SysCache sysCache = new SysCache();
		sysCache.setCacheKey(cacheKey);
		sysCache.setCacheValue(Objects.isNull(cacheValue) ? "" : JacksonUtils.to(cacheValue));
		sysCache.setExpireTime(redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS));
		return R.ok(sysCache);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheClear)
	@DeleteMapping("/clearCacheName/{monitoredId}")
	public R<?> clearCacheName(@PathVariable String monitoredId) {
		IMonitoredCache<?> iMonitoredCache = this.monitoredCaches.get(IMonitoredCache.BEAN_PREFIX + monitoredId);
		Assert.notNull(iMonitoredCache, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(monitoredId));
		Collection<String> cacheKeys = redisTemplate.keys(iMonitoredCache.getCacheKey() + "*");
		if (Objects.nonNull(cacheKeys)) {
			redisTemplate.delete(cacheKeys);
		}
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheClear)
	@DeleteMapping("/clearCacheKey")
	public R<?> clearCacheKey(@RequestBody ClearCacheDTO dto) {
		redisTemplate.delete(dto.getCacheKey());
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheClear)
	@DeleteMapping("/clearCacheAll")
	public R<?> clearCacheAll() {
		Collection<String> cacheKeys = redisTemplate.keys("*");
		if (Objects.nonNull(cacheKeys)) {
			redisTemplate.delete(cacheKeys);
		}
		return R.ok();
	}
}
