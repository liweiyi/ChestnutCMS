package com.chestnut.system.security;

import org.springframework.stereotype.Component;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.listener.SaTokenListenerForSimple;

@Component
public class SaTokenListenerForSys extends SaTokenListenerForSimple {

	@Override
	public void doSetConfig(SaTokenConfig config) {
		StpAdminUtil.getLoginType();
	}
}
