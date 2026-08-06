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
import com.chestnut.message.core.MessageRateLimitConfig;
import tools.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * EmailProps
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class EmailProps implements IMessageType.Props<EmailProps> {

    @Control(label = "{EMAIL.PROP.HOST}")
    private String host;

    @Control(label = "{EMAIL.PROP.PORT}", type = Control.ControlType.INPUT_NUMBER)
    private Integer port;

    @Control(label = "{EMAIL.PROP.SECURE}", type = Control.ControlType.SWITCH)
    private String secure;

    @Control(label = "{EMAIL.PROP.USER}")
    private String user;

    @Control(label = "{EMAIL.PROP.PWD}")
    private String password;

    @Control(label = "{EMAIL.PROP.FROM}")
    private String from;

    @Control(label = "{MESSAGE.PROP.RATE_LIMITS}", type = Control.ControlType.INPUT_PAIRS, pairsKeyName = "{MESSAGE.PROP.RATE_LIMITS.KEY_NAME}", pairsValueName = "{MESSAGE.PROP.RATE_LIMITS.VALUE_NAME}")
    private Map<String, String> rateLimits;

    @Control(label = "Properties", type = Control.ControlType.INPUT_PAIRS)
    private Map<String, String> properties;

    @Override
    public EmailProps fromJson(JsonNode json) {
        this.host = json.get("host").asString();
        this.port = json.get("port").asInt();
        this.secure = json.get("secure").asString();
        this.user = json.get("user").asString();
        this.password = json.get("password").asString();
        this.from = json.get("from").asString();
        MessageRateLimitConfig rateLimitConfig = MessageRateLimitConfig.fromJson(json);
        this.rateLimits = rateLimitConfig.toMap();
        if (json.has("properties")) {
            this.properties = new HashMap<>();
            json.get("properties").properties().forEach(e -> {
                this.properties.put(e.getKey(), e.getValue().asString());
            });
        }
        return this;
    }
}
