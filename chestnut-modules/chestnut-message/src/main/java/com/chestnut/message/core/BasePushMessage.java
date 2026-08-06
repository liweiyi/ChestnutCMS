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

import lombok.Getter;
import lombok.Setter;

/**
 * 消息推送内容
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class BasePushMessage {

    /**
     * 消息推送配置ID
     */
    private Long configId;

    /**
     * 消息内容
     */
    private String content;

    public String toJson() {
        return null;
    }
}
