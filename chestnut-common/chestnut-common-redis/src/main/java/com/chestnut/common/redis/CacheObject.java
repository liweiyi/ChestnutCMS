/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

/**
 * 缓存数据
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CacheObject<T> {

    private T data;

    private Long expiresIn;

    private TimeUnit timeUnit;

    public static <T> CacheObject<T> create(T data, Long expiresIn, TimeUnit timeUnit) {
        CacheObject<T> co = new CacheObject<>();
        co.data = data;
        co.expiresIn = expiresIn;
        co.timeUnit = timeUnit;
        return co;
    }
}
