/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.xmodel.core.impl;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.core.IMetaFieldValidation;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 非空判断
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaFieldValidation.BEAN_PREFIX + MetaFieldValidation_NotEmpty.TYPE)
public class MetaFieldValidation_NotEmpty implements IMetaFieldValidation {

    static final String TYPE = "NotEmpty";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean validate(Object fieldValue, Map<String, Object> args) {
        if (Objects.isNull(fieldValue)) {
            return false;
        }
        if (fieldValue.getClass().isArray()) {
            Object[] arr = (Object[]) fieldValue;
            return arr.length > 0;
        } else if (fieldValue instanceof Collection<?> list) {
            return !list.isEmpty();
        }
        return  StringUtils.isNotEmpty(fieldValue.toString());
    }
}
