package com.chestnut.common.log;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogDetail implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 日志类型
	 */
	private String logType;
	
	/**
	 * 日志标题
	 */
	private String logTitle;
	
	/**
	 * 日志业务类型
	 */
	private String businessType;
	
	/**
	 * 日志时间
	 */
	private LocalDateTime logTime;

	/**
	 * 耗时，单位（毫秒）
	 */
	private Long cost;
	
	/**
	 * 日志详情
	 */
	private Map<String, Object> details;
}
