package com.chestnut.common.security.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.dev33.satoken.annotation.SaMode;

/**
 * SaToken的@SaCheckLogin和@SaCheckPermission合并
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Priv {

    /**
     * 多账号体系下所属的账号体系标识 
     */
    String type();
    
    /**
     * 需要校验的权限码
     */
    String[] value() default {};

    /**
     * 验证模式：AND | OR，默认OR
     */
    SaMode mode() default SaMode.OR;
}
