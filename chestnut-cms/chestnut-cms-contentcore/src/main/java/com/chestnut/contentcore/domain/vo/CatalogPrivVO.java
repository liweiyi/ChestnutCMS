package com.chestnut.contentcore.domain.vo;

import com.chestnut.contentcore.perms.PrivItem;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class CatalogPrivVO {

	private Long catalogId;
	
	private Long parentId;

	private String name;
	
	private List<CatalogPrivVO> children;

	/**
	 * 拥有的权限项
	 */
	private Map<String, PrivItem> perms = new HashMap<>();
}
