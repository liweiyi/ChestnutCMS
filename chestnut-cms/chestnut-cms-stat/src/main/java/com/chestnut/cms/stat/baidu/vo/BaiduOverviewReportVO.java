package com.chestnut.cms.stat.baidu.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 百度概况统计通用数据结构
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class BaiduOverviewReportVO extends LineChartVO {
	
	/**
	 * 查询指标
	 */
	private List<String> fields;
}
