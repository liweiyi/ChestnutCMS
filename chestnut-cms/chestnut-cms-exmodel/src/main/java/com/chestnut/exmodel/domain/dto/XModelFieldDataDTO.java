package com.chestnut.exmodel.domain.dto;

import com.chestnut.exmodel.CmsExtendMetaModelType;
import com.chestnut.xmodel.domain.XModelField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class XModelFieldDataDTO {

    private String label;
    
    private String fieldName;
    	
    private String controlType;

	private List<Map<String, Object>> validations;
    
    private List<Map<String, String>> options;
    
    private Object value;

	private String valueSrc;
    
	public static XModelFieldDataDTO newInstance(XModelField field, String value) {
		XModelFieldDataDTO dto = new XModelFieldDataDTO();
		dto.setLabel(field.getName());
		dto.setFieldName(CmsExtendMetaModelType.DATA_FIELD_PREFIX + field.getCode());
		dto.setControlType(field.getControlType());
		dto.setValidations(field.getValidations());
		dto.setValue(value);
		return dto;
	}
}
