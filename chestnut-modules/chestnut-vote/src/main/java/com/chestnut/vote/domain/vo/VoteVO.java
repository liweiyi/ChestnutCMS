package com.chestnut.vote.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteVO {

	/**
	 * ID
	 */
	private Long voteId;
	
	/**
	 * 问卷调查标题
	 */
	private String title;

	/**
	 * 开始时间
	 */
	private LocalDateTime startTime;

	/**
	 * 结束时间
	 */
	private LocalDateTime endTime;
	
	/**
	 * 参与人类型
	 */
	private String userType;
	
	/**
	 * 每日限制次数
	 */
	private Integer dayLimit;
	
	/**
	 * 总共可参与次数
	 */
	private Integer totalLimit;
	
	/**
	 * 结果查看方式（不允许查看、提交后可看、不限制）
	 */
	private String viewType;
	
	/**
	 * 已参与人数
	 */
	private Integer total;
	
	/**
	 * 主题列表
	 */
	private List<VoteSubjectVO> subjects;
}
