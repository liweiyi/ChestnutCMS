package com.chestnut.system.validator;

import org.springframework.context.i18n.LocaleContextHolder;

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.utils.IdUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LongIdValidator implements ConstraintValidator<LongId, Long> {

	private String message;
	
	@Override
	public boolean isValid(Long valueId, ConstraintValidatorContext context) {
		if (IdUtils.validate(valueId)) {
			return true;
		}
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(I18nUtils.get(message, LocaleContextHolder.getLocale(), valueId)).addConstraintViolation();
		return false;
	}

	@Override
	public void initialize(LongId longId) {
		this.message = longId.message();
	}
}