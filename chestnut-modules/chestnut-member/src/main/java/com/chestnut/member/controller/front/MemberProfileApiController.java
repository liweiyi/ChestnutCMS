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

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.member.config.MemberConfig_AccountEmailMessage;
import com.chestnut.member.domain.Member;
import com.chestnut.member.domain.dto.ChangeMemberEmailRequest;
import com.chestnut.member.domain.dto.ModifyMemberInfoRequest;
import com.chestnut.member.domain.dto.ModifyMemberPasswordRequest;
import com.chestnut.member.exception.MemberTips;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.service.IMemberConfigService;
import com.chestnut.member.service.IMemberService;
import com.chestnut.member.service.IMemberStatDataService;
import com.chestnut.member.util.MemberUtils;
import com.chestnut.message.core.email.EmailMessageType;
import com.chestnut.message.domain.dto.UserMessageSendRequest;
import com.chestnut.message.service.IMessageSendService;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.service.ISecurityConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@XComment("{API.DOC.MEMBER.PROFILE_API_MODULE}")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member")
public class MemberProfileApiController extends BaseRestController {

	private final ISecurityConfigService securityConfigService;

	private final IMemberService memberService;

	private final IMemberStatDataService memberStatDataService;

	private final IMemberConfigService memberConfigService;

	private final IMessageSendService messageSendService;

	private final MemberConfig_AccountEmailMessage accountEmailMessage;

	private final RedisCache redisCache;

	private static final String SMS_CODE_CACHE_PREFIX = "member:sms_code:";

	@XComment("{API.DOC.MEMBER.SAVE_INFO}")
	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/info")
	public R<?> saveMemberInfo(@RequestBody @Validated ModifyMemberInfoRequest req) {
		LoginUser loginUser = StpMemberUtil.getLoginUser();
		Member member = (Member) loginUser.getUser();

		boolean update = this.memberService.lambdaUpdate().set(Member::getNickName, req.getNickName())
				.set(Member::getSlogan, req.getSlogan())
				.set(Member::getDescription, req.getDescription())
				.eq(Member::getMemberId, member.getMemberId())
				.update();
		if (!update) {
			return R.fail();
		}
		member.setNickName(req.getNickName());
		member.setSlogan(req.getSlogan());
		member.setDescription(req.getDescription());
		StpMemberUtil.setLoginUser(loginUser);

		this.memberStatDataService.removeMemberCache(member.getMemberId());
		return R.ok();
	}

	@XComment("{API.DOC.MEMBER.UPLOAD_AVATAR}")
	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/avatar")
	public R<?> uploadMemberAvatar(@RequestParam @XComment("{API.DOC.MEMBER.AVATAR_IMAGE}") String image,
								   @RequestParam(required = false, defaultValue = "false") @XComment("{API.DOC.MEMBER.PREVIEW}") Boolean preview) throws IOException {
		String url = this.memberService.uploadAvatarByBase64(StpMemberUtil.getLoginIdAsLong(), image);

		this.memberStatDataService.removeMemberCache(StpMemberUtil.getLoginIdAsLong());
		return R.ok(MemberUtils.getMemberResourcePrefix(preview) + url);
	}

	@XComment("{API.DOC.MEMBER.RESET_SELF_PWD}")
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/reset_pwd")
	public R<?> resetMemberPassword(@RequestBody @Validated ModifyMemberPasswordRequest req) {
		Member member = this.memberService.getById(StpMemberUtil.getLoginIdAsLong());
		if (!SecurityUtils.matches(req.getPassword(), member.getPassword())) {
			return R.fail(MemberTips.WRONG_PASSWORD.locale(LocaleContextHolder.getLocale()));
		}
		// 密码规则校验
		this.securityConfigService.validPassword(member, req.getNewPassword());

		boolean update = this.memberService.lambdaUpdate()
				.set(Member::getPassword, SecurityUtils.passwordEncode(req.getNewPassword()))
				.eq(Member::getMemberId, member.getMemberId())
				.update();
		return update ? R.ok() : R.fail();
	}

	@XComment("{API.DOC.MEMBER.SEND_SMS_CODE}")
	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/sms_code")
	public R<?> sendSmsCode(@RequestParam(required = false, defaultValue = "email") @XComment("{API.DOC.MEMBER.SMS_CODE_TYPE}") String type) {
		try {
			Member member = this.memberService.getById(StpMemberUtil.getLoginIdAsLong());
			String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
			Long[] messageConfig = memberConfigService.getConfigValue(accountEmailMessage);
			messageSendService.sendUserMessage(new UserMessageSendRequest(
					EmailMessageType.TYPE,
					MemberConfig_AccountEmailMessage.CHANGE_EMAIL_SCENE,
					member.getEmail(),
					messageConfig[MemberConfig_AccountEmailMessage.CONFIG_ID_INDEX],
					messageConfig[MemberConfig_AccountEmailMessage.TEMPLATE_ID_INDEX],
					JacksonUtils.objectNode().put("code", code)
			));

			redisCache.setCacheObject(SMS_CODE_CACHE_PREFIX + member.getMemberId(), code, 600, TimeUnit.SECONDS);
			return R.ok();
		} catch (GlobalException e) {
			throw e;
		} catch (Exception e) {
			return R.fail("发送失败，请联系管理员！");
		}
	}

	@XComment("{API.DOC.MEMBER.CHANGE_EMAIL}")
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/change_email")
	public R<?> changeMemberEmail(@RequestBody @Validated ChangeMemberEmailRequest dto) {
		String authCode = this.redisCache.getCacheObject(SMS_CODE_CACHE_PREFIX + StpMemberUtil.getLoginIdAsLong(), String.class);
		if (StringUtils.isEmpty(authCode) || !dto.getAuthCode().equals(authCode)) {
			return R.fail("验证码错误");
		}

		Member member = this.memberService.getById(StpMemberUtil.getLoginIdAsLong());
		boolean update = this.memberService.lambdaUpdate()
				.set(Member::getEmail, dto.getEmail())
				.eq(Member::getMemberId, member.getMemberId())
				.update();
		return update ? R.ok() : R.fail();
	}
}
