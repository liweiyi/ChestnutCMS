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
package com.chestnut.message.service.impl;

import com.chestnut.common.utils.Assert;
import com.chestnut.message.core.sms.ISmsProvider;
import com.chestnut.message.core.sms.SmsSendRequest;
import com.chestnut.message.exception.MessageErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * SmsService
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class SmsService {

    private final Map<String, ISmsProvider> smsProviderMap;

    public ISmsProvider getSmsProvider(String providerType) {
        ISmsProvider provider = smsProviderMap.get(ISmsProvider.BEAN_PREFIX + providerType);
        Assert.notNull(provider, () -> MessageErrorCode.UNSUPPORTED_SMS_PROVIDER.exception(providerType));
        return provider;
    }

    /**
     * 发送短信
     *
     * @param req
     */
    public void sendSms(SmsSendRequest req) throws Exception {
        ISmsProvider provider = this.getSmsProvider(req.getProvider());
        provider.send(req);
    }
}
