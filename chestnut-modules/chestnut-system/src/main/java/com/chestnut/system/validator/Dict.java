package com.chestnut.system.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = DictValidator.class)
public @interface Dict {

	String message() default "{VALIDATOR.SYSTEM.INVALID_DICT_VALUE}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * 字典类型
	 * 
	 * @return dictType
	 */
	String value();
}
