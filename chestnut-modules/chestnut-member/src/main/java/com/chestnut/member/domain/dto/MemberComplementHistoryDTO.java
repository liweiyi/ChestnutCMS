package com.chestnut.member.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberComplementHistoryDTO extends BaseDTO {

	@NotNull
	@Min(1970)
	private Integer year;

	@NotNull
	@Min(1)
	@Max(12)
	private Integer month;

	@NotNull
	@Min(1)
	@Max(31)
	private Integer day;
}
