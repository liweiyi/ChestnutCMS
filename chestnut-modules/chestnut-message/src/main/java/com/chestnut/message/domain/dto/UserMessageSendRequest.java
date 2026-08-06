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
package com.chestnut.message.domain.dto;

import com.chestnut.common.annotation.XComment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import tools.jackson.databind.node.ObjectNode;

/**
 * 用户发起的消息发送请求。
 */
@Getter
@AllArgsConstructor
@XComment("{API.DOC.MSG.USER_SEND_REQ}")
public class UserMessageSendRequest {

    @XComment("{API.DOC.MSG.MSG_TYPE}")
    private final String messageType;

    @XComment("{API.DOC.MSG.BUSINESS_SCENE}")
    private final String businessScene;

    @XComment("{API.DOC.MSG.USER_IDENTIFIER}")
    private final String userIdentifier;

    @XComment("{API.DOC.MSG.CONFIG_ID}")
    private final Long configId;

    @XComment("{API.DOC.MSG.TEMPLATE_ID}")
    private final Long templateId;

    @XComment("{API.DOC.MSG.SEND_PARAMS}")
    private final ObjectNode params;
}
