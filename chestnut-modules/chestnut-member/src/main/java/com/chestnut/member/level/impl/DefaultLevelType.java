package com.chestnut.member.level.impl;

import org.springframework.stereotype.Component;

import com.chestnut.member.level.ILevelType;

@Component(ILevelType.BEAN_PREFIX + DefaultLevelType.ID)
public class DefaultLevelType implements ILevelType {

	public static final String ID = "Default";
	
	private static final String NAME = "{MEMBER.LEVEL_TYPE." + ID + "}";
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return NAME;
	}
}
