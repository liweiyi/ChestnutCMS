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
