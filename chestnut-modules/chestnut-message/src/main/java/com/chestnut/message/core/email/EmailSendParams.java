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
package com.chestnut.message.core.email;

import com.chestnut.message.core.Control;
import com.chestnut.message.core.IMessageType;
import tools.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * EmailEventProps
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class EmailSendParams implements IMessageType.Props<EmailSendParams>{

    @Control(label = "{EMAIL.PROP.RECEIVERS}", type = Control.ControlType.INPUT_TAG)
    private List<String> mails;

    @Override
    public EmailSendParams fromJson(JsonNode json) {
        this.mails = new ArrayList<>();
        json.required("mails").values().forEach(node -> this.mails.add(node.asString()));
        return this;
    }
}
