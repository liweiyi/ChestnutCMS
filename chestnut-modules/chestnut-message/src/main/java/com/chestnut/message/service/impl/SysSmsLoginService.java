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
package com.chestnut.message.service.impl;

import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ValidationUtils;
import com.chestnut.message.core.IMessageType;
import com.chestnut.message.core.MessageRateLimitConfig;
import com.chestnut.message.core.sms.SmsMessageType;
import com.chestnut.message.domain.CcMessageConfig;
import com.chestnut.message.domain.dto.SendSmsLoginCodeReq;
import com.chestnut.message.security.SmsLoginRedisStore;
import com.chestnut.message.security.SmsLoginType;
import com.chestnut.message.service.IMessageConfigService;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.vo.SmsLoginCodeVO;
import com.chestnut.system.exception.DistributedLockException;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.security.SysLoginService;
import com.chestnut.system.security.config.LoginSecurityConfigType;
import com.chestnut.system.service.ISecurityConfigService;
import com.chestnut.system.service.impl.LockService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;

/**
 * 后台手机号免密登录，独立于密码登录的图形验证码和第三方登录配置。
 */
@Service
@RequiredArgsConstructor
public class SysSmsLoginService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ISecurityConfigService securityConfigService;

    private final IMessageConfigService messageConfigService;

    private final MessageDeliveryService messageDeliveryService;

    private final SysLoginService sysLoginService;

    private final SmsLoginRedisStore redisStore;

    /** 仅用于发送后的锁所有权检查，锁的获取和释放由 LockService 管理。 */
    private final RedissonClient redissonClient;

    private final LockService lockService;

    /**
     * 构造限流处理工具类
     *
     * @param messageConfigId 消息配置ID
     * @return 统一限流工具类
     */
    private MessageRateLimitConfig messageRateLimitConfig(Long messageConfigId) {
        if (!IdUtils.validate(messageConfigId)) {
            throw SysErrorCode.SMS_LOGIN_CONFIG_INVALID.exception();
        }
        try {
            CcMessageConfig messageConfig = messageConfigService.getMessageConfig(messageConfigId);
            if (messageConfig == null || !SmsMessageType.TYPE.equals(messageConfig.getType())
                    || messageConfig.getConfigProps() == null
                    || messageConfig.getConfigProps().path("provider").asString("").isBlank()) {
                throw SysErrorCode.SMS_LOGIN_CONFIG_INVALID.exception();
            }
            return MessageRateLimitConfig.fromJson(messageConfig.getConfigProps());
        } catch (GlobalException e) {
            throw e;
        } catch (RuntimeException e) {
            throw SysErrorCode.SMS_LOGIN_CONFIG_INVALID.exception(e);
        }
    }

    /**
     * 发送短信验证码。同一手机号立即尝试获取发送锁，不等待其他发送完成；
     * 使用 LockService 默认的看门狗续期，覆盖供应商调用和验证码保存过程。
     * 锁竞争失败提示发送频繁，中断、丢锁和锁操作故障提示短信登录服务不可用。
     *
     * @param request 发送参数
     * @return 发送结果
     */
    public SmsLoginCodeVO sendCode(SendSmsLoginCodeReq request) {
        enabledContext();
        ValidationUtils.PhoneNumber phoneNumber = ValidationUtils.PhoneNumber.create(request.getPhoneCallingCode(), request.getPhoneNumber());
        phoneNumber.validate();

        String fullPhoneNumber = phoneNumber.getFullPhoneNumber();
        String lockKey = redisStore.getLockKey(fullPhoneNumber);
        try {
            return lockService.lockWithResult(lockKey, () -> sendCodeUnderLock(fullPhoneNumber, lockKey));
        } catch (DistributedLockException e) {
            if (e.getReason() == DistributedLockException.Reason.CONTENTION) {
                throw SysErrorCode.SMS_LOGIN_SEND_TOO_FREQUENT.exception(1);
            }
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception(e);
        }
    }

    /**
     * 仅在持有手机号发送锁的同步回调内调用；发送成功且锁、账号和配置仍有效时保存新码。
     */
    private SmsLoginCodeVO sendCodeUnderLock(String fullPhoneNumber, String lockKey) {
        LoginContext context = enabledContext();
        SysUser user = this.sysLoginService.findValidUser(fullPhoneNumber, SysUser::getPhoneNumber);
        Long userId = user.getUserId();
        MessageRateLimitConfig rateLimitConfig = messageRateLimitConfig(context.smsMessageConfigId());
        String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
        try {
            messageDeliveryService.sendMessage(new IMessageType.SendMessageReq(
                    fullPhoneNumber,
                    context.smsMessageConfigId(),
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
        LoginContext latest = enabledContext();
        if (!Objects.equals(context.configId(), latest.configId())
                || !Objects.equals(userId, user.getUserId())) {
            throw SysErrorCode.SMS_LOGIN_CODE_INVALID.exception();
        }
        if (!Objects.equals(context.smsMessageConfigId, latest.smsMessageConfigId())) {
            throw SysErrorCode.SMS_LOGIN_CONFIG_INVALID.exception();
        }
        redisStore.saveCode(fullPhoneNumber, userId, context.configId(), code);
        int retryDuration = rateLimitConfig.windows().stream()
                .filter(window -> window.maxCount() == 1)
                .mapToInt(MessageRateLimitConfig.RateLimitWindow::windowSeconds)
                .max().orElse(0);
        return new SmsLoginCodeVO(SmsLoginRedisStore.CODE_EXPIRE_SECONDS, retryDuration);
    }

    public LoginContext enabledContext() {
        SysSecurityConfig securityConfig = securityConfigService.getSecurityConfig();
        if (Objects.isNull(securityConfig) || Objects.isNull(securityConfig.getConfigs()) || !EnableOrDisable.ENABLE.equals(securityConfig.getStatus())) {
            throw SysErrorCode.SMS_LOGIN_DISABLED.exception();
        }
        JsonNode jsonNode = securityConfig.getConfigs().get(LoginSecurityConfigType.TYPE);
        if (Objects.isNull(jsonNode) || jsonNode.isNull()) {
            throw SysErrorCode.SMS_LOGIN_DISABLED.exception();
        }
        String enabled = jsonNode.path(SmsLoginType.SECURITY_CONFIG_SMS_LOGIN_ENABLE).asString(YesOrNo.NO);
        if (!YesOrNo.isYes(enabled)) {
            throw SysErrorCode.SMS_LOGIN_DISABLED.exception();
        }
        long smsMessageConfigId = jsonNode.path(SmsLoginType.SECURITY_CONFIG_SMS_MESSAGE_CONFIG_ID).asLong(0L);
        if (!IdUtils.validate(smsMessageConfigId)) {
            throw SysErrorCode.SMS_LOGIN_CONFIG_INVALID.exception();
        }
        return new LoginContext(securityConfig.getConfigId(), smsMessageConfigId);
    }

    public record LoginContext(Long configId, Long smsMessageConfigId) {
    }
}
