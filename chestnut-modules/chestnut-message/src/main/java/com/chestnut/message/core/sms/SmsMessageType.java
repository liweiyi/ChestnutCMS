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

import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.ValidationUtils;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.exception.MessageErrorCode;
import com.chestnut.message.service.impl.SmsService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;


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

    public static ObjectNode createSendParameters(String code) {
        return JacksonUtils.objectNode().put("Code", code);
    }

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
    public boolean supportedTemplate() {
        return false;
    }

    @Override
    public boolean requiresReceiver() {
        return true;
    }

    @Override
    public void checkSendRequest(SendMessageReq req) {
        IMessageType.super.checkSendRequest(req);
        ValidationUtils.validatePhoneNumber(req.getReceiver());
    }

    @Override
    public ObjectNode buildSendParams(@Nonnull CcMessageConfig messageConfig, ObjectNode variables) {
        String templateId = messageConfig.getConfigProps() == null ? ""
                : messageConfig.getConfigProps().path("templateId").asString("");
        Assert.isTrue(!templateId.isBlank(), MessageErrorCode.TEMPLATE_ID_NOT_EMPTY::exception);
        ObjectNode params = JacksonUtils.objectNode();
        params.put("templateId", templateId);
        params.set("templateVariables", variables);
        return params;
    }

    @Override
    public void send(Message message) {
        try {
            SmsProps configProps = new SmsProps().fromJson(message.configProps());
            SmsSendParams sendParams = new SmsSendParams().fromJson(message.configProps());

            SmsSendRequest req = new SmsSendRequest();
            req.setProvider(configProps.getProvider());
            req.setSecretId(configProps.getSecretId());
            req.setSecretKey(configProps.getSecretKey());
            req.setAppId(configProps.getAppId());
            req.setRegion(configProps.getRegion());
            req.setSignName(configProps.getSignName());
            req.setPhoneNumber(message.receiver());
            if (StringUtils.isNotEmpty(sendParams.getTemplateId())) {
                req.setTemplateId(sendParams.getTemplateId());
            } else {
                req.setTemplateId(configProps.getTemplateId());
            }
            req.setTemplateParams(sendParams.getTemplateVariables());
            smsService.sendSms(req);
        } catch (Exception e) {
            if (e instanceof GlobalException globalException) {
                throw globalException;
            }
            throw MessageErrorCode.SEND_MESSAGE_FAIL.exception(e, e.getMessage());
        }
    }
}
