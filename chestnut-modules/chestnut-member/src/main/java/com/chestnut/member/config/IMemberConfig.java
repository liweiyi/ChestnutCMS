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
package com.chestnut.member.config;

import com.chestnut.common.utils.JacksonUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * 会员配置项扩展接口。
 *
 * @param <T> 配置值类型
 */
public interface IMemberConfig<T> {

    String BEAN_PREFIX = "MemberConfig_";

    /**
     * JSON 配置键。
     */
    String getId();

    /**
     * 国际化配置名称。
     */
    String getName();

    /**
     * 配置值类型。
     */
    Class<T> getValueType();

    /**
     * 默认值。
     */
    T getDefaultValue();

    /**
     * 后台配置控件类型。
     */
    default MemberConfigControlType getControlType() {
        return MemberConfigControlType.INPUT;
    }

    /**
     * 消息选择控件限定的消息类型。
     */
    default String getMessageType() {
        return null;
    }

    /**
     * 已废弃的配置键，规范化配置时自动移除。
     */
    default List<String> getDeprecatedIds() {
        return List.of();
    }

    /**
     * 配置项显示顺序。
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 读取并转换配置值，配置缺失时返回默认值。
     */
    default T getValue(ObjectNode configs) {
        JsonNode value = configs == null ? null : configs.get(this.getId());
        if (value == null || value.isNull()) {
            return this.getDefaultValue();
        }
        return JacksonUtils.convertValue(value, this.getValueType());
    }

    /**
     * 校验转换后的配置值。
     */
    default boolean validate(T value) {
        return true;
    }
}
