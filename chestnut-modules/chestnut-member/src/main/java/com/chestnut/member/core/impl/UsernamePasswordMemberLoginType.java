package com.chestnut.member.core.impl;

import cn.dev33.satoken.session.SaSession;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.enums.DeviceType;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.ValidationUtils;
import com.chestnut.member.core.IMemberLoginType;
import com.chestnut.member.domain.Member;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.fixed.dict.MemberStatus;
import com.chestnut.member.security.MemberLoginService;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.service.IMemberConfigService;
import com.chestnut.member.service.IMemberService;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.LoginLogType;
import com.chestnut.system.fixed.dict.SuccessOrFail;
import com.chestnut.system.service.ISecurityConfigService;
import com.chestnut.system.service.ISysLogininforService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;

/**
 * 会员用户名/邮箱+密码登录支持
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IMemberLoginType.BEAN_PREFIX + UsernamePasswordMemberLoginType.TYPE)
public class UsernamePasswordMemberLoginType implements IMemberLoginType {

    public static final String TYPE = "UsernamePassword";

    private final ISecurityConfigService securityConfigService;

    private final IMemberService memberService;

    private final MemberLoginService memberLoginService;

    private final IMemberConfigService memberConfigService;

    private final MemberConfig_LoginCaptchaEnable loginCaptchaEnable;

    private final ISysLogininforService logininfoService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String login(ObjectNode req, String ipAddress, String userAgent) {
        String username = StringUtils.trim(req.path("username").asString());
        ValidationUtils.notBlank(username);
        // 登录密码校验
        String password = req.path("password").asString();
        ValidationUtils.notBlank(password);
        // 验证码校验
        Boolean captchaEnabled = memberConfigService.getConfigValue(loginCaptchaEnable);
        if (captchaEnabled) {
            this.memberLoginService.validateCaptcha(req, username);
        }
        // 查找用户
        Member member = this.memberLoginService.findValidMember(username, null);
        // 密码校验
        if (!SecurityUtils.matches(password, member.getPassword())) {
            // 密码错误处理策略
            this.securityConfigService.processLoginPasswordError(member);
            if (member.isModified()) {
                this.memberService.updateById(member);
            }
            // 记录日志
            this.logininfoService.recordLogininfor(MemberUserType.TYPE, member.getMemberId(),
                    member.getUserName(), LoginLogType.LOGIN, SuccessOrFail.FAIL, "Invalid password.");
            throw SysErrorCode.PASSWORD_ERROR.exception();
        }
        this.securityConfigService.onLoginSuccess(member);
        // 记录用户最近登录时间和ip地址
        member.setLastLoginIp(ipAddress);
        member.setLastLoginTime(LocalDateTime.now());
        memberService.lambdaUpdate()
                .set(Member::getLastLoginIp, member.getLastLoginIp())
                .set(Member::getLastLoginTime, member.getLastLoginTime())
                .eq(Member::getMemberId, member.getMemberId())
                .update();
        // 生成token
        LoginUser loginUser = StpMemberUtil.createLoginUser(member, userAgent);
        StpMemberUtil.login(member.getUserId(), DeviceType.PC.value());
        loginUser.setToken(StpMemberUtil.getTokenValueByLoginId(member.getUserId()));
        StpMemberUtil.getTokenSession().set(SaSession.USER, loginUser);
        // 日志
        this.logininfoService.recordLogininfor(MemberUserType.TYPE, member.getUserId(),
                loginUser.getUsername(), LoginLogType.LOGIN, SuccessOrFail.SUCCESS, StringUtils.EMPTY);
        return StpMemberUtil.getTokenValue();
    }

    @Override
    public String register(ObjectNode req, String ipAddress, String userAgent) {
        String username = StringUtils.trim(req.path("username").asString());
        if (StringUtils.isEmpty(username)) {
            username = req.path("userName").asString(); // 历史兼容
        }
        ValidationUtils.validateUsername(username, 6, 30);
        String password = req.path("password").asString();
        ValidationUtils.notBlank(password);
        // 查找用户
        if (this.memberService.count(new LambdaQueryWrapper<Member>().eq(Member::getUserName, username)) > 0) {
            throw MemberErrorCode.ACCOUNT_EXISTS.exception();
        }

        Member member = new Member();
        member.setMemberId(IdUtils.getSnowflakeId());
        member.setUserName(username);
        member.setPassword(SecurityUtils.passwordEncode(password));
        member.setStatus(MemberStatus.ENABLE);
        // 记录用户最近登录时间和ip地址
        member.setLastLoginIp(ipAddress);
        member.setLastLoginTime(LocalDateTime.now());
        member.createBy(member.getUserName());
        // 密码规则校验
        this.securityConfigService.validPassword(member, password);
        this.memberService.save(member);

        // 生成token
        LoginUser loginUser = StpMemberUtil.createLoginUser(member, userAgent);
        StpMemberUtil.login(member.getUserId(), DeviceType.PC.value());
        loginUser.setToken(StpMemberUtil.getTokenValueByLoginId(member.getUserId()));
        StpMemberUtil.getTokenSession().set(SaSession.USER, loginUser);
        return StpMemberUtil.getTokenValue();
    }
}
