package com.chestnut.feishu;

import com.chestnut.system.security.ILoginType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * FeiShuLoginType
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
@Component(ILoginType.BEAN_PREFIX + FeiShuLoginType.TYPE)
public class FeiShuLoginType implements ILoginType {

    public static final String TYPE = "feishu";
    public static final String TYPE_NAME = "{SYS.LOGIN_TYPE." + TYPE + "}";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return TYPE_NAME;
    }

    @Override
    public void dealSensitive(ObjectNode configProps) {
        configProps.put("appSecret", "******");
    }

    @Override
    public void updateConfigProps(ObjectNode oldProps, ObjectNode newProps) {
        String appSecret = newProps.get("appSecret").asText();
        if ("******".equals(appSecret)) {
            newProps.put("appSecret", oldProps.get("appSecret").asText());
        }
    }
}
