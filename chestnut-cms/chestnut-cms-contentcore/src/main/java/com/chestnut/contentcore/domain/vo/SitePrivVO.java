package com.chestnut.contentcore.domain.vo;

import com.chestnut.contentcore.perms.PrivItem;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class SitePrivVO {

	/**
	 * 站点ID
	 */
	private Long siteId;

	/**
	 * 站点名称
	 */
	private String name;

	/**
	 * 拥有的权限项
	 */
	private Map<String, PrivItem> perms = new HashMap<>();
}
