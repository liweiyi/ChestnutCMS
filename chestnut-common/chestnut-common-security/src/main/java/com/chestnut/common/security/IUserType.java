package com.chestnut.common.security;

import java.util.List;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.StringUtils;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.session.SaSession;

/**
 * 用户类型接口
 */
public interface IUserType {

	public String BEAN_PRFIX = "SATOKEN.USERTYPE.";
	
	/**
	 * 用户类型唯一标识
	 */
	public String getType();
	
	/**
	 * 用户类型名称
	 */
	public String getName();

	default public boolean isLogin() {
		return SaManager.getStpLogic(this.getType()).isLogin();
	}
	
	default public void login(Object loginId) {
		SaManager.getStpLogic(this.getType()).login(loginId);
	}
	
	default public SaSession getTokenSession() {
		if (this.isLogin()) {
			return SaManager.getStpLogic(this.getType()).getTokenSession();
		}
		throw NotLoginException.newInstance(this.getType(), null);
	}

	default public String getTokenValue() {
		if (this.isLogin()) {
			String tokenValue = SaManager.getStpLogic(this.getType()).getTokenValue();
			if (StringUtils.isNotEmpty(tokenValue)) {
				return tokenValue;
			}
		}
		throw NotLoginException.newInstance(this.getType(), null);
	}
	
	default public LoginUser getLoginUser() {
		if (this.isLogin()) {
			return this.getTokenSession().getModel(SaSession.USER, LoginUser.class);
		}
		throw NotLoginException.newInstance(this.getType(), null);
	}
	
	default public SaSession getTokenSessionByToken(String tokenValue) {
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
