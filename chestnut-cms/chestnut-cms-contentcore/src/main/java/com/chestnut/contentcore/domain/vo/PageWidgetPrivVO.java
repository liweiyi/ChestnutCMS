package com.chestnut.contentcore.domain.vo;

import com.chestnut.contentcore.perms.PrivItem;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PageWidgetPrivVO {

	/**
	 * 页面部件ID
	 */
	private Long pageWidgetId;

	/**
	 * 站点名称
	 */
	private String name;

	/**
	 * 拥有的权限项
	 */
	private Map<String, PrivItem> perms = new HashMap<>();
}
