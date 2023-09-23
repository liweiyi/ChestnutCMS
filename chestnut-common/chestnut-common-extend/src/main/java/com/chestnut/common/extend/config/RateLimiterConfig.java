package com.chestnut.common.extend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import com.chestnut.common.extend.aspectj.RateLimiterAspect;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class RateLimiterConfig {
	
	private final RedisTemplate<String, Object> redisTempate;
	
	@Bean
	@ConditionalOnProperty(name = "chestnut.rate-limiter.enable", havingValue = "true")
	public RateLimiterAspect rateLimitAspect() {
		return new RateLimiterAspect(redisTempate, limitScript());
	}

	/**
	 * Lua限流脚本
	 */
	public DefaultRedisScript<Boolean> limitScript() {
		DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
		redisScript.setScriptText("""
				local key = KEYS[1] --限流KEY
				local limit = tonumber(ARGV[1]) --限流大小
				local expireTime = tonumber(ARGV[2]) --过期时间 单位/s

				local current = tonumber(redis.call('get', key) or "0")
				if current + 1 > limit then
				    return false --当前值超过限流大小阈值
				end
				current = tonumber(redis.call('incr', key)) --请求数+1
				if current == 1 then
				    redis.call('expire', key, expireTime) --设置过期时间
				end
				return true;
				""");
		redisScript.setResultType(Boolean.class);
		return redisScript;
	}
}
