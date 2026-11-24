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
package com.chestnut.member;

import com.chestnut.member.exception.MemberSubscribeErrorCode;

import java.time.DateTimeException;
import java.time.Instant;

/**
 * 会员订阅工具类。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class MemberSubscribeUtils {

    /** 单次增加时长上限，约十年。 */
    public static final long MAX_INCREASE_SECONDS = 316224000L;

    /**
     * 以原结束点和本次操作时间的较大秒时间戳为基准，精确增加或减少秒数。
     * 返回整秒时间，超出Instant支持范围时抛出订阅时间溢出业务异常。
     */
    public static Instant getSubscribeExpireTime(Instant beforeExpireTime, Instant operationTime, long deltaSeconds) {
        long delta = Math.min(deltaSeconds, MAX_INCREASE_SECONDS);
        try {
            long baseSeconds = Math.max(beforeExpireTime.getEpochSecond(), operationTime.getEpochSecond());
            return Instant.ofEpochSecond(baseSeconds).plusSeconds(delta);
        } catch (DateTimeException | ArithmeticException e) {
            throw MemberSubscribeErrorCode.TIME_OVERFLOW.exception(e);
        }
    }
}
