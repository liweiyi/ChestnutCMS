package com.chestnut.system.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = LongIdValidator.class)
public @interface LongId {

	String message() default "{VALIDATOR.SYSTEM.INVALID_LONG_ID}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}