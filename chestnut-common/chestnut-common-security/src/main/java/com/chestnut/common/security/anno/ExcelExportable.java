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
