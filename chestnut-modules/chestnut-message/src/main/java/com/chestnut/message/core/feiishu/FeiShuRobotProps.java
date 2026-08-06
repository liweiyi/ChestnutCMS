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
package com.chestnut.message.core.feiishu;

import com.chestnut.message.core.Control;
import com.chestnut.message.core.IMessageType;
import tools.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

/**
 * FeiShuRobotProps
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class FeiShuRobotProps implements IMessageType.Props<FeiShuRobotProps> {

    @Control(label = "WebHook")
    private String webHook;

    @Control(label = "Secret")
    private String secret;

    @Override
    public FeiShuRobotProps fromJson(JsonNode json) {
        this.webHook = json.get("webHook").asString();
        this.secret = json.get("secret").asString();
        return this;
    }
}
