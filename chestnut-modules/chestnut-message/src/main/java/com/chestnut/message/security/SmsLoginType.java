package com.chestnut.message.security;

import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.ValidationUtils;
import com.chestnut.message.service.impl.SysSmsLoginService;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.LoginLogType;
import com.chestnut.system.fixed.dict.SuccessOrFail;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.ILoginType;
import com.chestnut.system.security.SysLoginService;
import com.chestnut.system.security.config.LoginSecurityConfigType;
import com.chestnut.system.service.ISecurityConfigService;
import com.chestnut.system.service.ISysLogininforService;
import com.chestnut.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Objects;

@RequiredArgsConstructor
@Component(ILoginType.BEAN_PREFIX + SmsLoginType.TYPE)
public class SmsLoginType implements ILoginType {

    public static final String TYPE = "SMS";

    // 安全配置：是否开启短信登录
    public static final String SECURITY_CONFIG_SMS_LOGIN_ENABLE = "smsLoginEnable";
    // 安全配置：短信登录消息配置ID
    public static final String SECURITY_CONFIG_SMS_MESSAGE_CONFIG_ID = "smsMessageConfigId";
    // 登录参数：手机区号
    public static final String LOGIN_PARAM_PHONE_CALLING_CODE = "phoneCallingCode";
    // 登录参数：手机号
    public static final String LOGIN_PARAM_PHONE_NUMBER = "phoneNumber";
    // 登录参数：验证码
    public static final String LOGIN_PARAM_CAPTCHA_CODE = "code";

    private final SysSmsLoginService sysSmsLoginService;

    private final ISecurityConfigService securityConfigService;

    private final SysLoginService sysLoginService;

    private final ISysUserService userService;

    private final SmsLoginRedisStore redisStore;

    private final ISysLogininforService logininfoService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public ObjectNode getLoginConfig(SysSecurityConfig securityConfig) {
        ObjectNode jsonNodes = JacksonUtils.objectNode().put("enabled", false);
        if (Objects.isNull(securityConfig) || Objects.isNull(securityConfig.getConfigs())) {
            return jsonNodes;
        }
        JsonNode jsonNode = securityConfig.getConfigs().get(LoginSecurityConfigType.TYPE);
        if (Objects.isNull(jsonNode)) {
            return jsonNodes;
        }
        JsonNode smsLoginEnable = jsonNode.get(SECURITY_CONFIG_SMS_LOGIN_ENABLE);
        if (Objects.isNull(smsLoginEnable) || smsLoginEnable.isNull()) {
            return jsonNodes;
        }
        return jsonNodes.put("enabled", YesOrNo.isYes(smsLoginEnable.asString(YesOrNo.NO)));
    }

    @Override
    public String doLogin(ObjectNode objectNode) {
        SysSmsLoginService.LoginContext context = this.sysSmsLoginService.enabledContext();
        // 手机号校验
        String phoneCallingCode = StringUtils.trim(objectNode.path(LOGIN_PARAM_PHONE_CALLING_CODE).asString(null));
        String phoneNumber = StringUtils.trim(objectNode.path(LOGIN_PARAM_PHONE_NUMBER).asString(null));
        String fullPhoneNumber = phoneCallingCode + phoneNumber;
        ValidationUtils.validatePhoneNumber(fullPhoneNumber);
        // 短信验证码格式校验
        String code = objectNode.path(LOGIN_PARAM_CAPTCHA_CODE).asString();
        ValidationUtils.validateSmsCode(code, SysErrorCode.SMS_LOGIN_CODE_INVALID::exception);
        // 查找用户
        SysUser user = this.sysLoginService.findValidUser(fullPhoneNumber, SysUser::getPhoneNumber);
        // 校验验证码
        if (!redisStore.consumeCode(fullPhoneNumber, user.getUserId(), context.configId(), code)) {
            logininfoService.recordLogininfor(AdminUserType.TYPE, user.getUserId(), user.getUserName(),
                    LoginLogType.LOGIN, SuccessOrFail.FAIL, SysErrorCode.SMS_LOGIN_CODE_INVALID.name());
            throw SysErrorCode.SMS_LOGIN_CODE_INVALID.exception();
        }
        securityConfigService.onLoginSuccess(user);
        return sysLoginService.userLogin(user);
    }
}
