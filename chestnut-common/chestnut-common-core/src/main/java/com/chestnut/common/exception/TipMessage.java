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
package com.chestnut.common.exception;

import com.chestnut.common.domain.R;
import com.chestnut.common.i18n.I18nUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Objects;

/**
 * 自定义提示消息接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface TipMessage {

	/**
	 * 错误信息编码，对应国际化文件key
	 */
	String value();

    default String locale(Object... args) {
        return locale(LocaleContextHolder.getLocale(), args);
    }

    default String locale(Locale locale, Object... args) {
        if (Objects.isNull(locale)) {
            locale = LocaleContextHolder.getLocale();
        }
        return I18nUtils.parse(this.value(), locale, args);
    }
}
