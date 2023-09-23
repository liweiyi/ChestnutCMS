package com.chestnut.system.logs.impl;

import org.springframework.stereotype.Component;

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.system.logs.ILogMenu;

@Component
public class UserOperationLogMenu implements ILogMenu {

	@Override
	public String getId() {
		return "UserOperation";
	}

	@Override
	public String getName() {
		return I18nUtils.get("{LOG.MENU." + getId() + "}");
	}

	@Override
	public String getRouter() {
		return "/monitor/logs/operation";
	}
}
