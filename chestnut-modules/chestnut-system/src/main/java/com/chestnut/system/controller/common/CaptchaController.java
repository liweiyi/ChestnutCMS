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
package com.chestnut.system.controller.common;

import com.chestnut.common.captcha.CaptchaCheckResult;
import com.chestnut.common.captcha.CaptchaData;
import com.chestnut.common.captcha.CaptchaService;
import com.chestnut.common.captcha.ICaptchaType;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.domain.dto.CheckCaptchaRequest;
import com.chestnut.system.fixed.config.SysCaptchaEnable;
import com.chestnut.system.fixed.config.SysCaptchaType;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 验证码操作处理
 */
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController extends BaseRestController {

	private final CaptchaService captchaService;

	@GetMapping("/get")
	public R<?> getCaptcha(@RequestParam @NotBlank String type) {
		ICaptchaType captchaType = captchaService.getCaptchaType(type);
		Object o = captchaType.create(new CaptchaData(type));
		return R.ok(o);
	}

	@IgnoreDemoMode
	@PostMapping("/check")
	public R<?> checkCaptcha(@RequestParam @NotBlank String type, @RequestBody CheckCaptchaRequest req) {
		ICaptchaType captchaType = captchaService.getCaptchaType(type);
        try {
			CaptchaData captchaData = new CaptchaData();
			captchaData.setType(type);
			captchaData.setToken(req.getToken());
			captchaData.setData(req.getData());
			CaptchaCheckResult result = captchaType.check(captchaData);
			return R.ok(result);
		} catch (Exception e) {
            return R.ok(CaptchaCheckResult.fail());
        }
	}

	@GetMapping("/config")
	public R<?> getLoginCaptchaConfig() {
		return R.ok(Map.of(
				"type", SysCaptchaType.getValue(),
				"enabled", SysCaptchaEnable.isEnable()
		));
	}
}
