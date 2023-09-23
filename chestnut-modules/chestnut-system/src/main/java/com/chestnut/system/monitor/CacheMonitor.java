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
