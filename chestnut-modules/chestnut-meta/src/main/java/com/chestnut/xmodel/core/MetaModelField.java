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
package com.chestnut.xmodel.core;

import com.chestnut.xmodel.domain.XModelField;
import com.chestnut.xmodel.dto.FieldOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class MetaModelField {

    private String name;

    private String code;

    private String fieldName;

    private Object fieldValue;

    private boolean editable;

    private String defaultValue;

    private boolean primaryKey;

    private String controlType;

    private FieldOptions options;

    private List<Map<String, Object>> validations;

    public MetaModelField(String name, String code, String fieldName,
                          boolean primaryKey, String controlType) {
        this.name = name;
        this.code = code;
        this.fieldName = fieldName;
        this.primaryKey = primaryKey;
        this.controlType = controlType;
    }

    public MetaModelField(XModelField field) {
        this.name = field.getName();
        this.code = field.getCode();
        this.fieldName = field.getFieldName();
        this.controlType = field.getControlType();
        this.defaultValue = field.getDefaultValue();
        this.options = field.getOptions();
        this.validations = field.getValidations();
        this.editable = true;
    }
}