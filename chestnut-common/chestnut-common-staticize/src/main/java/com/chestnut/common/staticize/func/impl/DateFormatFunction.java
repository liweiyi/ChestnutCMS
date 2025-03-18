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
package com.chestnut.common.staticize.func.impl;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.*;
import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Freemarker模板自定义函数：日期格式化
 */
@Component
@RequiredArgsConstructor
public class DateFormatFunction extends AbstractFunc {

	static final String FUNC_NAME = "dateFormat";

	private static final String DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".DESC}";

	private static final String ARG1_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Name}";

	private static final String ARG2_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg2.Name}";

	private static final String ARG3_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg3.Name}";

	@Override
	public String getFuncName() {
		return FUNC_NAME;
	}

	@Override
	public String getDesc() {
		return DESC;
	}

	@Override
	public Object exec0(Object... args) throws TemplateModelException {
		if (args.length < 1 || Objects.isNull(args[0])) {
			return StringUtils.EMPTY;
		}
		Locale locale = args.length == 3 ? Locale.forLanguageTag(args[2].toString()) : Locale.getDefault();
		if (args[0] instanceof BeanModel model) {
			Object obj = model.getWrappedObject();
			if (obj instanceof TemporalAccessor t) {
				if (args.length > 1) {
					return DateTimeFormatter.ofPattern(args[1].toString(), locale).format(t);
				} else {
					return DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS, locale).format(t);
				}
			} else if (obj instanceof Date d) {
				if (args.length > 1) {
					String formatStr = ConvertUtils.toStr(args[1], DateUtils.YYYY_MM_DD_HH_MM_SS);
					SimpleDateFormat format = new SimpleDateFormat(formatStr, locale);
					return format.format(d);
				} else {
					SimpleDateFormat format = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS, locale);
					return format.format(d);
				}
			}
		} else if (args[0] instanceof SimpleScalar s) {
			String value = s.getAsString();
			if (NumberUtils.isCreatable(value)) {
				LocalDateTime dateTime = TimeUtils.toLocalDateTime(Instant.ofEpochMilli(ConvertUtils.toLong(value)));
				return DateTimeFormatter.ofPattern(args[1].toString(), locale).format(dateTime);
			}
		} else if (args[0] instanceof SimpleNumber s) {
			long value = s.getAsNumber().longValue();
			LocalDateTime dateTime = TimeUtils.toLocalDateTime(Instant.ofEpochMilli(value));
			return DateTimeFormatter.ofPattern(args[1].toString(), locale).format(dateTime);
		}
		return args[0].toString();
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg(ARG1_NAME, FuncArgType.DateTime, true),
				new FuncArg(ARG2_NAME, FuncArgType.String, false, null, "yyyy-MM-dd HH:mm:ss"),
				new FuncArg(ARG3_NAME, FuncArgType.String, false, null, Locale.getDefault().toLanguageTag()));
	}
}
