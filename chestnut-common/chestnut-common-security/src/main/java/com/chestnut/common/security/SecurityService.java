package com.chestnut.common.security;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityService {

	private final Map<String, IUserType> userTypes;
	
	public IUserType getUserType(String userType) {
		return this.userTypes.get(IUserType.BEAN_PRFIX + userType);
	}
	
	public List<IUserType> getUserTypes() {
		return this.userTypes.values().stream().toList();
	}
}
