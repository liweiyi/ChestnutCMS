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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;

/**
 * 消息类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMessageType<T> {

    String BEAN_PREFIX = "MessageType_";

    String getType();

    String getName();

    Class<T> getPropsClass();

    /**
     * 校验配置参数
     */
    default void validate(ObjectNode configs) {

    }

    /**
     * 发送消息
     */
    void send(ObjectNode jsonConfigProps, Message message);

    record Message(String title, String content, ObjectNode params) {}

    interface Props<T extends Props<T>> extends Serializable {

        T fromJson(JsonNode json);
    }
}
