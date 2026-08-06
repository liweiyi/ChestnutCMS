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

import com.chestnut.message.core.Control;
import com.chestnut.message.core.IMessageType;
import tools.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * SmsSendParams
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class SmsSendParams implements IMessageType.Props<SmsSendParams> {

    @Control(label = "{SMS.PROP.PHONE_NUMBERS}", type = Control.ControlType.INPUT_TAG)
    private List<String> phoneNumbers;

    @Control(label = "{SMS.PROP.TEMPLATE_ID}")
    private String templateId;

    @Control(label = "{SMS.PROP.TEMPLATE_VARIABLES}")
    private HashMap<String, String> templateVariables;

    @Override
    public SmsSendParams fromJson(JsonNode json) {
        this.phoneNumbers = new ArrayList<>();
        for (JsonNode jsonNode : json.required("phoneNumbers").values()) {
            this.phoneNumbers.add(jsonNode.asString());
        }
        this.templateId = json.required("templateId").asString();
        this.templateVariables = new HashMap<>();
        json.required("templateVariables").properties().forEach(entry -> {
            this.templateVariables.put(entry.getKey(), entry.getValue().asString());
        });
        return this;
    }
}
