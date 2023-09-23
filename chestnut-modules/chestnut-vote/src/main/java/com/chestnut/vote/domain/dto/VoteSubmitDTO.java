package com.chestnut.vote.domain.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteSubmitDTO {

	/**
	 * ID
	 */
	private Long voteId;

	/**
	 * 问卷调查主题信息
	 */
	private List<SubjectResult> subjects;
	
	/**
	 * IP
	 */
	private String ip;
	
	/**
	 * UserAgent
	 */
	private String userAgent;

	@Getter
	@Setter
	public static class SubjectResult {

		/**
		 * 主题ID
		 */
		private Long subjectId;
		
		/**
		 * 结果：itemId || inputText
		 */
		private String result;
	}
}
