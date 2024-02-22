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
package com.chestnut.cms.exmodel;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.xmodel.core.BaseModelData;
import jakarta.validation.constraints.NotNull;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class BaseModelDataGenerator {


    public static void main(String[] args) {
        List<String> stringFields = new ArrayList<>();
        List<String> longFields = new ArrayList<>();
        List<String> doubleFields = new ArrayList<>();
        List<String> dateFields = new ArrayList<>();
        Field[] declaredFields = BaseModelData.class.getDeclaredFields();
        for (Field field : declaredFields) {
            String fieldName = StringUtils.toUnderScoreCase(field.getName());
            if (field.getType() == String.class) {
                stringFields.add(fieldName);
            } else if (field.getType() == Long.class) {
                longFields.add(fieldName);
            } else if (field.getType() == Double.class) {
                doubleFields.add(fieldName);
            } else if (field.getType() == LocalDateTime.class) {
                dateFields.add(fieldName);
            }
        }
        StringBuilder sb = new StringBuilder();

        sb.append("public boolean isStringField(@NotNull String fieldName) {\n");
        sb.append("\treturn switch(fieldName) {\n");
        String collect = stringFields.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(", "));
        sb.append("\t\tcase " + collect + " -> true;");
        sb.append("\t\tdefault -> false;\n");
        sb.append("\t};\n");
        sb.append("}\n\n");

        sb.append("public String getStringFieldValue(@NotNull String fieldName) {\n");
        sb.append("\treturn switch(fieldName) {\n");
        stringFields.forEach(field -> {
            String getMethod = "get" + StringUtils.upperFirst(StringUtils.toCamelCase(field));
            sb.append("\t\tcase \"").append(field).append("\" -> ").append(getMethod).append("();\n");
        });
        sb.append("\t\tdefault -> throw new RuntimeException(\"Unknown field name: \" + fieldName);\n");
        sb.append("\t};\n");
        sb.append("}\n\n");

        sb.append("public Long getLongFieldValue(@NotNull String fieldName) {\n");
        sb.append("\treturn switch(fieldName) {\n");
        longFields.forEach(field -> {
            String getMethod = "get" + StringUtils.upperFirst(StringUtils.toCamelCase(field));
            sb.append("\t\tcase \"").append(field).append("\" -> ").append(getMethod).append("();\n");
        });
        sb.append("\t\tdefault -> throw new RuntimeException(\"Unknown field name: \" + fieldName);\n");
        sb.append("\t};\n");
        sb.append("}\n\n");

        sb.append("public Double getDoubleFieldValue(@NotNull String fieldName) {\n");
        sb.append("\treturn switch(fieldName) {\n");
        doubleFields.forEach(field -> {
            String getMethod = "get" + StringUtils.upperFirst(StringUtils.toCamelCase(field));
            sb.append("\t\tcase \"").append(field).append("\" -> ").append(getMethod).append("();\n");
        });
        sb.append("\t\tdefault -> throw new RuntimeException(\"Unknown field name: \" + fieldName);\n");
        sb.append("\t};\n");
        sb.append("}\n\n");

        sb.append("public LocalDateTime getDateFieldValue(@NotNull String fieldName) {\n");
        sb.append("\treturn switch(fieldName) {\n");
        dateFields.forEach(field -> {
            String getMethod = "get" + StringUtils.upperFirst(StringUtils.toCamelCase(field));
            sb.append("\t\tcase \"").append(field).append("\" -> ").append(getMethod).append("();\n");
        });
        sb.append("\t\tdefault -> throw new RuntimeException(\"Unknown field name: \" + fieldName);\n");
        sb.append("\t};\n");
        sb.append("}\n\n");

        sb.append("public Object getFieldValue(@NotNull String fieldName) {\n");
        sb.append("\treturn switch(fieldName) {\n");
        stringFields.forEach(field -> {
            String getMethod = "get" + StringUtils.upperFirst(StringUtils.toCamelCase(field));
            sb.append("\t\tcase \"").append(field).append("\" -> ").append(getMethod).append("();\n");
        });
        longFields.forEach(field -> {
            String getMethod = "get" + StringUtils.upperFirst(StringUtils.toCamelCase(field));
            sb.append("\t\tcase \"").append(field).append("\" -> ").append(getMethod).append("();\n");
        });
        doubleFields.forEach(field -> {
            String getMethod = "get" + StringUtils.upperFirst(StringUtils.toCamelCase(field));
            sb.append("\t\tcase \"").append(field).append("\" -> ").append(getMethod).append("();\n");
        });
        dateFields.forEach(field -> {
            String getMethod = "get" + StringUtils.upperFirst(StringUtils.toCamelCase(field));
            sb.append("\t\tcase \"").append(field).append("\" -> ").append(getMethod).append("();\n");
        });
        sb.append("\t\tdefault -> throw new RuntimeException(\"Unknown field name: \" + fieldName);\n");
        sb.append("\t};\n");
        sb.append("}\n\n");

        sb.append("public void setFieldValue(@NotNull String fieldName, Object fieldValue) {\n");
        sb.append("\tswitch(fieldName) {\n");

        stringFields.forEach(field -> {
            String setMethod = "set" + StringUtils.upperFirst(StringUtils.toCamelCase(field));
            sb.append("\t\tcase \"").append(field).append("\" -> ").append(setMethod).append("(ConvertUtils.toStr(fieldValue));\n");
        });

        longFields.forEach(field -> {
            String setMethod = "set" + StringUtils.upperFirst(StringUtils.toCamelCase(field));
            sb.append("\t\tcase \"").append(field).append("\" -> ").append(setMethod).append("(ConvertUtils.toLong(fieldValue));\n");
        });

        doubleFields.forEach(field -> {
            String setMethod = "set" + StringUtils.upperFirst(StringUtils.toCamelCase(field));
            sb.append("\t\tcase \"").append(field).append("\" -> ").append(setMethod).append("(ConvertUtils.toDouble(fieldValue));\n");
        });

        dateFields.forEach(field -> {
            String setMethod = "set" + StringUtils.upperFirst(StringUtils.toCamelCase(field));
            sb.append("\t\tcase \"").append(field).append("\" -> ").append(setMethod).append("(ConvertUtils.toLocalDateTime(fieldValue));\n");
        });

        sb.append("\t\tdefault -> throw new RuntimeException(\"Unknown field name: \" + fieldName);\n");
        sb.append("\t}\n");
        sb.append("}");
        System.out.println(sb);
    }
}
