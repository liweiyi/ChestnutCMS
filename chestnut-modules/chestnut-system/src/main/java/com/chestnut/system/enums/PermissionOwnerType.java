package com.chestnut.system.enums;

public enum PermissionOwnerType {

	Role, User;

	public static boolean isUser(String ownerType) {
		return User.name().equals(ownerType);
	}
}
