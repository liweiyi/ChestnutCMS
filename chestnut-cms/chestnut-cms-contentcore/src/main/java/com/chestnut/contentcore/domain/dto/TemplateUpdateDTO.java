package com.chestnut.contentcore.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.validator.LongId;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateUpdateDTO extends BaseDTO {

	@LongId
	private Long templateId;

    private String content;
    
    private String remark;
}
