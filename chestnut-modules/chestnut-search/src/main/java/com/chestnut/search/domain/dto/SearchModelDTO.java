package com.chestnut.search.domain.dto;

import java.util.ArrayList;
import java.util.List;

import com.chestnut.common.security.domain.BaseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchModelDTO extends BaseDTO {
	
	/*
	 * 索引标签
	 */
	private String label;

	/*
	 * 索引名
	 */
	private String name;
	
	/*
	 * 索引字段列表
	 */
	private List<SearchIndexField> fields = new ArrayList<>();
	
	@Getter
	@Setter
	public static class SearchIndexField {
		
		/*
		 * 字段标签
		 */
		private String label;
		
		/*
		 * 字段名
		 */
		private String name;
		
		/*
		 * 字段类型
		 */
		private String type;
		
		/*
		 * 是否主键
		 */
		private boolean primary;
		
		/*
		 * 字段权重 
		 */
		private double weight = 1;
		
		/*
		 * 是否索引/可搜索
		 */
		private boolean index;
		
		/*
		 * 分词策略
		 */
		private String analyzer;
		
		public SearchIndexField(String label, String name, String type, boolean primary) {
			this.label = label;
			this.name = name;
			this.type = type;
			this.primary = primary;
			this.weight = 1;
			this.index = true;
		}
	}
}
