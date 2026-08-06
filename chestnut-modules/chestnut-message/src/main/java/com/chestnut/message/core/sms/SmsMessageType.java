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

import com.chestnut.common.utils.StringUtils;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.core.MessageRateLimitConfig;
import com.chestnut.message.service.impl.SmsService;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 手机短信消息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IMessageType.BEAN_PREFIX + SmsMessageType.TYPE)
public class SmsMessageType implements IMessageType<SmsProps> {

    public static final String TYPE = "SMS";

    public static final String NAME = "{MessageType." + TYPE + "}";

    private final SmsService smsService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<SmsProps> getPropsClass() {
        return SmsProps.class;
    }

    @Override
    public void validate(ObjectNode configs) {
        MessageRateLimitConfig.normalize(configs);
    }

    @Override
    public void send(ObjectNode jsonConfigProps, Message message) {
        try {
            SmsProps configProps = new SmsProps().fromJson(jsonConfigProps);
            SmsSendParams sendParams = new SmsSendParams().fromJson(message.params());

            SmsSendRequest req = new SmsSendRequest();
            req.setProvider(configProps.getProvider());
            req.setSecretId(configProps.getSecretId());
            req.setSecretKey(configProps.getSecretKey());
            req.setAppId(configProps.getAppId());
            req.setRegion(configProps.getRegion());
            req.setSignName(configProps.getSignName());
            req.setPhoneNumbers(sendParams.getPhoneNumbers());
            if (StringUtils.isNotEmpty(sendParams.getTemplateId())) {
                req.setTemplateId(sendParams.getTemplateId());
            } else {
                req.setTemplateId(configProps.getTemplateId());
            }
            req.setTemplateParams(sendParams.getTemplateVariables());
            smsService.sendSms(req);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
