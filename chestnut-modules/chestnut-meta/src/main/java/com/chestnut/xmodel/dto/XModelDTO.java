package com.chestnut.xmodel.dto;

import org.springframework.beans.BeanUtils;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.xmodel.domain.XModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class XModelDTO extends BaseDTO {

    private Long modelId;
    
    private String ownerType;
    
    private String ownerId;
    
    @NotBlank
    private String name;

    @Pattern(regexp = "[A-Za-z0-9_]+")
    private String code;

    @NotBlank
    private String tableName;
    
	public static XModelDTO newInstance(XModel model) {
		XModelDTO dto = new XModelDTO();
    	BeanUtils.copyProperties(model, dto);
		return dto;
	}
}
