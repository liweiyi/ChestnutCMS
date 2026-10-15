package com.chestnut.message.security;

import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.message.core.sms.SmsMessageType;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.service.IMessageConfigService;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.security.config.ISecurityConfigValidator;
import com.chestnut.system.security.config.LoginSecurityConfigType;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Objects;

/**
 * 短信登录安全配置校验
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class SmsSecurityConfigValidator implements ISecurityConfigValidator {

    private final IMessageConfigService messageConfigService;

    @Override
    public void validate(@Nonnull ObjectNode configs) {
        JsonNode jsonNode = configs.get(LoginSecurityConfigType.TYPE);
        if  (Objects.isNull(jsonNode)) {
            return;
        }
        // 开启短信登录，至少要配置正确的短信消息配置ID
        String smsLoginEnabled = jsonNode.path(SmsLoginType.SECURITY_CONFIG_SMS_LOGIN_ENABLE).asString(YesOrNo.NO);
        if (YesOrNo.YES.equals(smsLoginEnabled)) {
            // 是否合法消息ID
            JsonNode smsMessageConfigIdNode = jsonNode.get(SmsLoginType.SECURITY_CONFIG_SMS_MESSAGE_CONFIG_ID);
            if (Objects.isNull(smsMessageConfigIdNode) || smsMessageConfigIdNode.isNull()) {
                throw SysErrorCode.SMS_LOGIN_CONFIG_INVALID.exception();
            }
            long smsMessageConfigId = smsMessageConfigIdNode.asLong(0L);
            Assert.isTrue(IdUtils.validate(smsMessageConfigId), SysErrorCode.SMS_LOGIN_CONFIG_INVALID::exception);
            // 短信消息配置是否存在
            CcMessageConfig messageConfig = messageConfigService.getMessageConfig(smsMessageConfigId);
            if (Objects.isNull(messageConfig) || !SmsMessageType.TYPE.equals(messageConfig.getType())) {
                throw SysErrorCode.SMS_LOGIN_CONFIG_INVALID.exception();
            }
        }
    }
}
