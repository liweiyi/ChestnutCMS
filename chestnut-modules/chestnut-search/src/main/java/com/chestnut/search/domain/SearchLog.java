package com.chestnut.search.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 搜索日志
 */
@Getter
@Setter
@TableName(SearchLog.TABLE_NAME)
public class SearchLog implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "search_log";

	@TableId(value = "log_id", type = IdType.AUTO)
	private Long logId;

	/**
	 * 搜索词
	 */
	private String word;

	/**
	 * Header:UserAgent
	 */
	private String userAgent;

	/**
	 * IP地址
	 */
	private String ip;

	private String location;

	/**
	 * Header:Referer
	 */
	private String referer;

	/**
	 * 搜索客户端类型
	 */
	private String clientType;

	/**
	 * 来源
	 */
	private String source;

	/**
	 * 搜索时间
	 */
	private LocalDateTime logTime;
}