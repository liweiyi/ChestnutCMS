package com.chestnut.comment.domain.dto;

import java.util.List;

import com.chestnut.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditCommentDTO extends BaseDTO {

	/**
	 * 评论ID
	 */
	@NotEmpty
	private List<Long> commentIds;
		
	/**
	 * 通过/不通过（Y/N）
	 */
	@NotEmpty
	private String auditFlag;
}
