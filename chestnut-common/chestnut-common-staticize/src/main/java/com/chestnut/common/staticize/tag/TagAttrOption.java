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
package com.chestnut.common.staticize.tag;

import lombok.Getter;
import lombok.Setter;

/**
 * 标签属性可选项
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class TagAttrOption {

    /**
     * 标签属性可选项值
     */
    String value;

    /**
     * 标签属性可选项描述
     */
    String desc;

    public TagAttrOption(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String lowerCaseValue() {
        return this.value.toLowerCase();
    }
}
