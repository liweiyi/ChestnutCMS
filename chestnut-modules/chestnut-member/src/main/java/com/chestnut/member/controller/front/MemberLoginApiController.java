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
package com.chestnut.member.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.captcha.CaptchaCheckResult;
import com.chestnut.common.domain.R;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.member.core.IMemberLoginType;
import com.chestnut.member.core.impl.EmailPasswordMemberLoginType;
import com.chestnut.member.core.impl.SmsMemberLoginType;
import com.chestnut.member.core.impl.UsernamePasswordMemberLoginType;
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.vo.MemberCache;
import com.chestnut.member.domain.vo.MemberMenuVO;
import com.chestnut.member.domain.vo.SendEmailRegisterCodeVO;
import com.chestnut.member.domain.vo.SendSmsRegisterCodeVO;
import com.chestnut.member.exception.MemberErrorCode;
import com.chestnut.member.fixed.dict.MemberStatus;
import com.chestnut.member.security.MemberLoginService;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.service.IMemberService;
import com.chestnut.member.service.IMemberStatDataService;
import com.chestnut.member.util.MemberUtils;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.domain.dto.CheckCaptchaRequest;
import com.chestnut.system.domain.vo.GetCaptchaVO;
import com.chestnut.system.domain.vo.SmsLoginCodeVO;
import com.chestnut.system.fixed.dict.LoginLogType;
import com.chestnut.system.fixed.dict.SuccessOrFail;
import com.chestnut.system.service.ISysLogininforService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@XComment("{API.DOC.MEMBER.LOGIN_API_MODULE}")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member")
public class MemberLoginApiController extends BaseRestController {

	private final IMemberService memberService;

	private final MemberLoginService memberLoginService;

	private final IMemberStatDataService memberStatDataService;

	private final ISysLogininforService logininforService;

	private final EmailPasswordMemberLoginType emailPasswordMemberLoginType;

	private final SmsMemberLoginType smsMemberLoginType;

	private final Map<String, IMemberLoginType> loginTypeMap;

	private final RedisCache redisCache;

	private static final String SMS_CODE_CACHE_PREFIX = "member:sms_code:";

	@XComment("{API.DOC.MEMBER.CHECK_LOGIN}")
	@GetMapping("/is_login")
	public R<?> checkLogin(@RequestParam(required = false, defaultValue = "false") @XComment("{API.DOC.MEMBER.PREVIEW}") Boolean preview) {
		boolean login = StpMemberUtil.isLogin();
		if (!login) {
			return R.fail("Not Login");
		}
		MemberCache memberCache = memberStatDataService.getMemberCache(StpMemberUtil.getLoginIdAsLong());
		if (StringUtils.isNotEmpty(memberCache.getAvatar())) {
			memberCache.setAvatar(MemberUtils.getMemberResourcePrefix(preview) + memberCache.getAvatar());
		}
		if (StringUtils.isNotEmpty(memberCache.getCover())) {
			memberCache.setAvatar(MemberUtils.getMemberResourcePrefix(preview) + memberCache.getCover());
		}
		if (memberCache.getMenus().isEmpty()) {
			memberCache.getMenus().add(new MemberMenuVO("账号信息", "account/setting"));
			memberCache.getMenus().add(new MemberMenuVO("修改密码", "account/password"));
			memberCache.getMenus().add(new MemberMenuVO("文章投稿", "account/contribute"));
		}
		return R.ok(memberCache);
	}

	/**
	 * 获取登录验证码
	 */
	@XComment("{API.DOC.MEMBER.GET_LOGIN_CAPTCHA}")
	@GetMapping("/get_captcha")
	public R<?> getCaptcha(@RequestParam @NotBlank String token) {
		// 先校验用户名是否存在
		List<Member> matches = this.memberService.list(new LambdaQueryWrapper<Member>()
				.select(Member::getPhoneNumber, Member::getStatus, Member::getLockEndTime)
				.eq(Member::getUserName, token)
				.or().eq(Member::getEmail, token)
				.or().eq(Member::getPhoneNumber, token));
		if (matches.size() != 1) {
			throw MemberErrorCode.MEMBER_NOT_EXISTS.exception();
		}
		Member member = matches.get(0);
		if (MemberStatus.isDisable(member.getStatus())) {
			throw MemberErrorCode.MEMBER_DISABLED.exception();
		} else if (member.isLocked()) {
			throw MemberErrorCode.MEMBER_LOCKED.exception();
		}
		GetCaptchaVO captcha = this.memberLoginService.getCaptcha(token);
		return R.ok(captcha);
	}

	/**
	 * 校验登录验证码
	 */
	@XComment("{API.DOC.CAPTCHA.CHECK_LOGIN_CAPTCHA}")
	@IgnoreDemoMode
	@PostMapping("/check_captcha")
	public R<?> checkCaptcha(@RequestBody CheckCaptchaRequest req) {
		CaptchaCheckResult captchaCheckResult = this.memberLoginService.checkCaptcha(req);
		return R.ok(captchaCheckResult);
	}

