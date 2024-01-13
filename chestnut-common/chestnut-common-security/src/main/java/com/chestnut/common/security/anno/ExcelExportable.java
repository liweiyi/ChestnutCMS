package com.chestnut.common.security.anno;

import java.lang.annotation.*;

/**
 * Excel导出注解
 *
 * 使用：
 * 在Controller的请求方法上添加，要求：
 * 1. 返回值类型为: R<TableData<注解指定的class>>
 * 2. 前端发起导出请求方式固定为: POST
 * 3. 前端发起导出请求带请求头：ExcelExportAspect.CONDITION_HEADER
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelExportable {

    /**
     * 导出数据类型
     */
    Class<?> value();
}
