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
package com.chestnut.message.core.sms.tencent;

import com.chestnut.message.core.sms.ISmsProvider;
import com.chestnut.message.core.sms.SmsSendRequest;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component(ISmsProvider.BEAN_PREFIX + TencentSmsProvider.ID)
public class TencentSmsProvider implements ISmsProvider {

    public static final String ID = "tencent";

    public static final String NAME = "{SMS.PROVIDER." + ID + "}";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void send(SmsSendRequest req) throws Exception {
        if (req.getPhoneNumbers().size() > 1000) {
            throw new RuntimeException("PhoneNumber size exceed 1000.");
        }
        try {
            SmsClient client = this.createClient(req.getSecretId(), req.getSecretKey(), req.getRegion());
            SendSmsRequest request = new SendSmsRequest();
            String[] phoneNumbers = req.getPhoneNumbers().stream()
                    .map(TencentSmsProvider::normalizePhoneNumber).toArray(String[]::new);
            request.setPhoneNumberSet(phoneNumbers);
            request.setSmsSdkAppId(req.getAppId());
            request.setTemplateId(req.getTemplateId());
            request.setSignName(req.getSignName());
            request.setTemplateParamSet(req.getTemplateParams().values().toArray(new String[0]));
            SendSmsResponse res = client.SendSms(request);

            if (res == null || res.getSendStatusSet() == null || res.getSendStatusSet().length == 0) {
                throw new IllegalStateException("Tencent SMS response has no recipient status");
            }
            Set<String> pending = new HashSet<>(Arrays.asList(phoneNumbers));
            for (var status : res.getSendStatusSet()) {
                if (status == null || !"Ok".equals(status.getCode())
                        || !pending.remove(normalizePhoneNumber(status.getPhoneNumber()))) {
                    throw new IllegalStateException("Tencent SMS request was not accepted");
                }
            }
            if (!pending.isEmpty()) {
                throw new IllegalStateException("Tencent SMS response is missing recipient status");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("1[3-9][0-9]{9}")
                ? "+86" + phoneNumber : phoneNumber;
    }

    SmsClient createClient(String secretId, String secretKey, String region) {
        Credential cred = new Credential(secretId, secretKey);
//        HttpProfile httpProfile = new HttpProfile();
//        httpProfile.setEndpoint("sms.tencentcloudapi.com");
//        ClientProfile clientProfile = new ClientProfile();
//        clientProfile.setHttpProfile(httpProfile);
        // 实例化要请求产品的client对象,clientProfile是可选的
        return new SmsClient(cred, region);
    }
}
