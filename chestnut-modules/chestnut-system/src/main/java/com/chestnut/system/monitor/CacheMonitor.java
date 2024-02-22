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
package com.chestnut.system.monitor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.chestnut.common.extend.ExtendConstants;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.redis.IMonitoredCache;
import com.chestnut.system.SysConstants;

@Configuration
public class CacheMonitor {
	
	@Bean
	public IMonitoredCache repeatSubmitCache() {
		return new IMonitoredCache() {
			
			@Override
			public String getCacheName() {
				return I18nUtils.get("{MONITORED.CACHE.REPEAT_SUBMIT}");
			}
			
			@Override
			public String getCacheKey() {
				return ExtendConstants.REPEAT_SUBMIT_KEY;
			}
		};
	}
	
	@Bean
	public IMonitoredCache rateLimiterCache() {
		return new IMonitoredCache() {
			
			@Override
			public String getCacheName() {
				return I18nUtils.get("{MONITORED.CACHE.RATE_LIMITER}");
			}
			
			@Override
			public String getCacheKey() {
				return ExtendConstants.RATE_LIMIT_KEY;
			}
		};
	}
	
	@Bean
	public IMonitoredCache captchCache() {
		return new IMonitoredCache() {
			
			@Override
			public String getCacheName() {
				return I18nUtils.get("{MONITORED.CACHE.CAPTCHA}");
			}
			
			@Override
			public String getCacheKey() {
				return SysConstants.CAPTCHA_CODE_KEY;
			}
		};
	}
	
	@Bean
	public IMonitoredCache sysConfigCache() {
		return new IMonitoredCache() {
			
			@Override
			public String getCacheName() {
				return I18nUtils.get("{MONITORED.CACHE.CONFIG}");
			}
			
			@Override
			public String getCacheKey() {
				return SysConstants.CACHE_SYS_CONFIG_KEY;
			}
		};
	}
	
	@Bean
	public IMonitoredCache sysDictDataCache() {
		return new IMonitoredCache() {
			
			@Override
			public String getCacheName() {
				return I18nUtils.get("{MONITORED.CACHE.DICT}");
			}
			
			@Override
			public String getCacheKey() {
				return SysConstants.CACHE_SYS_DICT_KEY;
			}
		};
	}
	
	@Bean
	public IMonitoredCache sysDeptCache() {
		return new IMonitoredCache() {
			
			@Override
			public String getCacheName() {
				return I18nUtils.get("{MONITORED.CACHE.DEPT}");
			}
			
			@Override
			public String getCacheKey() {
				return SysConstants.CACHE_SYS_DEPT_KEY;
			}
		};
	}
	
	@Bean
	public IMonitoredCache sysRoleCache() {
		return new IMonitoredCache() {
			
			@Override
			public String getCacheName() {
				return I18nUtils.get("{MONITORED.CACHE.ROLE}");
			}
			
			@Override
			public String getCacheKey() {
				return SysConstants.CACHE_SYS_ROLE_KEY;
			}
		};
	}
	
	@Bean
	public IMonitoredCache sysPostCache() {
		return new IMonitoredCache() {
			
			@Override
			public String getCacheName() {
				return I18nUtils.get("{MONITORED.CACHE.POST}");
			}
			
			@Override
			public String getCacheKey() {
				return SysConstants.CACHE_SYS_POST_KEY;
			}
		};
	}
}
