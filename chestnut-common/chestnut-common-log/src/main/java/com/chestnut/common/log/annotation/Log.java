package com.chestnut.common.log.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.log.restful.RestfulLogType;

/**
 * 日志记录注解
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

	/**
	 * 日志标题，一般使用功能模块名称
	 */
	public String title() default "";

	/**
	 * 日志类型
	 */
	public String type() default RestfulLogType.TYPE;

	/**
	 * 功能类型
	 */
	public BusinessType businessType() default BusinessType.OTHER;

	/**
	 * 是否保存请求的参数
	 */
	public boolean isSaveRequestData() default true;

	/**
	 * 是否保存响应的参数
	 */
	public boolean isSaveResponseData() default true;
}
