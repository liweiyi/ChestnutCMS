package com.chestnut.system.security;

import java.util.List;

import cn.dev33.satoken.session.SaSession;
import com.chestnut.common.security.domain.LoginUser;
import org.springframework.stereotype.Component;

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.IUserType;
import com.chestnut.system.service.ISysPermissionService;
import com.chestnut.system.service.ISysRoleService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(IUserType.BEAN_PRFIX + AdminUserType.TYPE)
public class AdminUserType implements IUserType {

	public static final String TYPE = "sys_user";

	private final ISysPermissionService permissionService;

	private final ISysRoleService roleService;

	static {
		// 调用一次StpAdminUtil中的方法，保证其可以尽早的初始化 StpLogic ,@see SaManager.setConfig
		StpAdminUtil.getLoginType();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getName() {
		return I18nUtils.get("{SATOKEN.USERTYPE.ADMIN}");
	}

	@Override
	public List<String> getPermissionList(Long loginUid) {
		SaSession tokenSession = StpAdminUtil.getTokenSessionByToken(StpAdminUtil.getTokenValueByLoginId(loginUid));
		return tokenSession.getModel(SaSession.USER, LoginUser.class).getPermissions();
	}

	@Override
	public List<String> getRoleList(Long loginUid) {
		return roleService.selectRoleKeysByUserId(loginUid);
	}
}
