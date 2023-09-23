package com.chestnut.contentcore.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.chestnut.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetTopContentDTO extends BaseDTO {

	/**
	 * 置顶内容IDs
	 */
	@NotEmpty
	public List<Long> contentIds;
	
	/**
	 * 置顶结束时间
	 */
	public LocalDateTime topEndTime;
}
