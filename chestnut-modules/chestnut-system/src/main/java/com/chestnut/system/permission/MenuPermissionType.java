package com.chestnut.system.permission;

import cn.dev33.satoken.annotation.SaMode;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component(IPermissionType.BEAN_PREFIX + MenuPermissionType.ID)
public class MenuPermissionType implements IPermissionType {

	public static final String ID = "Menu";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "菜单权限";
	}

	@Override
	public Set<String> deserialize(String json) {
		if (StringUtils.isEmpty(json)) {
			return new HashSet<>();
		}
		return JacksonUtils.fromSet(json, String.class);
	}

	@Override
	public String serialize(Set<String> permissionKeys) {
		return JacksonUtils.to(permissionKeys);
	}

	@Override
	public boolean hasPermission(List<String> permissionKeys, String json, SaMode mode) {
		Set<String> perms = deserialize(json);
		if (mode == SaMode.AND) {
			for (String key : permissionKeys) {
				if(!perms.contains(key)) {
					return false;
				}
			}
			return true;
		} else {
			for (String key : permissionKeys) {
				if(perms.contains(key)) {
					return true;
				}
			}
			return false;
		}
	}
}
