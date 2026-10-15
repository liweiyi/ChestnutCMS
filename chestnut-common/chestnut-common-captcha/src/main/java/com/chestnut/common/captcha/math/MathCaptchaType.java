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
package com.chestnut.common.captcha.math;

import com.chestnut.common.captcha.*;
import com.chestnut.common.captcha.config.properties.CaptchaProperties;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.image.ImageUtils;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * MathCaptchaType
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
@Component(ICaptchaType.BEAN_PREFIX + MathCaptchaType.TYPE)
public class MathCaptchaType implements ICaptchaType {

    public final static String TYPE = "Math";
    private final static String IMAGE_TYPE_GIF = "gif";

    private final MathKaptchaProducer mathKaptchaProducer;

    private final ICaptchaStorage captchaStorage;

    protected final Validator validator;

    private final CaptchaProperties captchaProperties;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "{CAPTCHA.TYPE." + TYPE + "}";
    }

    @Override
    public ICaptchaStorage getStorage() {
        return this.captchaStorage;
    }

    @Override
    public Object create(CaptchaData captchaData) {
        Assert.notEmpty(captchaData.getToken(), CaptchaErrorCode.CAPTCHA_TOKEN_NOT_EMPTY::exception);
        try {
            String capText = mathKaptchaProducer.createText();
            String capStr = capText.substring(0, capText.lastIndexOf("@"));
            String code = capText.substring(capText.lastIndexOf("@") + 1);
            BufferedImage image = mathKaptchaProducer.createImage(capStr);
            String imageBase64Str = ImageUtils.imageToBase64(image, IMAGE_TYPE_GIF);
            // 写入缓存
            long expireSeconds = captchaProperties.getExpireSeconds();
            long configExpireSeconds = JacksonUtils.getAsLongElse(captchaData.getProperties(), EXPIRE_SECONDS_PROP_KEY, 0L);
            if (configExpireSeconds > 0) {
                expireSeconds = configExpireSeconds;
            }
            captchaStorage.set(this.getCacheKey(captchaData.getScene(), captchaData.getToken()), code, expireSeconds);
            // 返回客户端
            return new MathCaptchaVO(captchaData.getToken(), imageBase64Str);
        } catch (IOException e) {
            throw CaptchaErrorCode.GENERATE_CAPTCHA_FAILED.exception(TYPE);
        }
    }

    @Override
    public CaptchaCheckResult check(CaptchaData captchaData) {
        Assert.notEmpty(captchaData.getToken(), CaptchaErrorCode.CAPTCHA_TOKEN_NOT_EMPTY::exception);
        boolean checked = checkCaptcha(captchaData);
        if (!checked) {
            return CaptchaCheckResult.fail();
        }

        long expireSeconds = captchaProperties.getExpireSeconds();
        long configExpireSeconds = JacksonUtils.getAsLongElse(captchaData.getProperties(), EXPIRE_SECONDS_PROP_KEY, 0L);
        if (configExpireSeconds > 0) {
            expireSeconds = configExpireSeconds;
        }
        String successKey = this.getSuccessCacheKey(captchaData.getScene(), captchaData.getToken());
        this.captchaStorage.set(successKey, "1", expireSeconds);
        return CaptchaCheckResult.success();
    }

    private boolean checkCaptcha(CaptchaData captchaData) {
        String cacheKey = this.getCacheKey(captchaData.getScene(), captchaData.getToken());
        String cacheCode = this.captchaStorage.consume(cacheKey);
        return StringUtils.isNotBlank(cacheCode) && cacheCode.equals(captchaData.getData());
    }

    @Override
    public boolean isTokenValidated(CaptchaData captchaData) {
        String successCacheKey = this.getSuccessCacheKey(captchaData.getScene(), captchaData.getToken());
        if (this.captchaStorage.consume(successCacheKey) != null) {
            return true;
        }
        // 如果不存在尝试校验验证码
        return checkCaptcha(captchaData);
    }
}
