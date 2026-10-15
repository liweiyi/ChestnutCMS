package com.chestnut.system.security;

import com.chestnut.common.captcha.CaptchaData;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.ValidationUtils;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.dto.LoginBody;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.LoginLogType;
import com.chestnut.system.fixed.dict.SuccessOrFail;
import com.chestnut.system.service.ISecurityConfigService;
import com.chestnut.system.service.ISysLogininforService;
import com.chestnut.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Objects;

@RequiredArgsConstructor
@Component(ILoginType.BEAN_PREFIX + UsernamePasswordLoginType.TYPE)
public class UsernamePasswordLoginType implements ILoginType {

    public static final String TYPE = "UsernamePassword";

    private final ISecurityConfigService securityConfigService;

    private final ISysUserService userService;

    private final ISysLogininforService logininfoService;

    private final SysLoginService sysLoginService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public ObjectNode getLoginConfig(SysSecurityConfig securityConfig) {
        return JacksonUtils.objectNode().put("enabled", true);
    }

    private LoginBody parseLoginBody(JsonNode node) {
        LoginBody loginBody = new LoginBody();
        loginBody.setUsername(node.get("username").asString());
        loginBody.setPassword(node.get("password").asString());
        JsonNode captcha = node.get("captcha");
        if (Objects.nonNull(captcha) && !captcha.isNull()) {
            CaptchaData captchaData = new CaptchaData();
            captchaData.setType(captcha.required("type").asString());
            captchaData.setScene(SysLoginService.CAPTCHA_SCENE);
            captchaData.setToken(captcha.required("token").asString());
            captchaData.setData(captcha.path("data").asString(""));
            if (captcha.has("properties")) {
                captchaData.setProperties(captcha.get("properties").asObject());
            }
            loginBody.setCaptcha(captchaData);
        }
        return loginBody;
    }

    @Override
    public String doLogin(ObjectNode requestBody) {
        String username = requestBody.path("username").asString();
        ValidationUtils.notBlank(username);
        String password = requestBody.path("password").asString();
        ValidationUtils.notBlank(password);
        // 验证码校验
        LoginBody loginBody = this.parseLoginBody(requestBody);
        this.securityConfigService.validateLoginCaptcha(loginBody);
        // 根据用户名/手机号/邮箱查找用户，手机号地区默认+86
        SysUser user = this.sysLoginService.findValidUser(username, null);
        if (StringUtils.isEmpty(user.getPassword())) {
            throw SysErrorCode.PASSWORD_ERROR.exception(); // 第三方登录账号可能无密码
        }
        // 密码校验
        if (!SecurityUtils.matches(loginBody.getPassword(), user.getPassword())) {
            // 密码错误处理策略
            this.securityConfigService.processLoginPasswordError(user);
            if (user.isModified()) {
                this.userService.updateById(user);
            }
            // 记录日志
            this.logininfoService.recordLogininfor(AdminUserType.TYPE, user.getUserId(),
                    user.getUserName(), LoginLogType.LOGIN, SuccessOrFail.FAIL, "Invalid password.");
            throw SysErrorCode.PASSWORD_ERROR.exception();
        }
        this.securityConfigService.onLoginSuccess(user);
        return this.sysLoginService.userLogin(user);
    }
}
