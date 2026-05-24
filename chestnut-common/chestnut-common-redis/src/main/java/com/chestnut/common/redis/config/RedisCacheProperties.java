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
package com.chestnut.common.redis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ChestnutCMS Redis 缓存扩展配置。
 *
 * <pre>
 * chestnut:
 *   redis:
 *     ttlJitter: false               # 是否对缓存 TTL 施加随机抖动（防雪崩）。默认关闭。
 *     null-value-expire-seconds: 30  # 空值占位的过期时间（秒）。默认 30 秒。
 * </pre>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "chestnut.redis")
public class RedisCacheProperties {

    /**
     * 是否对缓存 TTL 施加随机抖动（上浮 ≤ 10%）以分散过期时间，防止缓存雪崩。
     * 仅作用于缓存语义路径（loadIfAbsent / setCacheObjectWithJitter / setCacheSet 等），
     * 不影响业务精确 TTL（expire / 验证码 / 令牌等）。
     * 默认 false。
     */
    private boolean ttlJitter = false;

    /**
     * 空值占位（{@code RedisNullValue}）的过期时间（秒）。
     * 在 {@code allowNullValue=true} 时，当数据源返回 null，写入占位以防止缓存穿透。
     * 默认 30 秒。
     */
    private long nullValueExpireSeconds = 30L;
}
