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
package com.chestnut.common.redis.config;

import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

/**
 * redis配置
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig implements CachingConfigurer {

	private final CacheProperties cacheProperties;

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);

		ObjectMapper objectMapper = JacksonUtils.newObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
				JsonTypeInfo.As.PROPERTY);

		Jackson2JsonRedisSerializer<Object> redisValueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
		redisTemplate.setDefaultSerializer(redisValueSerializer);

		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setHashKeySerializer(RedisSerializer.string());
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}
	
	/**
	 * 使用@Cacheable等缓存注解的时候默认缓存的值是二进制的，改成JSON格式的
	 */
	@Bean
	public RedisCacheConfiguration redisCacheConfiguration(RedisTemplate<String, Object> redisTemplate) {
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
				.serializeValuesWith(
						RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getValueSerializer()));
		if (cacheProperties.getRedis().getTimeToLive() != null) {
			redisCacheConfiguration = redisCacheConfiguration.entryTtl(cacheProperties.getRedis().getTimeToLive());
		} else {
			redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofHours(1)); // 缓存失效时间默认改成1小时
		}
		if (!cacheProperties.getRedis().isUseKeyPrefix()) {
			redisCacheConfiguration = redisCacheConfiguration.disableKeyPrefix();
		}
		if (redisCacheConfiguration.usePrefix() && StringUtils.isNotEmpty(cacheProperties.getRedis().getKeyPrefix())) {
			redisCacheConfiguration = redisCacheConfiguration.prefixCacheNameWith(cacheProperties.getRedis().getKeyPrefix());
		}
		if (!cacheProperties.getRedis().isCacheNullValues()) {
			redisCacheConfiguration = redisCacheConfiguration.disableCachingNullValues();
		}
		return redisCacheConfiguration;
	}
}
