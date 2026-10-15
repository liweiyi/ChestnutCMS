package com.chestnut.member.core.impl;

import cn.dev33.satoken.session.SaSession;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.enums.DeviceType;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.ValidationUtils;
import com.chestnut.member.core.EmailMemberRegisterRedisStore;
import com.chestnut.member.core.IMemberLoginType;
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.vo.SendEmailRegisterCodeVO;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.fixed.dict.MemberStatus;
import com.chestnut.member.security.MemberLoginService;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.service.IMemberConfigService;
import com.chestnut.member.service.IMemberService;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.core.email.EmailMessageType;
import com.chestnut.message.service.impl.MessageDeliveryService;
import com.chestnut.system.exception.DistributedLockException;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.LoginLogType;
import com.chestnut.system.fixed.dict.SuccessOrFail;
import com.chestnut.system.service.ISecurityConfigService;
import com.chestnut.system.service.ISysLogininforService;
import com.chestnut.system.service.impl.LockService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 邮箱+密码登录支持
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IMemberLoginType.BEAN_PREFIX + EmailPasswordMemberLoginType.TYPE)
public class EmailPasswordMemberLoginType implements IMemberLoginType {

    public static final String TYPE = "EmailPassword";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ISecurityConfigService securityConfigService;

    private final MessageDeliveryService messageDeliveryService;

    private final IMemberService memberService;

    private final MemberLoginService memberLoginService;

    private final IMemberConfigService memberConfigService;

    private final MemberConfig_LoginCaptchaEnable loginCaptchaEnable;

    private final MemberConfig_RegisterEmailMessage registerMailMessageConfigId;

    private final LockService lockService;

    private final RedissonClient redissonClient;

    private final ISysLogininforService logininfoService;

    private final EmailMemberRegisterRedisStore emailMemberRegisterRedisStore;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String login(ObjectNode req, String ipAddress, String userAgent) {
        String email = StringUtils.trim(req.path("email").asString());
        ValidationUtils.validateEmail(email);
        // 登录密码校验
        String password = req.path("password").asString();
        ValidationUtils.notBlank(password);
        // 验证码校验
        Boolean captchaEnabled = memberConfigService.getConfigValue(loginCaptchaEnable);
        if (captchaEnabled) {
            this.memberLoginService.validateCaptcha(req, email);
        }
        // 查找用户
        Member member = this.memberLoginService.findValidMember(email, Member::getEmail);
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
        String email = StringUtils.trim(req.path("email").asString());
        ValidationUtils.validateEmail(email);
        String password = req.path("password").asString();
        ValidationUtils.notBlank(password);
        // 邮箱验证码校验
        String code = req.path("authCode").asString();
        ValidationUtils.notBlank(code);
        boolean codeVerified = this.emailMemberRegisterRedisStore.consumeCode(email, code);
        Assert.isTrue(codeVerified, MemberErrorCode.INVALID_LOGIN_CAPTCHA::exception);
        // 查找用户
        if (this.memberService.count(new LambdaQueryWrapper<Member>().eq(Member::getEmail, email)) > 0) {
            throw MemberErrorCode.ACCOUNT_EXISTS.exception();
        }

        Member member = new Member();
        member.setMemberId(IdUtils.getSnowflakeId());
        member.setUserName(member.getMemberId().toString());
        member.setEmail(email);
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

    /**
     * 发送邮箱注册验证码
     */
    public SendEmailRegisterCodeVO sendEmailCode(String email) {
        ValidationUtils.validateEmail(email);
        // 查找用户
        long count = this.memberService.count(new LambdaQueryWrapper<Member>()
                .eq(Member::getEmail, email));
        if (count > 0) {
            throw MemberErrorCode.EMAIL_CONFLICT.exception();
        }

        String lockKey = emailMemberRegisterRedisStore.getLockKey(email);
        try {
            return lockService.lockWithResult(lockKey, () -> sendCodeUnderLock(email, email, lockKey));
        } catch (DistributedLockException e) {
            if (e.getReason() == DistributedLockException.Reason.CONTENTION) {
                throw SysErrorCode.SMS_LOGIN_SEND_TOO_FREQUENT.exception(1);
            }
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception(e);
        }
    }

    private SendEmailRegisterCodeVO sendCodeUnderLock(String uuid, String email, String lockKey) {
        Long[] configIds = this.memberConfigService.getConfigValue(this.registerMailMessageConfigId);
        MemberConfig_RegisterEmailMessage.MemberEmailRegisterMessageConfig messageConfig = MemberConfig_RegisterEmailMessage.MemberEmailRegisterMessageConfig.decode(configIds);
        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        try {
            messageDeliveryService.sendMessage(new IMessageType.SendMessageReq(
                    email,
                    messageConfig.messageConfigId(),
                    messageConfig.messageTemplateConfigId(),
                    EmailMessageType.createSendParameters(code)
            ));
        } catch (GlobalException e) {
            throw e;
        } catch (RuntimeException e) {
            // 新验证码只在发送成功后保存；发送失败时旧码仍可使用。
            throw SysErrorCode.SMS_LOGIN_SEND_FAILED.exception(e);
        }
        // 必须在保存新码前确认所有权；仅依赖回调结束时的解锁检查无法阻止旧请求覆盖新码。
        boolean held;
        try {
            held = redissonClient.getLock(lockKey).isHeldByCurrentThread();
        } catch (RuntimeException e) {
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception(e);
        }
        if (!held) {
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception();
        }
        this.emailMemberRegisterRedisStore.saveCode(email, code);
        return new SendEmailRegisterCodeVO(email, EmailMemberRegisterRedisStore.CODE_EXPIRE_SECONDS, 60);
    }
}
