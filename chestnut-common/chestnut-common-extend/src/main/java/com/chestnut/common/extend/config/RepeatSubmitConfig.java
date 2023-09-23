package com.chestnut.common.extend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.chestnut.common.extend.aspectj.RepeatSubmitAspect;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.SecurityService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class RepeatSubmitConfig {
	
	private final RedisCache redisCache;
	
	private final SecurityService securityService;
	
	@Bean
	@ConditionalOnProperty(name = "chestnut.repeat-submit.enable", havingValue = "true")
	public RepeatSubmitAspect repeatSubmitAspect() {
		return new RepeatSubmitAspect(this.redisCache, this.securityService.getUserTypes());
	}
}
