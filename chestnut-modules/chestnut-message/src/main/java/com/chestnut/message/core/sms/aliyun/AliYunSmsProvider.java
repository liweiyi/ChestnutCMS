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
package com.chestnut.message.core.sms.aliyun;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.message.core.sms.ISmsProvider;
import com.chestnut.message.core.sms.SmsSendRequest;
import org.springframework.stereotype.Component;

@Component(ISmsProvider.BEAN_PREFIX + AliYunSmsProvider.ID)
public class AliYunSmsProvider implements ISmsProvider {

    public static final String ID = "aliyun";

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
        Client client = this.createClient(req.getSecretId(), req.getSecretKey(), req.getRegion());
        RuntimeOptions runtimeOptions = new RuntimeOptions();
        runtimeOptions.setConnectTimeout(5000);  // 链接超时
        runtimeOptions.setReadTimeout(10000); // 读超时
        runtimeOptions.setAutoretry(true); // 是否自动重试
        runtimeOptions.setMaxAttempts(3); // 最大重试次数

        SendSmsRequest sendRequest = new SendSmsRequest()
                .setPhoneNumbers(String.join(",", req.getPhoneNumbers()))
                .setSignName(req.getSignName())
                .setTemplateCode(req.getTemplateId())
                .setTemplateParam(JacksonUtils.to(req.getTemplateParams()));
        SendSmsResponse res = client.sendSmsWithOptions(sendRequest, runtimeOptions);
        if (res == null || res.getBody() == null || !"OK".equals(res.getBody().getCode())) {
            throw new IllegalStateException("Aliyun SMS request was not accepted");
        }
    }

    Client createClient(String accessKeyId, String accessKeySecret, String region) throws Exception {
        com.aliyun.credentials.Client credential = new com.aliyun.credentials.Client();
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config().setCredential(credential);
        // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
        config.setRegionId(StringUtils.isEmpty(region) ? "cn-hangzhou" : region);
        config.setEndpoint(RegionEndpoint.region2endpoint(config.getRegionId())); // 国内固定，国际再说
        config.setAccessKeyId(accessKeyId);
        config.setAccessKeySecret(accessKeySecret);
        return new Client(config);
    }

    enum RegionEndpoint {
        cn_qingdao("青岛", "cn-qingdao", "dysmsapi.aliyuncs.com"),
        cn_zhangjiakou("张家口", "cn-zhangjiakou", "dysmsapi.aliyuncs.com"),
        cn_huhehaote("呼和浩特", "cn-huhehaote", "dysmsapi.aliyuncs.com"),
        cn_hangzhou("杭州", "cn-hangzhou", "dysmsapi.aliyuncs.com"),
        cn_shenzhen("深圳", "cn-shenzhen", "dysmsapi.aliyuncs.com"),
        cn_chengdu("成都", "cn-chengdu", "dysmsapi.aliyuncs.com"),
        ap_southeast_1("新加坡", "ap-southeast-1", "dysmsapi.ap-southeast-1.aliyuncs.com"),
        ap_southeast_5("印度尼西亚（雅加达）", "ap-southeast-5", "dysmsapi.ap-southeast-5.aliyuncs.com"),
        cn_hongkong("中国香港", "cn-hongkong", "dysmsapi.aliyuncs.com"),
        frankfurt_germany("德国（法兰克福）", "eu-central-1", "dysmsapi.eu-central-1.aliyuncs.com"),
        cn_beijing_finance_1("华北2 金融云（邀测）", "cn-beijing-finance-1", "dysmsapi.aliyuncs.com"),
        cn_hangzhou_finance("华东1 金融云", "cn-hangzhou-finance", "dysmsapi.aliyuncs.com"),
        cn_shanghai_finance_1("华东2 金融云", "cn-shanghai-finance-1", "dysmsapi.aliyuncs.com"),
        cn_shenzhen_finance_1("华南1 金融云", "cn-shenzhen-finance-1", "dysmsapi.aliyuncs.com"),
        cn_north_2_gov_1("北京政务云", "cn-north-2-gov-1", "dysmsapi.aliyuncs.com");

        private final String label;
        private final String region;
        private final String endpoint;

        RegionEndpoint(String label, String region, String endpoint) {
            this.label = label;
            this.region = region;
            this.endpoint = endpoint;
        }

        public String label() {
            return this.label;
        }

        public String region() {
            return this.region;
        }

        public String endpoint() {
            return this.endpoint;
        }

        public static String region2endpoint(String region) {
            for (RegionEndpoint re : RegionEndpoint.values()) {
                if (re.region().equals(region)) {
                    return re.endpoint();
                }
            }
            return RegionEndpoint.cn_hangzhou.endpoint();
        }
     }
}
