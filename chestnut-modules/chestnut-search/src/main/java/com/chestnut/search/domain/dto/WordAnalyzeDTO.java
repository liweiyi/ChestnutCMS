package com.chestnut.search.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WordAnalyzeDTO {
	
	@NotEmpty
	private String type;

	@NotEmpty
    private String text;
}