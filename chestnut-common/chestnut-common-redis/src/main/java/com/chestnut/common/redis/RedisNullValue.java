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
package com.chestnut.common.redis;

import java.io.Serial;
import java.io.Serializable;

/**
 * Redis 空值占位符，用于防止缓存穿透。
 *
 * <p>设计约束：
 * <ul>
 *   <li>必须为非 final 类，Jackson {@code NON_FINAL} typing 才会写入 {@code @class} 字段以支持反序列化识别。</li>
 *   <li>单例，通过 {@code readResolve} 保证反序列化后仍返回同一实例。</li>
 *   <li>不要修改类名或移动包路径，否则旧缓存中的占位数据将无法正确识别。</li>
 * </ul>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class RedisNullValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final RedisNullValue INSTANCE = new RedisNullValue();

    private RedisNullValue() {}

    @Serial
    private Object readResolve() {
        return INSTANCE;
    }
}
