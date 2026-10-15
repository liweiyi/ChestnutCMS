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

import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.exception.MessageErrorCode;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
     * 是否支持配置本地消息模板
     */
    default boolean supportedTemplate() {
        return true;
    }

    /**
     * 校验配置参数
     */
    default void validate(ObjectNode configs) {

    }

    /**
     * 校验用户主动发送的参数。消息类型可扩展校验规则，或抛出异常拒绝用户主动发送。
     */
    default void checkSendRequest(SendMessageReq req) {
        Assert.isTrue(IdUtils.validate(req.getMessageConfigId()),
                MessageErrorCode.INVALID_USER_MESSAGE_REQUEST::exception);
        if (supportedTemplate()) {
            Assert.isTrue(IdUtils.validate(req.getMessageTemplateId()),
                    MessageErrorCode.INVALID_USER_MESSAGE_REQUEST::exception);
        }
    }

    /**
     * 是否需要显式指定单个接收人；群机器人使用消息配置中的 Webhook。
     */
    default boolean requiresReceiver() {
        return false;
    }

    /**
     * 构造类型专用的发送参数。
     *
     * @param messageConfig 消息配置
     * @param variables 消息自定义参数
     */
    default ObjectNode buildSendParams(@Nonnull CcMessageConfig messageConfig, ObjectNode variables) {
        return variables.deepCopy();
    }

    /**
     * 发送一条消息；显式接收人只使用 Message.receiver，群机器人使用配置中的 Webhook。
     */
    void send(Message message);

    /**
     * 发送消息请求参数
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    class SendMessageReq {
        private String receiver; // 接收人，可为空
        private Long messageConfigId; // 消息配置ID
        private Long messageTemplateId; // 消息模版ID
        private ObjectNode templateVariables; // 消息内容模板变量，不可随意插入数据，短信平台会做参数校验。
    }

    record Message(String title, String content, String receiver, ObjectNode configProps) {}

    interface Props<T extends Props<T>> extends Serializable {

        T fromJson(JsonNode json);
    }
}
