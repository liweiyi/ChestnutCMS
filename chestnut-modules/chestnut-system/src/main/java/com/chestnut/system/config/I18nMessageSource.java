/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.common.utils.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractMessageSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
public class I18nMessageSource extends AbstractMessageSource implements InitializingBean {

	private final static String CACHE_PREFIX = "i18n:";

	private final RedisCache redisCache;

	private List<String> basename;

	private Charset encoding = StandardCharsets.UTF_8;

	private long cacheSeconds;

	private Locale defaultLocale = Locale.SIMPLIFIED_CHINESE;

	@Override
	public void afterPropertiesSet() {
	}

	@Override
	protected MessageFormat resolveCode(@NotNull String code, Locale locale) {
		Object value = this.redisCache.getCacheMapValue(CACHE_PREFIX + locale.toLanguageTag(), code);
		if (Objects.nonNull(value)) {
			return new MessageFormat(value.toString(), locale);
		}
		return null;
	}
	
	@Override
	protected String resolveCodeWithoutArguments(@NotNull String code, Locale locale) {
		Object value = this.redisCache.getCacheMapValue(CACHE_PREFIX + locale.toLanguageTag(), code);
		return ConvertUtils.toStr(value);
	}

	@Override
	protected String getDefaultMessage(@NotNull String code) {
		Object value = this.redisCache.getCacheMapValue(CACHE_PREFIX + this.getDefaultLocale().toLanguageTag(), code);
		return ConvertUtils.toStr(value, isUseCodeAsDefaultMessage() ? code : StringUtils.EMPTY);
	}
}
