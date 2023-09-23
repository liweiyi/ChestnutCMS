package com.chestnut.contentcore.domain.vo;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteStatVO {

	private Long siteId;
	
	private Long catalogCount;
	
	private Long contentCount;
	
	private Map<String, Long> contentDetails;
	
	private Long resourceCount;
	
	private Map<String, Long> resourceDetails;

	private Long templateCount;
}
