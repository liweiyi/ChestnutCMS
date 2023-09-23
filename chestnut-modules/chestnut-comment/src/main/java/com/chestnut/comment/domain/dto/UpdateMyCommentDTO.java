package com.chestnut.comment.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMyCommentDTO extends BaseDTO {

	/**
	 * 评论ID
	 */
	@Min(1)
	private Long commentId;
		
	/**
	 * 评论内容
	 */
	@NotBlank
	private String content;
}
