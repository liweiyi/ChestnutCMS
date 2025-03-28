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

import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.core.IMetaControlType;
import com.chestnut.xmodel.dto.XModelFieldDataDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 元数据模型字段类型：多选框
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaControlType.BEAN_PREFIX + MetaControlType_Checkbox.TYPE)
public class MetaControlType_Checkbox implements IMetaControlType {

    public static final String TYPE = "checkbox";

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
        return JacksonUtils.to(obj);
    }

    @Override
    public Object stringAsValue(String valueStr) {
        if (StringUtils.isEmpty(valueStr)) {
            return List.of();
        }
        if (!JacksonUtils.isJson(valueStr)) {
            return StringUtils.split(valueStr, StringUtils.COMMA);
        }
        return JacksonUtils.fromList(valueStr, String.class);
    }
}
