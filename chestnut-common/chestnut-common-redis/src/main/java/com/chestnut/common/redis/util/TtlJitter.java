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
package com.chestnut.common.redis.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * TTL 随机抖动工具，用于防止缓存雪崩。
 *
 * <p>对缓存类 TTL 在基础值上随机上浮 [0, 10%)，分散大批 key 的集中过期时间。
 * <b>仅</b>应用于缓存语义的 TTL（如 {@code loadIfAbsent}、{@code setCacheObjectWithJitter}）；
 * 业务精确 TTL（验证码、令牌等）不应使用本工具。
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public final class TtlJitter {

    private TtlJitter() {}

    /**
     * 对 TTL 应用随机上浮偏移，公式：{@code ttl + ttl * 0.1 * random}
     *
     * @param ttl 原始过期时间（秒），≤ 0 时直接返回原值
     * @return 加入抖动后的过期时间（秒）
     */
    public static long apply(long ttl) {
        if (ttl <= 0) {
            return ttl;
        }
        return ttl + (long) (ttl * 0.1d * ThreadLocalRandom.current().nextDouble());
    }
}
