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
package com.chestnut.system.fixed.dict;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysDictData;
import com.chestnut.system.fixed.FixedDictType;
import com.chestnut.system.service.ISysDictTypeService;

/**
 * 密码校验规则，允许后台添加扩展，备注字段填写对应正则表达式即可
 */
@Component(FixedDictType.BEAN_PREFIX + PasswordRule.TYPE)
public class PasswordRule extends FixedDictType {

	public static final String TYPE = "SecurityPasswordRule";

	public static final String NONE = "NONE"; // 无要求

	public static final String LETTER_NUMBER = "LETTER_NUMBER"; // 必须包含字母、数字

	public static final String REGEX_LETTER_NUMBER = "^(?=.*\\d+)(?=.*[A-Za-z]+)[A-Za-z\\d!@#$%^&*?()\\[\\]{}<>:;,.'\"~·+=_-]+$";

	public static final String UPPER_LOW_LETTER_NUMBER = "UPPER_LOW_LETTER_NUMBER"; // 必须包含大小写字母、数字
	
	public static final String REGEX_UPPER_LOW_LETTER_NUMBER = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d!@#$%^&*?()\\[\\]{}<>:;,.'\"~·+=_-]+$";

	public static final String LETTER_NUMBER_SPECIAL = "LETTER_NUMBER_SPECIAL"; // 必须包含字母、数字、特殊字符
	
	public static final String REGEX_LETTER_NUMBER_SPECIAL = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*?()\\[\\]{}<>:;,.'\"~·+=_-])[A-Za-z\\d!@#$%^&*?()\\[\\]{}<>:;,.'\"~·+=_-]+$";

	public static final String UPPER_LOW_LETTER_NUMBER_SPECIAL = "UPPER_LOW_LETTER_NUMBER_SPECIAL"; // 必须包含大小写字母、数字、特殊字符

	public static final String REGEX_UPPER_LOW_LETTER_NUMBER_SPECIAL = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*?()\\[\\]{}<>:;,.'\"~·+=_-])[A-Za-z\\d!@#$%^&*?()\\[\\]{}<>:;,.'\"~·+=_-]+$";

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	private static final Map<String, Pattern> PatternMap = Map.of(
			LETTER_NUMBER, Pattern.compile(REGEX_LETTER_NUMBER),
			UPPER_LOW_LETTER_NUMBER, Pattern.compile(REGEX_UPPER_LOW_LETTER_NUMBER),
			LETTER_NUMBER_SPECIAL, Pattern.compile(REGEX_LETTER_NUMBER_SPECIAL),
			UPPER_LOW_LETTER_NUMBER_SPECIAL, Pattern.compile(REGEX_UPPER_LOW_LETTER_NUMBER_SPECIAL));
	
	public PasswordRule() {
		super(TYPE, "{DICT." + TYPE + "}", true);
		super.addDictData("{DICT." + TYPE + "." + NONE + "}", NONE, 1, "");
		super.addDictData("{DICT." + TYPE + "." + LETTER_NUMBER + "}", LETTER_NUMBER, 2, REGEX_LETTER_NUMBER);
		super.addDictData("{DICT." + TYPE + "." + UPPER_LOW_LETTER_NUMBER + "}", UPPER_LOW_LETTER_NUMBER, 3, REGEX_UPPER_LOW_LETTER_NUMBER);
		super.addDictData("{DICT." + TYPE + "." + LETTER_NUMBER_SPECIAL + "}", LETTER_NUMBER_SPECIAL, 4, REGEX_LETTER_NUMBER_SPECIAL);
		super.addDictData("{DICT." + TYPE + "." + UPPER_LOW_LETTER_NUMBER_SPECIAL + "}", UPPER_LOW_LETTER_NUMBER_SPECIAL, 5, REGEX_UPPER_LOW_LETTER_NUMBER_SPECIAL);
	}
	
	public static String getRuleRegex(String rule) {
		Optional<SysDictData> opt = dictTypeService.optDictData(TYPE, rule);
		return opt.isPresent() ? opt.get().getRemark() : StringUtils.EMPTY;
	}
	
	public static Pattern getRulePatter(String rule) {
		Pattern pattern = PatternMap.get(rule);
		if (Objects.isNull(pattern)) {
			Optional<SysDictData> opt = dictTypeService.optDictData(TYPE, rule);
			if (opt.isPresent() && StringUtils.isNotEmpty(opt.get().getRemark())) {
				pattern = Pattern.compile(opt.get().getRemark());
				PatternMap.put(rule, pattern);
			}
		}
		return pattern;
	}
	
	public static boolean match(String rule, String password) {
		Pattern pattern = getRulePatter(rule);
		if (Objects.isNull(pattern)) {
			return true;
		}
		return pattern.matcher(password).matches();
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
