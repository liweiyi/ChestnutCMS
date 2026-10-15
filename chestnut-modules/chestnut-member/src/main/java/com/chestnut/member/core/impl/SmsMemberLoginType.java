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
import com.chestnut.member.core.IMemberLoginType;
import com.chestnut.member.core.SmsMemberLoginRedisStore;
import com.chestnut.member.core.SmsMemberRegisterRedisStore;
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.vo.SendSmsRegisterCodeVO;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.fixed.dict.MemberStatus;
import com.chestnut.member.security.MemberLoginService;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.service.IMemberConfigService;
import com.chestnut.member.service.IMemberService;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.core.sms.SmsMessageType;
import com.chestnut.message.security.SmsLoginRedisStore;
import com.chestnut.message.service.impl.MessageDeliveryService;
import com.chestnut.system.domain.vo.SmsLoginCodeVO;
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
import java.util.List;
import java.util.Locale;

/**
 * 手机号免密登录支持
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IMemberLoginType.BEAN_PREFIX + SmsMemberLoginType.TYPE)
public class SmsMemberLoginType implements IMemberLoginType {

    public static final String TYPE = "SMS";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final LockService lockService;

    private final RedissonClient redissonClient;

    private final IMemberService memberService;

    private final IMemberConfigService memberConfigService;

    private final MemberLoginService memberLoginService;

    private final MemberConfig_LoginCaptchaEnable loginCaptchaEnable;

    private final ISysLogininforService logininfoService;

    private final SmsMemberLoginRedisStore loginRedisStore;

    private final SmsMemberRegisterRedisStore registerRedisStore;

    private final MessageDeliveryService messageDeliveryService;

    private final MemberConfig_SmsLoginMessageConfigId smsMessageConfigId;

    private final MemberConfig_SmsRegisterMessageConfigId smsRegisterMessageConfigId;

    private final ISecurityConfigService securityConfigService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String login(ObjectNode req, String ipAddress, String userAgent) {
        String phoneNumber = StringUtils.trim(req.path("phoneNumber").asString());
        ValidationUtils.validatePhoneNumber(phoneNumber);
        // 登录验证码校验
        String code = req.path("code").asString();
        ValidationUtils.validateSmsCode(code, MemberErrorCode.INVALID_LOGIN_CAPTCHA::exception);
        // 验证码校验
        Boolean captchaEnabled = memberConfigService.getConfigValue(loginCaptchaEnable);
        if (captchaEnabled) {
            this.memberLoginService.validateCaptcha(req, phoneNumber);
        }
        // 查找用户e
        Member member = this.memberLoginService.findValidMember(phoneNumber, Member::getPhoneNumber);
        // 校验短信验证码
        if (!loginRedisStore.consumeCode(member.getMemberId(), code)) {
            logininfoService.recordLogininfor(MemberUserType.TYPE, member.getUserId(), member.getUserName(),
                    LoginLogType.LOGIN, SuccessOrFail.FAIL, "INVALID_SMS_LOGIN_CODE");
            throw SysErrorCode.SMS_LOGIN_CODE_INVALID.exception();
        }
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
        String argPhoneCallingCode = req.path("phoneCallingCode").asString();
        String argPhoneNumber = StringUtils.trim(req.path("phoneNumber").asString());
        if (StringUtils.isEmpty(argPhoneNumber)) {
            argPhoneNumber = req.path("phonenumber").asString(); // 历史兼容
        }
        ValidationUtils.PhoneNumber phoneNumber = ValidationUtils.PhoneNumber.create(argPhoneCallingCode, argPhoneNumber);
        phoneNumber.validate();
        // 验证码校验
        String code = req.path("code").asString();
        if (StringUtils.isEmpty(code)) {
            code = req.path("authCode").asString(); // 历史兼容
        }
        ValidationUtils.notBlank(code);
        String fullPhoneNumber = phoneNumber.getFullPhoneNumber();
        boolean codeVerified = this.registerRedisStore.consumeCode(fullPhoneNumber, code);
        Assert.isTrue(codeVerified, MemberErrorCode.INVALID_LOGIN_CAPTCHA::exception);

        String password = req.path("password").asString();
        ValidationUtils.notBlank(password);
        // 查找用户
        if (this.memberService.count(new LambdaQueryWrapper<Member>().eq(Member::getPhoneNumber, fullPhoneNumber)) > 0) {
            throw MemberErrorCode.ACCOUNT_EXISTS.exception();
        }

        Member member = new Member();
        member.setMemberId(IdUtils.getSnowflakeId());
        member.setUserName(member.getMemberId().toString());
        member.setPhoneCallingCode(phoneNumber.phoneCallingCode());
        member.setPhoneNumber(fullPhoneNumber);
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
     * 发送短信登录验证码
     *
     * @param phoneNumber 带区号的完整手机号
     */
    public SmsLoginCodeVO sendLoginSmsCode(String phoneNumber) {
        ValidationUtils.validatePhoneNumber(phoneNumber);
        // 查找用户
        List<Member> matches = this.memberService.list(new LambdaQueryWrapper<Member>()
                .eq(Member::getPhoneNumber, phoneNumber));
        if (matches.size() != 1) {
            throw MemberErrorCode.MEMBER_NOT_EXISTS.exception();
        }
        Member member = matches.get(0);
        if (MemberStatus.isDisable(member.getStatus())) {
            throw MemberErrorCode.MEMBER_DISABLED.exception();
        } else if (member.isLocked()) {
            throw MemberErrorCode.MEMBER_LOCKED.exception();
        }

        String lockKey = loginRedisStore.getLockKey(member.getMemberId());
        try {
            return lockService.lockWithResult(lockKey, () -> sendCodeUnderLock(phoneNumber, lockKey, member.getMemberId()));
        } catch (DistributedLockException e) {
            if (e.getReason() == DistributedLockException.Reason.CONTENTION) {
                throw SysErrorCode.SMS_LOGIN_SEND_TOO_FREQUENT.exception(1);
            }
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception(e);
        }
    }

    private SmsLoginCodeVO sendCodeUnderLock(String phoneNumber, String lockKey, Long memberId) {
        Long messageConfigId = this.memberConfigService.getConfigValue(smsMessageConfigId);
        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        try {
            messageDeliveryService.sendMessage(new IMessageType.SendMessageReq(
                    phoneNumber,
                    messageConfigId,
                    null,
                    SmsMessageType.createSendParameters(code)
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
        loginRedisStore.saveCode(memberId, code);
        return new SmsLoginCodeVO(SmsLoginRedisStore.CODE_EXPIRE_SECONDS, 0);
    }

    /**
     * 发送手机短信注册验证码
     */
    public SendSmsRegisterCodeVO sendRegisterCode(String phoneNumber) {
        ValidationUtils.validatePhoneNumber(phoneNumber);
        // 查找用户
        long count = this.memberService.count(new LambdaQueryWrapper<Member>()
                .eq(Member::getPhoneNumber, phoneNumber));
        if (count > 0) {
            throw MemberErrorCode.PHONE_CONFLICT.exception();
        }

        String lockKey = this.registerRedisStore.getLockKey(phoneNumber);
        try {
            return lockService.lockWithResult(lockKey, () -> sendRegisterCodeUnderLock(phoneNumber, lockKey));
        } catch (DistributedLockException e) {
            if (e.getReason() == DistributedLockException.Reason.CONTENTION) {
                throw SysErrorCode.SMS_LOGIN_SEND_TOO_FREQUENT.exception(1);
            }
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception(e);
        }
    }

    private SendSmsRegisterCodeVO sendRegisterCodeUnderLock(String phoneNumber, String lockKey) {
        Long messageConfigId = this.memberConfigService.getConfigValue(this.smsRegisterMessageConfigId);
        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        try {
            messageDeliveryService.sendMessage(new IMessageType.SendMessageReq(
                    phoneNumber,
                    messageConfigId,
                    null,
                    SmsMessageType.createSendParameters(code)
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
        this.registerRedisStore.saveCode(phoneNumber, code);
        return new SendSmsRegisterCodeVO(phoneNumber, SmsMemberRegisterRedisStore.CODE_EXPIRE_SECONDS, 60);
    }
}
