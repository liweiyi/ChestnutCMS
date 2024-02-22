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
package com.chestnut.system.config;

import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.time.Duration;
import java.util.Locale;

/**
 * 动态国际化字符串存储数据库
 */
@Order
@Configuration
@EnableConfigurationProperties
@RequiredArgsConstructor
public class SysI18nConfig {

	private final RedisCache redisCache;

	@Bean
	@ConfigurationProperties(prefix = "spring.messages")
	public MessageSourceProperties messageSourceProperties() {
		return new MessageSourceProperties();
	}

	@Bean("messageSource")
	public MessageSource messageSource(MessageSourceProperties properties) {
		I18nMessageSource messageSource = new I18nMessageSource(this.redisCache);
		if (StringUtils.isNotBlank(properties.getBasename())) {
			messageSource.setBasename(properties.getBasename());
		}
		if (properties.getEncoding() != null) {
			messageSource.setEncoding(properties.getEncoding());
		}
//		messageSource.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
		Duration cacheDuration = properties.getCacheDuration();
		if (cacheDuration != null) {
			messageSource.setCacheSeconds(cacheDuration.toSeconds());
		}
		messageSource.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
		messageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
		return messageSource;
	}

	/**
	 * 语言区域解析器
	 * 
	 * RequestParam -> Accept-Language -> Default
	 * 
	 * @return
	 */
	@Bean
	public LocaleResolver localeResolver() {
		XyLocaleResolver localeResolver = new XyLocaleResolver();
		localeResolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
		return localeResolver;
	}

	public static class XyLocaleResolver extends AcceptHeaderLocaleResolver {
		
		@Override
		public Locale resolveLocale(HttpServletRequest request) {
			String languageTag = request.getParameter("lang");
			if (StringUtils.isNotEmpty(languageTag)) {
				Locale locale = Locale.forLanguageTag(languageTag);
				if (this.getSupportedLocales().contains(locale)) {
					return locale;
				}
			}
			return super.resolveLocale(request);
		}
	}
}
