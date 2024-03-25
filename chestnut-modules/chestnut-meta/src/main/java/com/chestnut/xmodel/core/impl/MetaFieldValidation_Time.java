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
package com.chestnut.xmodel.core.impl;

import com.chestnut.common.utils.ObjectUtils;
import com.chestnut.xmodel.core.IMetaFieldValidation;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * 时间格式：HH:mm:ss
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaFieldValidation.BEAN_PREFIX + MetaFieldValidation_Time.TYPE)
public class MetaFieldValidation_Time implements IMetaFieldValidation {

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    static final String TYPE = "Time";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean validate(Object fieldValue, Map<String, Object> args) {
        if (ObjectUtils.isNullOrEmptyStr(fieldValue)) {
            return true;
        }
        try {
            LocalTime.parse(fieldValue.toString(), FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
