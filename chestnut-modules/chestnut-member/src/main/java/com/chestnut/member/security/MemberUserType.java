package com.chestnut.member.security;

import org.springframework.stereotype.Component;

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.IUserType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(IUserType.BEAN_PRFIX + MemberUserType.TYPE)
public class MemberUserType implements IUserType {

	public static final String TYPE = "cc_member";

	static {
		// 调用一次StpMemberUtil中的方法，保证其可以尽早的初始化 StpLogic ,@see SaManager.setConfig
		StpMemberUtil.getLoginType();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return I18nUtils.get("{SATOKEN.USERTYPE.MEMBER}");
	}
}
