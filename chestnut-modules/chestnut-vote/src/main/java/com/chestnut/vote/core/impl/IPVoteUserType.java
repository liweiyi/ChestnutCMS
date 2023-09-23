package com.chestnut.vote.core.impl;

import org.springframework.stereotype.Component;

import com.chestnut.common.utils.ServletUtils;
import com.chestnut.vote.core.IVoteUserType;

@Component(IVoteUserType.BEAN_PREFIX + IPVoteUserType.ID)
public class IPVoteUserType implements IVoteUserType {

	public static final String ID = "ip";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "{VOTE.USER_TYPE.IP}";
	}

	@Override
	public String getUserId() {
		return ServletUtils.getIpAddr(ServletUtils.getRequest());
	}
}
