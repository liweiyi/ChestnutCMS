package com.chestnut.contentcore.user.preference;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.user.preference.IUserPreference;

import lombok.RequiredArgsConstructor;

/**
 * 内容编辑是否使用新窗口打开
 */
@Component
@RequiredArgsConstructor
public class OpenContentEditorWPreference implements IUserPreference {
	
	public static final String ID = "OpenContentEditorW";
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "内容编辑是否使用新窗口";
	}
	
	@Override
	public boolean validate(Object config) {
		return YesOrNo.YES.equals(config) || YesOrNo.NO.equals(config);
	}
	
	@Override
	public String getDefaultValue() {
		return YesOrNo.NO;
	}

	public static boolean getValue(LoginUser loginUser) {
		SysUser user = (SysUser) loginUser.getUser();
		return YesOrNo.isYes(MapUtils.getString(user.getPreferences(), ID, YesOrNo.NO));
	}
}
