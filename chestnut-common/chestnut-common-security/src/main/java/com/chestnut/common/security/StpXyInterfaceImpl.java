package com.chestnut.common.security;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;

/**
 * SaToken 权限缓存数据接口
 */
@Component
@RequiredArgsConstructor
public class StpXyInterfaceImpl implements StpInterface {
	
	private final Map<String, IUserType> userTypes;

	@Override
	public List<String> getPermissionList(Object loginId, String loginType) {
		IUserType userType = this.userTypes.get(IUserType.BEAN_PRFIX + loginType);
		return userType.getPermissionList(Long.parseLong(String.valueOf(loginId)));
	}

	@Override
	public List<String> getRoleList(Object loginId, String loginType) {
		IUserType userType = this.userTypes.get(IUserType.BEAN_PRFIX + loginType);
		return userType.getRoleList(Long.parseLong(String.valueOf(loginId)));
	}
}
