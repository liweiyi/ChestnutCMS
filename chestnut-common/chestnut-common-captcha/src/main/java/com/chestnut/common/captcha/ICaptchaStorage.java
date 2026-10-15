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
package com.chestnut.common.captcha;

import java.util.concurrent.TimeUnit;

/**
 * 验证码存储策略
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ICaptchaStorage {

    /**
     * 原子读取并删除验证码或校验成功标记，同一个值最多返回给一个请求。
     * 不存在或已过期时返回 null；实现不能拆分为读取和删除两个操作。
     */
    String consume(String key);

    /**
     * 保存验证码缓存，指定过期时间
     *
     * @param key 存储KEY
     * @param value 验证码值
     * @param expireSeconds 过期时间，单位：秒
     */
    void set(String key, String value, long expireSeconds);

    /**
     * 删除验证码记录
     */
    void delete(String key);

    default long getExpires(String key, TimeUnit timeUnit) {
        return -1;
    }
}
