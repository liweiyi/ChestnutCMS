package com.chestnut.member.level.impl;

import org.springframework.stereotype.Component;

import com.chestnut.member.level.IExpOperation;

/**
 * 等级经验操作项：签到
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IExpOperation.BEAN_PREFIX + SignInExpOperation.ID)
public class SignInExpOperation implements IExpOperation {
	
	public static final String ID = "SignIn";
	
	private static final String NAME = "{MEMBER.EXP_OPERATION." + ID + "}";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
