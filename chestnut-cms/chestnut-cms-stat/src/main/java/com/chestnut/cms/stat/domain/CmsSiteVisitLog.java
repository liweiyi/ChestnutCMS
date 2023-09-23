package com.chestnut.cms.stat.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.stat.RequestEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(CmsSiteVisitLog.TABLE_NAME)
public class CmsSiteVisitLog extends RequestEvent implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cms_site_visit_log";

	@TableId(value = "log_id", type = IdType.AUTO)
	private Long logId;

	/**
	 * 站点ID
	 */
	private Long siteId;
	
	/**
	 * 栏目ID
	 */
	private Long catalogId;
	
	/**
	 * 内容ID
	 */
	private Long contentId;
}
