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
package com.chestnut.member.core;

import com.chestnut.system.exception.SysErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 会员端短信登录的一次性验证码，按会员手机号独立保存和消费。
 * 调用方应统一传入包含拨号区号的完整手机号。
 */
@Component
@RequiredArgsConstructor
public class SmsMemberLoginRedisStore {

    public static final int CODE_EXPIRE_SECONDS = 600;

    public static final int MAX_CODE_FAILURES = 5;

    private static final String KEY_PREFIX = "cc:memberlogin:sms:";

    private static final DefaultRedisScript<Long> SAVE_CODE_SCRIPT = new DefaultRedisScript<>("""
            redis.call('DEL', KEYS[1])
            redis.call('HSET', KEYS[1], 'code', ARGV[1], 'failures', '0')
            redis.call('EXPIRE', KEYS[1], ARGV[2])
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> CONSUME_CODE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return 0
            end
            if redis.call('HGET', KEYS[1], 'code') == ARGV[1] then
                redis.call('DEL', KEYS[1])
                return 1
            end
            local failures = redis.call('HINCRBY', KEYS[1], 'failures', 1)
            if failures >= tonumber(ARGV[2]) then
                redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    public String getLockKey(Long memberId) {
        return prefix(memberId) + "send-lock";
    }

    /**
     * 调用方持有手机号发送锁，且供应商确认成功后，再原子保存新验证码及有效期。
     */
    public void saveCode(Long memberId, String code) {
        Long result;
        try {
            result = redisTemplate.execute(SAVE_CODE_SCRIPT, List.of(prefix(memberId) + "code"),
                    code, String.valueOf(CODE_EXPIRE_SECONDS));
        } catch (RuntimeException e) {
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception(e);
        }
        if (result == null || result != 1L) {
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception();
        }
    }

    /**
     * 原子校验并消费手机号对应的验证码，验证成功或累计错误达到上限后删除。
     */
    public boolean consumeCode(Long memberId, String code) {
        Long result;
        try {
            result = redisTemplate.execute(CONSUME_CODE_SCRIPT, List.of(prefix(memberId) + "code"),
                    code, String.valueOf(MAX_CODE_FAILURES));
        } catch (RuntimeException e) {
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception(e);
        }
        if (result == null) {
            throw SysErrorCode.SMS_LOGIN_UNAVAILABLE.exception();
        }
        return result == 1L;
    }

    private String prefix(Long memberId) {
        return KEY_PREFIX + memberId + ":";
    }
}
