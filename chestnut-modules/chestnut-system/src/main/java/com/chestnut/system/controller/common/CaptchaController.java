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
package com.chestnut.system.controller.common;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.captcha.*;
import com.chestnut.common.domain.R;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.domain.dto.CheckCaptchaRequest;
import com.chestnut.system.domain.vo.GetCaptchaVO;
import com.chestnut.system.security.config.LoginSecurity;
import com.chestnut.system.security.config.LoginSecurityConfigType;
import com.chestnut.system.service.ISecurityConfigService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 验证码操作处理
 */
@XComment("{API.DOC.CAPTCHA.MODULE}")
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController extends BaseRestController {

	private final CaptchaService captchaService;

    private final ISecurityConfigService securityConfigService;

    private final List<ICaptchaType> captchaTypes;

    private final RedisCache redisCache;

    private final LoginSecurityConfigType loginSecurityConfigType;

    @XComment("{API.DOC.CAPTCHA.GET_OPTIONS}")
    @GetMapping("/options")
    public R<?> getCaptchaOptions() {
        return bindSelectOptions(captchaTypes, ICaptchaType::getType, ICaptchaType::getName);
    }

    @XComment("{API.DOC.CAPTCHA.GET_CAPTCHA}")
	@GetMapping("/get")
	public R<?> getCaptcha(@RequestParam @NotBlank String token) {
        SysSecurityConfig securityConfig = this.securityConfigService.getSecurityConfig();
        if (Objects.isNull(securityConfig)) {
            return R.fail("The captcha not enabled.");
        }
        LoginSecurity config = loginSecurityConfigType.getConfig(securityConfig.getConfigs());
        ICaptchaType captchaType = captchaService.getCaptchaType(config.getCaptchaType());
        // 校验刷新间隔
        if (config.getCaptchaDuration() > 0) {
            String cacheKey = captchaType.getCacheKey(token);
            long expire = this.redisCache.getExpire(cacheKey, TimeUnit.SECONDS);
            if (expire > 0 && config.getCaptchaExpires() - expire < config.getCaptchaDuration()) {
                throw CaptchaErrorCode.CAPTCHA_LIMIT.exception();
            }
        }
		CaptchaData captchaData = new CaptchaData(config.getCaptchaType(), token);
		captchaData.setProperties(JacksonUtils.objectNode()
				.put("messageConfigId", Objects.requireNonNullElse(config.getCaptchaMessageConfigId(), 0L))
				.put("messageTemplateId", Objects.requireNonNullElse(config.getCaptchaMessageTemplateId(), 0L)));
		Object o = captchaType.create(captchaData);
        return R.ok(new GetCaptchaVO(config.getCaptchaDuration(), o));
	}



    @XComment("{API.DOC.CAPTCHA.CHECK_CAPTCHA}")
	@IgnoreDemoMode
	@PostMapping("/check")
	public R<?> checkCaptcha(@RequestBody CheckCaptchaRequest req) {
        SysSecurityConfig securityConfig = this.securityConfigService.getSecurityConfig();
        if (Objects.isNull(securityConfig)) {
            return R.fail("The captcha not enabled.");
        }
        LoginSecurity config = loginSecurityConfigType.getConfig(securityConfig.getConfigs());
		ICaptchaType captchaType = captchaService.getCaptchaType(config.getCaptchaType());
        try {
			CaptchaData captchaData = new CaptchaData();
			captchaData.setType(config.getCaptchaType());
			captchaData.setToken(req.getToken());
			captchaData.setData(req.getData());
			CaptchaCheckResult result = captchaType.check(captchaData);
			return R.ok(result);
		} catch (Exception e) {
            return R.ok(CaptchaCheckResult.fail());
        }
	}
}
