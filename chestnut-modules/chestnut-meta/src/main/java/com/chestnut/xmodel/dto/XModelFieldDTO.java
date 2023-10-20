package com.chestnut.xmodel.dto;

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.xmodel.domain.XModelField;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class XModelFieldDTO extends BaseDTO {
	
	private Long fieldId;

    private Long modelId;

    @NotNull
    private String name;

    @NotNull
    private String code;

    private String fieldType;

    private String fieldName;

    @NotNull
    private String controlType;

    private List<Map<String, Object>> validations;

    private FieldOptions options;
    
    private String defaultValue;

    private Long sortFlag;
    
    private boolean isDefaultTable;
    
	public static XModelFieldDTO newInstance(XModelField modelField) {
		XModelFieldDTO dto = new XModelFieldDTO();
    	BeanUtils.copyProperties(modelField, dto);
		return dto;
	}
}
