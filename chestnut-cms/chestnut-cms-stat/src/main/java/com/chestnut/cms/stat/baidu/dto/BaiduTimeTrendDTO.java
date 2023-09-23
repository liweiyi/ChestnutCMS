package com.chestnut.cms.stat.baidu.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaiduTimeTrendDTO {

	/**
	 * 站点ID
	 */
	@NotNull(message = "{VALIDATOR.CMS.STAT.BD_SITE_ID_NOT_NULL}")
	private Long siteId;
	
	/**
	 * 开始时间
	 */
	@NotNull(message = "{VALIDATOR.CMS.STAT.START_DATE_NOT_NULL}")
	private LocalDateTime startDate;
	
	/**
	 * 结束时间
	 */
	@NotNull(message = "{VALIDATOR.CMS.STAT.END_DATE_NOT_NULL}")
	private LocalDateTime endDate;
	
	/**
	 * 指标
	 */
	private List<String> metrics;
	
	/**
	 * 时间粒度
	 */
	@NotEmpty(message = "{VALIDATOR.CMS.STAT.TIME_GRAN_NOT_NULL}")
	private String gran;
	
	/**
	 * 来源
	 */
	private String source;
	
	/**
	 * 设备类型
	 */
	private String deviceType;
	
	/**
	 * 区域
	 */
	private String area;
	
	/**
	 * 访客类型
	 */
	private String visitor;
}
