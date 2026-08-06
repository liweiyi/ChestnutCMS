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
import com.chestnut.message.core.MessageRateLimitConfig;
import com.chestnut.message.core.sms.aliyun.AliYunSmsProvider;
import com.chestnut.message.core.sms.tencent.TencentSmsProvider;
import tools.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * SmsProps
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class SmsProps implements IMessageType.Props<SmsProps> {

    @Control(label = "{SMS.PROP.PROVIDER}", type = Control.ControlType.SELECT, options = {
            @Control.ControlOption(value = TencentSmsProvider.ID, label = TencentSmsProvider.NAME),
            @Control.ControlOption(value = AliYunSmsProvider.ID, label = AliYunSmsProvider.NAME)
    })
    private String provider;

    @Control(label = "{SMS.PROP.SECRET_ID}")
    private String secretId;

    @Control(label = "{SMS.PROP.SECRET_KEY}")
    private String secretKey;

    @Control(label = "{SMS.PROP.APP_ID}")
    private String appId;

    @Control(label = "{SMS.PROP.SIGN_NAME}")
    private String signName;

    @Control(label = "{SMS.PROP.REGION}")
    private String region;

    @Control(label = "{SMS.PROP.TEMPLATE_ID}")
    private String templateId;

    @Control(label = "{MESSAGE.PROP.RATE_LIMITS}", type = Control.ControlType.INPUT_PAIRS)
    private Map<String, String> rateLimits;

    @Override
    public SmsProps fromJson(JsonNode json) {
        this.provider = json.get("provider").asString();
        this.secretId = json.get("secretId").asString();
        this.secretKey = json.get("secretKey").asString();
        this.appId = json.get("appId").asString();
        this.signName = json.get("signName").asString();
        this.region = json.get("region").asString();
        this.templateId = json.get("templateId").asString();
        MessageRateLimitConfig rateLimitConfig = MessageRateLimitConfig.fromJson(json);
        this.rateLimits = rateLimitConfig.toMap();
        return this;
    }
}
