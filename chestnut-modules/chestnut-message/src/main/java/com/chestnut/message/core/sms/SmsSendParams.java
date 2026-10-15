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
package com.chestnut.message.core.sms;

import com.chestnut.common.utils.Assert;
import com.chestnut.message.exception.MessageErrorCode;
import com.chestnut.message.core.Control;
import com.chestnut.message.core.IMessageType;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * SmsSendParams
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class SmsSendParams implements IMessageType.Props<SmsSendParams> {

    @Control(label = "{SMS.PROP.TEMPLATE_ID}")
    private String templateId;

    @Control(label = "{SMS.PROP.TEMPLATE_VARIABLES}")
    private Map<String, String> templateVariables;

    @Override
    public SmsSendParams fromJson(JsonNode json) {
        this.templateId = json == null ? null : json.path("templateId").asString(null);
        this.templateVariables = new LinkedHashMap<>();
        JsonNode templateVariablesNode = json == null ? null : json.get("templateVariables");
        if (Objects.nonNull(templateVariablesNode) && !templateVariablesNode.isNull()) {
            Assert.isTrue(templateVariablesNode.isObject(), MessageErrorCode.INVALID_USER_MESSAGE_REQUEST::exception);
            templateVariablesNode.properties().forEach(entry -> {
                this.templateVariables.put(entry.getKey(), entry.getValue().asString());
            });
        }
        return this;
    }
}
