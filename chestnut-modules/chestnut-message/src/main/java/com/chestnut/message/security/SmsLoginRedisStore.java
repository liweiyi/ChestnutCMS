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
package com.chestnut.message.security;

import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.exception.SysErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * 后台短信登录的一次性验证码，与手机号、后台账号和安全配置绑定。
 */
@Component
@RequiredArgsConstructor
public class SmsLoginRedisStore {

    public static final int CODE_EXPIRE_SECONDS = 600;

    public static final int MAX_CODE_FAILURES = 5;

    private static final String KEY_PREFIX = "sys:login:sms:";

    private static final DefaultRedisScript<Long> SAVE_CODE_SCRIPT = new DefaultRedisScript<>("""
            redis.call('DEL', KEYS[1])
            redis.call('HSET', KEYS[1], 'userId', ARGV[1], 'configId', ARGV[2],
                       'code', ARGV[3], 'failures', '0')
            redis.call('EXPIRE', KEYS[1], ARGV[4])
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> CONSUME_CODE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return 0
            end
            if redis.call('HGET', KEYS[1], 'userId') ~= ARGV[1]
               or redis.call('HGET', KEYS[1], 'configId') ~= ARGV[2] then
                redis.call('DEL', KEYS[1])
                return 0
            end
            if redis.call('HGET', KEYS[1], 'code') == ARGV[3] then
                redis.call('DEL', KEYS[1])
                return 1
            end
            local failures = redis.call('HINCRBY', KEYS[1], 'failures', 1)
            if failures >= tonumber(ARGV[4]) then
                redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public String getLockKey(String phoneNumber) {
        return prefix(phoneNumber) + "send-lock";
    }

    /**
     * 调用方持有手机号发送锁，且供应商确认成功后，再原子保存新验证码及有效期。
     */
    public void saveCode(String phoneNumber, Long userId, Long securityConfigId, String code) {
        Long result;
        try {
            result = redisTemplate.execute(SAVE_CODE_SCRIPT, List.of(prefix(phoneNumber) + "code"),
                    userId.toString(), securityConfigId.toString(),
                    code, String.valueOf(CODE_EXPIRE_SECONDS));
        } catch (RuntimeException e) {
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception(e);
        }
        if (result == null || result != 1L) {
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception();
        }
    }

    public boolean consumeCode(String phoneNumber, Long userId, Long securityConfigId, String code) {
        Long result;
        try {
            result = redisTemplate.execute(CONSUME_CODE_SCRIPT, List.of(prefix(phoneNumber) + "code"),
                    userId.toString(), securityConfigId.toString(), code, String.valueOf(MAX_CODE_FAILURES));
        } catch (RuntimeException e) {
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception(e);
        }
        if (result == null) {
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception();
        }
        return result == 1L;
    }

    private String prefix(String phoneNumber) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(StringUtils.trim(phoneNumber).getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + "{" + HexFormat.of().formatHex(digest) + "}:";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
