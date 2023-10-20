package com.chestnut.common.security;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.session.SaSession;
import com.chestnut.common.security.domain.LoginUser;

import java.util.List;

/**
 * 用户类型接口
 */
public interface IUserType {

	String BEAN_PRFIX = "SATOKEN.USERTYPE.";

	/**
	 * 用户类型唯一标识
	 */
	String getType();

	/**
	 * 用户类型名称
	 */
	String getName();

	default boolean isLogin() {
		return SaManager.getStpLogic(this.getType()).isLogin();
	}

	default void login(Object loginId) {
		SaManager.getStpLogic(this.getType()).login(loginId);
	}

	default SaSession getTokenSession() {
		return SaManager.getStpLogic(this.getType()).getTokenSession();
	}

	default String getTokenValue() {
		return SaManager.getStpLogic(this.getType()).getTokenValue(true);
	}

	default LoginUser getLoginUser() {
		return this.getTokenSession().getModel(SaSession.USER, LoginUser.class);
	}

	default SaSession getTokenSessionByToken(String tokenValue) {
		return SaManager.getStpLogic(this.getType()).getTokenSessionByToken(tokenValue);
	}

	/**
	 * 获取用户权限列表
	 *
	 * @param loginUid
	 * @return
	 */
	default List<String> getPermissionList(Long loginUid) {
		return List.of();
	}

	/**
	 * 获取用户角色列表
	 *
	 * @param loginUid
	 * @return
	 */
	default List<String> getRoleList(Long loginUid) {
		return List.of();
	}
}
