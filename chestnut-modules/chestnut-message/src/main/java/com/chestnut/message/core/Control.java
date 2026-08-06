/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
package com.chestnut.message.core;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Control {

    /**
     * 类型
     */
    ControlType type() default ControlType.INPUT;

    /**
     * 控件可选值字典类型，优先级高于options
     */
    String dictType() default "";

    /**
     * 控件可选值
     */
    ControlOption[] options() default {};

    /**
     * 名称
     */
    String label();

    String pairsKeyName() default "KEY";

    String pairsValueName() default "VALUE";

    @interface ControlOption {

        String label();

        String value();
    }

    enum ControlType {
        /**
         * 输入框
         */
        INPUT,
        /**
         * 数字输入框
         */
        INPUT_NUMBER,
        /**
         * 标签输入框
         */
        INPUT_TAG,
        /**
         * 键值对输入框
         */
        INPUT_PAIRS,
        /**
         * 开关
         */
        SWITCH,
        /**
         * 单选框
         */
        RADIO,
        /**
         * 多选框
         */
        CHECKBOX,
        /**
         * 下拉框
         */
        SELECT,
        /**
         * 日期选择
         */
        DATE,
        /**
         * 时间选择
         */
        TIME,
        /**
         * 日期时间选择
         */
        DATETIME,
    }
}
