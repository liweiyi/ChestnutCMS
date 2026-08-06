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
package com.chestnut.message.core.dingtalk;

import com.chestnut.common.utils.HttpUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.exception.MessageErrorCode;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * 钉钉群机器人消息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component(IMessageType.BEAN_PREFIX + DingTalkRobotMessageType.TYPE)
public class DingTalkRobotMessageType implements IMessageType<DingTalkRobotConfigProps> {

    public static final String TYPE = "DingTalkRobot";

    public static final String NAME = "{MessageType." + TYPE + "}";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Class<DingTalkRobotConfigProps> getPropsClass() {
        return DingTalkRobotConfigProps.class;
    }

    @Override
    public void send(ObjectNode jsonConfigProps, Message message) {
        try {
            DingTalkRobotConfigProps configProps = new DingTalkRobotConfigProps().fromJson(jsonConfigProps);
            ObjectNode jsonContent = JacksonUtils.parseObjectNode(message.content());
            String url = configProps.getWebHook();
            if (StringUtils.isNotEmpty(configProps.getSecret())) {
                long timestamp = Instant.now().toEpochMilli();
                url = url + "&timestamp=" + timestamp + "&sign=" + this.sign(configProps.getSecret(), timestamp);
            }
            String result = HttpUtils.postJSON(new URI(url), jsonContent.toString());
            JsonNode jsonResult = JacksonUtils.parse(result);
            if (jsonResult.get("errcode").asInt(-1) != 0) {
                log.error("Send message fail: {}", result);
                throw MessageErrorCode.SEND_MESSAGE_FAIL.exception(result);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private String sign(String secret, long timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(new String(Base64.encodeBase64(signData), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}
