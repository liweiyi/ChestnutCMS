package com.chestnut.contentcore.domain.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublishContentDTO {

	/**
	 * 内容ID列表
	 */
	@NotEmpty
	private List<Long> contentIds;
}
