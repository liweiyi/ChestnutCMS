package com.chestnut.cms.stat.baidu.vo;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LineChartVO {

	/**
	 * x轴
	 */
	private List<String> xAxisDatas;
	
	/**
	 * y轴数据
	 */
	private Map<String, List<Object>> datas;
}
