package com.chestnut.vote.domain.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteSubjectItemVO {

	/**
	 * ID
	 */
	private Long itemId;
	
	/**
	 * 主题ID
	 */
	private Long subjectId;
	
	/**
	 * 类型（文字、图片、内容引用）
	 */
	private String type;
	
	/**
	 * 选项内容（文字内容、图片地址、内容引用地址）
	 */
	private String content;
	
	/**
	 * 选项描述
	 */
	private String description;

	/**
	 * 排序标识
	 */
	private Integer sortFlag;
	
	/**
	 * 票数
	 */
	private Long voteTotal;
}
