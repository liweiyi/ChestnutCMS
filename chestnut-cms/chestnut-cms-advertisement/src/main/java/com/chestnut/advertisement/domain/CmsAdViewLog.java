package com.chestnut.advertisement.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.stat.RequestEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(CmsAdViewLog.TABLE_NAME)
public class CmsAdViewLog extends RequestEvent implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cms_ad_view_log";

	@TableId(value = "log_id", type = IdType.AUTO)
	private Long logId;
	
	/**
	 * 站点ID
	 */
	private Long siteId;
	
	/**
	 * 广告ID
	 */
	private Long adId;
	
	/**
	 * 广告名称
	 */
	@TableField(exist = false)
	private String adName;
}
