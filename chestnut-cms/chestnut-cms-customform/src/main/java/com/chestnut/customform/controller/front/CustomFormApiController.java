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
package com.chestnut.customform.controller.front;

import com.chestnut.common.captcha.CaptchaType;
import com.chestnut.common.config.CaptchaConfig;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.customform.CmsCustomFormMetaModelType;
import com.chestnut.customform.CustomFormConsts;
import com.chestnut.customform.cache.CustomFormCaptchaMonitoredCache;
import com.chestnut.customform.domain.CmsCustomForm;
import com.chestnut.customform.exception.CustomFormErrorCode;
import com.chestnut.customform.fixed.config.CustomFormCaptchaExpireSeconds;
import com.chestnut.customform.service.ICustomFormApiService;
import com.chestnut.customform.service.ICustomFormService;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.config.properties.SysProperties;
import com.chestnut.system.domain.vo.ImageCaptchaVO;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.validator.LongId;
import com.google.code.kaptcha.Producer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

	private final CustomFormCaptchaMonitoredCache captchaCache;

	private final Map<String, Producer> captchaProducers;

	private final SysProperties properties;

	@GetMapping("/captchaImage")
	public R<?> getCaptchaImage(@RequestParam @LongId Long formId, HttpServletRequest request) {
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
		// 保存验证码信息
		String verifyKey = CustomFormCaptchaMonitoredCache.CACHE_PREFIX + uuid;

		String capStr;
		String code;
		BufferedImage image;

		Producer captchaProducer = captchaProducers.get(CaptchaConfig.BEAN_PREFIX + this.properties.getCaptchaType());
		Assert.notNull(captchaProducer, () -> SysErrorCode.CAPTCHA_CONFIG_ERR.exception(this.properties.getCaptchaType()));
		// 生成验证码
		String captchaType = properties.getCaptchaType();
		if (CaptchaType.MATH.equals(captchaType)) {
			String capText = captchaProducer.createText();
			capStr = capText.substring(0, capText.lastIndexOf("@"));
			code = capText.substring(capText.lastIndexOf("@") + 1);
			image = captchaProducer.createImage(capStr);
		} else if (CaptchaType.CHAR.equals(captchaType)) {
			capStr = code = captchaProducer.createText();
			image = captchaProducer.createImage(capStr);
		} else {
			throw new GlobalException("Unknown captcha type: " + captchaType);
		}

		Integer expireSeconds = CustomFormCaptchaExpireSeconds.getSeconds();
		captchaCache.setCache(verifyKey, code, expireSeconds, TimeUnit.SECONDS);

		try(FastByteArrayOutputStream os = new FastByteArrayOutputStream()) {
			ImageIO.write(image, "jpg", os);
			ImageCaptchaVO vo = ImageCaptchaVO.builder().captchaEnabled(true).uuid(uuid)
					.img(Base64.getEncoder().encodeToString(os.toByteArray())).build();
			return R.ok(vo);
		} catch (IOException e) {
			return R.fail(e.getMessage());
		}
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

	private void validateCaptcha(String code, String uuid) {
		Assert.notEmpty(code, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("captcha"));

		String cacheKey = CustomFormCaptchaMonitoredCache.CACHE_PREFIX + Objects.requireNonNullElse(uuid, StringUtils.EMPTY);
		String cacheValue = captchaCache.getCache(cacheKey);
		// 过期判断
		Assert.notNull(cacheValue, CommonErrorCode.CAPTCHA_EXPIRED::exception);
		// 未过期判断是否与输入验证码一致
		Assert.isTrue(StringUtils.equals(code, cacheValue), CommonErrorCode.INVALID_CAPTCHA::exception);
		// 移除缓存
		captchaCache.deleteCache(cacheKey);
	}
}
