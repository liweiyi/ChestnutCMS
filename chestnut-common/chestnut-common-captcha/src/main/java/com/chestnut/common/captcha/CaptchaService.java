/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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

import com.chestnut.common.utils.Assert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * CaptchaService
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final Map<String, ICaptchaType> captchaTypeMap;

    public ICaptchaType getCaptchaType(String type) {
        ICaptchaType captchaType = captchaTypeMap.get(ICaptchaType.BEAN_PREFIX + type);
        Assert.notNull(captchaType, () -> CaptchaErrorCode.UNSUPPORTED_CAPTCHA_TYPE.exception(type));
        return captchaType;
    }
}
