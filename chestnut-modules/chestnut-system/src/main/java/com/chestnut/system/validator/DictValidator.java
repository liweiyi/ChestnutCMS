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
package com.chestnut.system.validator;

import java.util.Optional;

import org.springframework.context.i18n.LocaleContextHolder;

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysDictData;
import com.chestnut.system.service.ISysDictTypeService;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DictValidator implements ConstraintValidator<Dict, String> {

	private String dictType;
	
	private String message;
	
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		Optional<SysDictData> findFirst = SpringUtils.getBean(ISysDictTypeService.class).selectDictDatasByType(dictType)
				.stream().filter(data -> StringUtils.equals(data.getDictValue(), value)).findFirst();
		if (findFirst.isPresent()) {
			return true;
		}
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(I18nUtils.get(message, LocaleContextHolder.getLocale(), dictType, value)).addConstraintViolation();
		return false;
	}

	@Override
	public void initialize(Dict dict) {
		this.dictType = dict.value();
		this.message = dict.message();
	}
}