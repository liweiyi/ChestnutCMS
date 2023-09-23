package com.chestnut.vote.domain.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteSubjectVO {

	/**
	 * ID
	 */
	private Long subjectId;
	
	/**
	 * 类型（单选、多选、输入）
	 */
	private String type;
	
	/**
	 * 标题
	 */
	private String title;

	/**
	 * 排序标识
	 */
	private Integer sortFlag;

	/**
	 * 选项列表
	 */
	private List<VoteSubjectItemVO> items;
}
