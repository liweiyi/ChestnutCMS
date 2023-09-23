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