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
package com.chestnut.customform.controller.front;

import com.chestnut.common.captcha.CaptchaData;
import com.chestnut.common.captcha.CaptchaService;
import com.chestnut.common.captcha.ICaptchaType;
import com.chestnut.common.captcha.math.MathCaptchaType;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.*;
import com.chestnut.customform.CmsCustomFormMetaModelType;
import com.chestnut.customform.CustomFormConsts;
import com.chestnut.customform.domain.CmsCustomForm;
import com.chestnut.customform.exception.CustomFormErrorCode;
import com.chestnut.customform.service.ICustomFormApiService;
import com.chestnut.customform.service.ICustomFormService;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.config.properties.SysProperties;
import com.chestnut.system.domain.vo.ImageCaptchaVO;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.validator.LongId;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * 自定义表单API控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/api/customform")
@RequiredArgsConstructor
public class CustomFormApiController extends BaseRestController {

	private final ICustomFormService customFormService;

	private final ICustomFormApiService customFormApiService;

	private final SysProperties properties;

	private final CaptchaService captchaService;

	@GetMapping("/captcha")
	public R<?> getCaptcha(@RequestParam @LongId Long formId, HttpServletRequest request) {
		CmsCustomForm form = this.customFormService.getById(formId);
		Assert.notNull(form, CustomFormErrorCode.FORM_NOT_FOUND::exception);
		// 是否需要验证码
		if (!YesOrNo.isYes(form.getNeedCaptcha())) {
			return R.ok(ImageCaptchaVO.builder().captchaEnabled(false).build());
		}
		// 是否登录
		if (YesOrNo.isYes(form.getNeedLogin()) && !StpMemberUtil.isLogin()) {
			throw CustomFormErrorCode.NOT_LOGIN.exception();
		}
		String uuid = CustomFormConsts.tryToGetUUID(request);
		Assert.notEmpty(uuid, CustomFormErrorCode.MISSING_UUID::exception);
		// TODO 目前固定使用数学计算验证码，待修改成自定义表单后台配置
		ICaptchaType captchaType = captchaService.getCaptchaType(MathCaptchaType.TYPE);
		CaptchaData captchaData = new CaptchaData(MathCaptchaType.TYPE);
		captchaData.setToken(uuid);
		Object captcha = captchaType.create(captchaData);
		return R.ok(captcha);
	}

	@IgnoreDemoMode
	@PostMapping("/submit")
	public R<?> submitForm(@RequestBody Map<String, Object> formData, HttpServletRequest request) throws IOException {
		Long formId = MapUtils.getLong(formData, "formId");
		if (!IdUtils.validate(formId)) {
			throw CommonErrorCode.INVALID_REQUEST_ARG.exception("formId");
		}
		CmsCustomForm form = this.customFormService.getById(formId);
		Assert.notNull(form, CustomFormErrorCode.FORM_NOT_FOUND::exception);
		// 判断登录
		if (YesOrNo.isYes(form.getNeedLogin()) && !StpMemberUtil.isLogin()) {
			throw CustomFormErrorCode.NOT_LOGIN.exception();
		}
		// 获取用户标识
		String uuid = CustomFormConsts.tryToGetUUID(request);
		Assert.notEmpty(uuid, CustomFormErrorCode.MISSING_UUID::exception);
		// 验证码
		if (YesOrNo.isYes(form.getNeedCaptcha())) {
			String captcha = MapUtils.getString(formData, "captcha", StringUtils.EMPTY);
			this.validateCaptcha(captcha, uuid);
		}

		String clientIp = ServletUtils.getIpAddr(request);
		formData.put(CmsCustomFormMetaModelType.FIELD_DATA_ID.getCode(), IdUtils.getSnowflakeId());
		formData.put(CmsCustomFormMetaModelType.FIELD_MODEL_ID.getCode(), form.getFormId());
		formData.put(CmsCustomFormMetaModelType.FIELD_SITE_ID.getCode(), form.getSiteId());
		formData.put(CmsCustomFormMetaModelType.FIELD_CLIENT_IP.getCode(), clientIp);
		formData.put(CmsCustomFormMetaModelType.FIELD_UUID.getCode(), uuid);
		formData.put(CmsCustomFormMetaModelType.FIELD_CREATE_TIME.getCode(), LocalDateTime.now());
		if (YesOrNo.isYes(form.getNeedLogin())) {
			formData.put(CmsCustomFormMetaModelType.FIELD_UID.getCode(), StpMemberUtil.getLoginUser().getUserId());
		}
		customFormApiService.submit(form, formData);
		return R.ok();
	}

	private void validateCaptcha(String captchaJson, String uuid) {
		Assert.notEmpty(captchaJson, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("captcha"));
		CaptchaData captchaData = JacksonUtils.from(captchaJson, CaptchaData.class);
		captchaData.setToken(uuid);
		ICaptchaType captchaType = captchaService.getCaptchaType(MathCaptchaType.TYPE);
		if (!captchaType.isTokenValidated(captchaData)) {
			throw CommonErrorCode.INVALID_CAPTCHA.exception();
		}
	}
}