	/**
	 * 获取手机号免密登录验证码
	 */
	@XComment("{API.DOC.MEMBER.GET_SMS_LOGIN_CODE}")
	@GetMapping("/get_sms_login_code")
	public R<?> getSMSLoginCode(@RequestParam String phoneNumber) {
		SmsLoginCodeVO vo = smsMemberLoginType.sendLoginSmsCode(phoneNumber);
		return R.ok(vo);
	}

	/**
	 * 获取手机号註冊短信验证码
	 */
	@XComment("{API.DOC.MEMBER.GET_SMS_LOGIN_CODE}")
	@GetMapping("/get_sms_register_code")
	public R<?> getSMSRegisterCode(@RequestParam String phoneNumber) {
		SendSmsRegisterCodeVO vo = smsMemberLoginType.sendRegisterCode(phoneNumber);
		return R.ok(vo);
	}

	@XComment("{API.DOC.MEMBER.LOGIN}")
	@IgnoreDemoMode
	@PostMapping("/login")
	public R<?> login(@RequestParam(required = false) String type, @RequestBody ObjectNode req) {
		String ip = ServletUtils.getIpAddr(ServletUtils.getRequest());
		String userAgent = ServletUtils.getUserAgent();
		// 默认用户名密码登录兼容历史
		String loginTypeId = Objects.requireNonNullElse(type, UsernamePasswordMemberLoginType.TYPE);
		IMemberLoginType loginType = this.loginTypeMap.get(IMemberLoginType.BEAN_PREFIX + loginTypeId);
		Assert.notNull(loginType, MemberErrorCode.UNSUPPORTED_LOGIN_TYPE::exception);
		String token = loginType.login(req, ip, userAgent);
		return R.ok(token);
	}

	@XComment("{API.DOC.MEMBER.REGISTER}")
	@IgnoreDemoMode
	@PostMapping("/register")
	public R<?> resgiter(@RequestBody ObjectNode req, HttpServletRequest request) {
		String ip = ServletUtils.getIpAddr(request);
		String userAgent = ServletUtils.getUserAgent(request);
		String loginTypeId = mappingOldType(req.path("type").asString());
		IMemberLoginType loginType = this.loginTypeMap.get(IMemberLoginType.BEAN_PREFIX + loginTypeId);
		Assert.notNull(loginType, MemberErrorCode.UNSUPPORTED_LOGIN_TYPE::exception);
		String token = loginType.register(req, ip, userAgent);
		return R.ok(token);
	}

	private String mappingOldType(String oldType) {
		// 兼容历史
		return switch (oldType) {
			case "phone" -> SmsMemberLoginType.TYPE;
			case "email" -> EmailPasswordMemberLoginType.TYPE;
			case "username" -> UsernamePasswordMemberLoginType.TYPE;
			default -> oldType;
		};
	}


	@XComment("{API.DOC.MEMBER.LOGOUT}")
	@IgnoreDemoMode
	@PostMapping("/logout")
	public R<?> logout() {
		try {
			if (StpMemberUtil.isLogin()) {
				LoginUser loginUser = StpMemberUtil.getLoginUser();
				StpMemberUtil.logout();
				this.logininforService.recordLogininfor(loginUser.getUserType(),
						loginUser.getUserId(), loginUser.getUsername(), LoginLogType.LOGOUT, SuccessOrFail.SUCCESS,
						StringUtils.EMPTY);
			}
		} catch (Exception e) {
			log.warn("Logout failed", e);
		}
		return R.ok();
	}

	@XComment("{API.DOC.MEMBER.SEND_REGISTER_SMS_CODE}")
	@IgnoreDemoMode
	@GetMapping("/register_mail_code")
	public R<?> sendEmailRegisterCode(@RequestParam @Email @XComment("{API.DOC.MEMBER.EMAIL}") String email) {
		SendEmailRegisterCodeVO vo = emailPasswordMemberLoginType.sendEmailCode(email);
		return R.ok(vo);
	}

	// 兼容历史接口
	@Deprecated(forRemoval = true, since = "1.6.3")
	@XComment("{API.DOC.MEMBER.SEND_REGISTER_SMS_CODE}")
	@IgnoreDemoMode
	@GetMapping("/register_sms_code")
	public R<?> sendRegisterSmsCode(@RequestParam @Email @XComment("{API.DOC.MEMBER.EMAIL}") String email) {
		SendEmailRegisterCodeVO vo = emailPasswordMemberLoginType.sendEmailCode(email);
		return R.ok(IdUtils.simpleUUID()); // 历史兼容返回一个UUID，实际已无用
	}
}
