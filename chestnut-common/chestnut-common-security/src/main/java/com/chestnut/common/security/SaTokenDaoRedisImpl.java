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
package com.chestnut.common.security;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.chestnut.common.redis.RedisCache;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.util.SaFoxUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaTokenDaoRedisImpl implements SaTokenDao {
	
	private final RedisCache redisCache;

	@Override
	public String get(String key) {
		return this.redisCache.getCacheObject(key);
	}

	@Override
	public void set(String key, String value, long timeout) {
		if (timeout > 0) {
			this.redisCache.setCacheObject(key, value, timeout, TimeUnit.SECONDS);
		} else if (timeout == SaTokenDao.NEVER_EXPIRE) {
			this.redisCache.setCacheObject(key, value);
		}
	}

	/**
	 * 过期时间不变
	 */
	@Override
	public void update(String key, String value) {
		long expire = getTimeout(key);
		if(expire == NOT_VALUE_EXPIRE) {
			return;
		}
		this.set(key, value, expire);
	}

	@Override
	public void delete(String key) {
		this.redisCache.deleteObject(key);
	}

	@Override
	public long getTimeout(String key) {
		return this.redisCache.getExpire(key, TimeUnit.SECONDS);
	}

	@Override
	public void updateTimeout(String key, long timeout) {
		if (timeout > 0) {
			this.redisCache.expire(key, timeout);
		} else if (timeout == NEVER_EXPIRE) {
			long expire = this.getTimeout(key);
			if (expire != NEVER_EXPIRE) {
				this.redisCache.expire(key, timeout, TimeUnit.SECONDS);
			}
		}
	}

	@Override
	public Object getObject(String key) {
		return this.redisCache.getCacheObject(key);
	}

	@Override
	public void setObject(String key, Object object, long timeout) {
		if (timeout > 0) {
			this.redisCache.setCacheObject(key, object, timeout, TimeUnit.SECONDS);
		} else if (timeout == SaTokenDao.NEVER_EXPIRE) {
			this.redisCache.setCacheObject(key, object);
		}
	}

	/**
	 * 过期时间不变
	 */
	@Override
	public void updateObject(String key, Object object) {
		long expire = this.getObjectTimeout(key);
		if(expire == NOT_VALUE_EXPIRE) {
			return;
		}
		this.setObject(key, object, expire);
	}

	@Override
	public void deleteObject(String key) {
		this.redisCache.deleteObject(key);
		
	}

	@Override
	public long getObjectTimeout(String key) {
		return this.redisCache.getExpire(key, TimeUnit.SECONDS);
	}

	@Override
	public void updateObjectTimeout(String key, long timeout) {
		if (timeout > 0) {
			this.redisCache.expire(key, timeout);
		} else if (timeout == NEVER_EXPIRE) {
			long expire = this.getObjectTimeout(key);
			if (expire != NEVER_EXPIRE) {
				this.redisCache.expire(key, timeout, TimeUnit.SECONDS);
			}
		}
	}

	@Override
	public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
		List<String> keys = this.redisCache.scanKeys(prefix + "*" + keyword + "*", 1000).stream().toList();
		return SaFoxUtil.searchList(keys, start, size, sortType);
	}

}
