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
package com.chestnut.contentcore.template.func;

import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.ConvertUtils;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.NumberUtils;
import com.chestnut.common.utils.StringUtils;
import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Freemarker模板自定义函数：日期格式化
 */
@Component
@RequiredArgsConstructor
public class DateFormatFunction extends AbstractFunc {

	static final String FUNC_NAME = "dateFormat";

	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	private static final SimpleDateFormat DEFAULT_SIMPLE_DATE_FORMAT = new SimpleDateFormat(
			DateUtils.YYYY_MM_DD_HH_MM_SS);

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
		if (args[0] instanceof BeanModel model) {
			Object obj = model.getWrappedObject();

			if (obj instanceof TemporalAccessor t) {
				if (args.length > 1) {
					return DateTimeFormatter.ofPattern(args[1].toString()).format(t);
				} else {
					return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(t);
				}
			} else if (obj instanceof Date d) {
				if (args.length > 1) {
					String formatStr = ConvertUtils.toStr(args[1], DateUtils.YYYY_MM_DD_HH_MM_SS);
					return DateUtils.parseDateToStr(formatStr, d);
				} else {
					return DEFAULT_SIMPLE_DATE_FORMAT.format(d);
				}
			}
		} else if (args[0] instanceof SimpleScalar s) {
			String value = s.getAsString();
			if (NumberUtils.isCreatable(value)) {
				LocalDateTime dateTime = Instant.ofEpochMilli(ConvertUtils.toLong(value))
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				return DateTimeFormatter.ofPattern(args[1].toString()).format(dateTime);
			}
		} else if (args[0] instanceof SimpleNumber s) {
			long value = s.getAsNumber().longValue();
			LocalDateTime dateTime = Instant.ofEpochMilli(value)
					.atZone(ZoneId.systemDefault()).toLocalDateTime();
			return DateTimeFormatter.ofPattern(args[1].toString()).format(dateTime);
		}
		return args[0].toString();
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("日期时间", FuncArgType.DateTime, true, null),
				new FuncArg("格式化字符串", FuncArgType.String, false, "默认格式：yyyy-MM-dd HH:mm:ss"));
	}
}
