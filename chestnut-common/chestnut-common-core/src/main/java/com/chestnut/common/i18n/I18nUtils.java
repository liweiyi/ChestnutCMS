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
package com.chestnut.common.i18n;

import com.chestnut.common.utils.ReflectASMUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.PropertyPlaceholderHelper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
public class I18nUtils {

	private final static MessageSource messageSource = SpringUtils
			.getBean(AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME);

	private static final PropertyPlaceholderHelper FieldPlaceholderHelper = new PropertyPlaceholderHelper("#{", "}");

	private static final I18nPlaceholderHelper PlaceholderHelper = new I18nPlaceholderHelper("{", "}", ":",
			true);

	public static void replaceI18nFields(List<?> objs) {
		replaceI18nFields(objs, LocaleContextHolder.getLocale());
	}

	/**
	 * 将列表所有对象中@I18nField标注的字段替换为指定国际化语言值
	 *
	 * @param objs
	 * @param locale
	 */
	public static void replaceI18nFields(List<?> objs, Locale locale) {
		if (Objects.isNull(objs)) {
			return;
		}
		objs.forEach(obj -> replaceI18nFields(obj, locale));
	}

	public static void replaceI18nFields(Object obj) {
		replaceI18nFields(obj, LocaleContextHolder.getLocale());
	}

	/**
	 * 将对象有@I18nField标注的字段替换为指定国际化语言值
	 *
	 * @param obj
	 * @param locale
	 */
	public static void replaceI18nFields(Object obj, Locale locale) {
		if (Objects.isNull(obj)) {
			return;
		}
		Field[] declaredFields = obj.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			if (field.isAnnotationPresent(I18nField.class)) {
				I18nField i18nField = field.getAnnotation(I18nField.class);
				try {
					if (StringUtils.isNotEmpty(i18nField.value())) {
						String langStr = i18nField.value();
						// 先将#{fieldName}占位替换为obj字段值
						langStr = FieldPlaceholderHelper.replacePlaceholders(langStr, placehoderName -> {
							Object fieldV = ReflectASMUtils.invokeGetter(obj, placehoderName);
							return Objects.nonNull(fieldV) ? fieldV.toString() : StringUtils.EMPTY;
						});
						String langValue = get(langStr, locale);
						ReflectASMUtils.invokeSetter(obj, field.getName(), langValue);
					} else {
						Object fieldV = ReflectASMUtils.invokeGetter(obj, field.getName());
						if (Objects.nonNull(fieldV)) {
							String langKey = fieldV.toString();
							String langValue = get(langKey, locale);
							ReflectASMUtils.invokeSetter(obj, field.getName(), langValue);
						}
					}
				} catch (Exception e) {
					log.warn("Replace i18n field failed: {}.{}", obj.getClass().getName(), field.getName());
				}
			}
		}
	}

	/**
	 * 获取国际化键名对应的当前默认语言值
	 *
	 * @param str
	 * @return
	 */
	public static String get(String str) {
		return get(str, LocaleContextHolder.getLocale());
	}


	public static String get(String str, Object... args) {
		return get(str, LocaleContextHolder.getLocale(), args);
	}

	/**
	 * 获取国际化键名指定的语言值
	 *
	 * @param str
	 * @param locale
	 * @param args
	 * @return
	 */
	public static String get(String str, Locale locale, Object... args) {
		if (StringUtils.isEmpty(str)) {
			return str;
		}
		return PlaceholderHelper.replacePlaceholders(str, langKey -> messageSource.getMessage(langKey, args, locale));
	}
}
