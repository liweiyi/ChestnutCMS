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
package com.chestnut.member.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.chestnut.common.captcha.CaptchaCheckResult;
import com.chestnut.common.captcha.CaptchaData;
import com.chestnut.common.captcha.CaptchaService;
import com.chestnut.common.captcha.ICaptchaType;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.member.core.impl.MemberConfig_LoginCaptchaDuration;
import com.chestnut.member.core.impl.MemberConfig_LoginCaptchaEnable;
import com.chestnut.member.core.impl.MemberConfig_LoginCaptchaExpireSeconds;
import com.chestnut.member.core.impl.MemberConfig_LoginCaptchaType;
import com.chestnut.member.domain.Member;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.fixed.dict.MemberStatus;
import com.chestnut.member.service.IMemberConfigService;
import com.chestnut.member.service.IMemberService;
import com.chestnut.system.domain.dto.CheckCaptchaRequest;
import com.chestnut.system.domain.vo.GetCaptchaVO;
import com.chestnut.system.service.ISecurityConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;

/**
 * 会员登录校验方法
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberLoginService {

	private static final String CAPTCHA_SCENE_MEMBER_LOGIN = "memberlogin";
	
	private final IMemberService memberService;

	private final ISecurityConfigService securityConfigService;

	private final IMemberConfigService memberConfigService;

	private final CaptchaService captchaService;

	private final MemberConfig_LoginCaptchaEnable loginCaptchaEnable;

	private final MemberConfig_LoginCaptchaType loginCaptchaType;

	private final MemberConfig_LoginCaptchaExpireSeconds loginCaptchaExpireSeconds;

	private final MemberConfig_LoginCaptchaDuration loginCaptchaDuration;

	/**
	 * 查找正常用户，根据指定字段查询或查询username+email+phonenumber
	 */
	public Member findValidMember(String accountName, SFunction<Member, ?> findColumn) {
		LambdaQueryWrapper<Member> q = new LambdaQueryWrapper<>();
		if (Objects.nonNull(findColumn)) {
			q.eq(findColumn, accountName);
		} else {
			q.eq(Member::getUserName, accountName)
				.or().eq(Member::getEmail, accountName)
				.or().eq(Member::getPhoneNumber, accountName);
		}
		List<Member> matches = this.memberService.list(q);
		if (matches.size() != 1) {
			throw MemberErrorCode.MEMBER_NOT_EXISTS.exception();
		}
		Member member = matches.get(0);
		if (MemberStatus.isDisable(member.getStatus())) {
			throw MemberErrorCode.MEMBER_DISABLED.exception();
		} else if (member.isLocked()) {
			throw MemberErrorCode.MEMBER_LOCKED.exception();
		}
		return member;
	}

	/**
	 * 获取登录验证码
	 *
	 * @param token 验证码标识：用户名/邮箱/手机号
	 * @return 验证码结果
	 */
	public GetCaptchaVO getCaptcha(String token) {
		Boolean captchaEnabled = memberConfigService.getConfigValue(loginCaptchaEnable);
		if (!captchaEnabled) {
			throw CommonErrorCode.INVALID_REQUEST_ARG.exception("Login captcha is not enabled.");
		}
		String captchaTypeId = memberConfigService.getConfigValue(this.loginCaptchaType);
		ICaptchaType captchaType = captchaService.getCaptchaType(captchaTypeId);
		long expireSeconds = memberConfigService.getConfigValue(this.loginCaptchaExpireSeconds);
		int captchaDuration = memberConfigService.getConfigValue(this.loginCaptchaDuration);
		// 校验刷新间隔
		captchaType.checkLimit(CAPTCHA_SCENE_MEMBER_LOGIN, token, expireSeconds, captchaDuration);
		// 创建验证码
		CaptchaData captchaData = new CaptchaData(captchaTypeId, token);
		captchaData.setScene(CAPTCHA_SCENE_MEMBER_LOGIN);
		captchaData.setProperties(JacksonUtils.objectNode().put(ICaptchaType.EXPIRE_SECONDS_PROP_KEY, Long.valueOf(expireSeconds)));
		Object o = captchaType.create(captchaData);
		return new GetCaptchaVO(captchaDuration, o);
	}

	/**
	 * 校验验证码
	 */
	public CaptchaCheckResult checkCaptcha(CheckCaptchaRequest req) {
		String captchaTypeId = this.memberConfigService.getConfigValue(this.loginCaptchaType);
		ICaptchaType captchaType = captchaService.getCaptchaType(captchaTypeId);
		try {
			CaptchaData captchaData = new CaptchaData();
			captchaData.setType(captchaTypeId);
			captchaData.setScene(CAPTCHA_SCENE_MEMBER_LOGIN);
			captchaData.setToken(req.getToken());
			captchaData.setData(req.getData());
			Integer expireSeconds = memberConfigService.getConfigValue(this.loginCaptchaExpireSeconds);
			captchaData.setProperties(JacksonUtils.objectNode().put(ICaptchaType.EXPIRE_SECONDS_PROP_KEY, Long.valueOf(expireSeconds)));
			return captchaType.check(captchaData);
		} catch (Exception e) {
			log.error("Check member login captcha failed.", e);
			return CaptchaCheckResult.fail();
		}
	}

	/**
	 * 校验会员登录验证码是否已校验通过
	 *
	 * @param node 请求参数
	 * @param bindingMemberToken 当前登录的用户验证码token
	 */
	public void validateCaptcha(JsonNode node, String bindingMemberToken) {
		JsonNode captcha = node.get("captcha");
		if (Objects.nonNull(captcha) && !captcha.isNull()) {
			// 校验验证码参数是否与当前登录用户一致
			String token = captcha.required("token").asString();
			Assert.isTrue(Strings.CS.equals(token, bindingMemberToken), MemberErrorCode.INVALID_LOGIN_CAPTCHA::exception);
			// 校验当前验证码类型是否与配置一致
			String type = captcha.required("type").asString();
			String configCaptchaType = memberConfigService.getConfigValue(this.loginCaptchaType);
			Assert.isTrue(Strings.CS.equals(type, configCaptchaType), MemberErrorCode.INVALID_LOGIN_CAPTCHA::exception);

			CaptchaData captchaData = new CaptchaData();
			captchaData.setType(type);
			captchaData.setScene(CAPTCHA_SCENE_MEMBER_LOGIN);
			captchaData.setToken(token);
			captchaData.setData(captcha.path("data").asString(""));
			if (captcha.has("properties")) {
				captchaData.setProperties(captcha.get("properties").asObject());
			}
			ICaptchaType captchaType = captchaService.getCaptchaType(captchaData.getType());
			boolean validated = captchaType.isTokenValidated(captchaData);
			if (validated) {
				return;
			}
		}
		throw MemberErrorCode.INVALID_LOGIN_CAPTCHA.exception();
	}
}
