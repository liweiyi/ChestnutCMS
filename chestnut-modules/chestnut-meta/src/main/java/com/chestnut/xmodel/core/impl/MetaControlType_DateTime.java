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

import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.core.IMetaControlType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * 元数据模型字段类型：日期时间选择框
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaControlType.BEAN_PREFIX + MetaControlType_DateTime.TYPE)
public class MetaControlType_DateTime implements IMetaControlType {

    public static final String TYPE = "datetime";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "{META.CONTROL_TYPE." + TYPE + "}";
    }

    @Override
    public String valueAsString(Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (obj instanceof LocalDateTime ldt) {
            ldt.format(DateUtils.FORMAT_YYYY_MM_DD);
        } else if(obj instanceof LocalDate ldt) {
            ldt.format(DateUtils.FORMAT_YYYY_MM_DD);
        } else if (obj instanceof Date date) {
            return DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, date);
        }
        return IMetaControlType.super.valueAsString(obj);
    }

    @Override
    public Object stringAsValue(String valueStr) {
        try {
            Date date = DateUtils.parseDate(valueStr);
            return DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, date);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }
}
