package com.chestnut.cms.stat.baidu.vo;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaiduTimeTrendVO extends LineChartVO {

	private Integer offset;
	
	/**
	 * 时间范围
	 */
	private String timeSpan;
	
	/**
	 * 指标字段
	 */
	private List<String> fields;
	
	/**
	 * 总数
	 */
	private Integer total;
	
	/**
	 * 指标合计
	 */
	private Map<String, Object> sum;
	
	/**
	 * 
	 */
	private Map<String, Object> pageSum;
}
