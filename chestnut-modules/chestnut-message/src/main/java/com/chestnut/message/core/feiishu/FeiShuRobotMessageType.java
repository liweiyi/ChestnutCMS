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
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * 飞书群机器人消息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component(IMessageType.BEAN_PREFIX + FeiShuRobotMessageType.TYPE)
public class FeiShuRobotMessageType implements IMessageType<FeiShuRobotProps> {

    public static final String TYPE = "FeiShuRobot";

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
    public Class<FeiShuRobotProps> getPropsClass() {
        return FeiShuRobotProps.class;
    }

    @Override
    public void send(ObjectNode jsonConfigProps, Message message) {
        try {
            FeiShuRobotProps configProps = new FeiShuRobotProps().fromJson(jsonConfigProps);
            ObjectNode jsonContent = JacksonUtils.parseObjectNode(message.content());
            if (StringUtils.isNotEmpty(configProps.getSecret())) {
                long timestamp = Instant.now().toEpochMilli() / 1000;
                jsonContent.put("timestamp", timestamp);
                jsonContent.put("sign", this.sign(configProps.getSecret(), timestamp));
            }
            String result = HttpUtils.postJSON(new URI(configProps.getWebHook()), jsonContent.toString());
            JsonNode jsonResult = JacksonUtils.parse(result);
            if (jsonResult.get("code").asInt(-1) != 0) {
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
        mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(new byte[]{});
        return new String(Base64.encodeBase64(signData), StandardCharsets.UTF_8);
    }
}
