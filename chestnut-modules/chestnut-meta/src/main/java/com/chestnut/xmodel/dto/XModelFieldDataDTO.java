/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.xmodel.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.xmodel.core.IMetaModelType;
import com.chestnut.xmodel.core.MetaModelField;
import com.chestnut.xmodel.util.XModelUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@XComment("{API.DOC.META.FIELD_DATA_DTO}")
public class XModelFieldDataDTO {

	@XComment("{SYS.XMODEL_FIELD.NAME}")
    private String label;

	@XComment("{API.DOC.META.FIELD_NAME}")
    private String fieldName;

	@XComment("{API.DOC.META.FIELD_TYPE}")
	private String fieldType;

	@XComment("{SYS.XMODEL_FIELD.CONTROL_TYPE}")
    private String controlType;

	@XComment("{SYS.XMODEL_FIELD.VALIDATIONS}")
	private List<Map<String, Object>> validations;

	@XComment("{SYS.XMODEL_FIELD.OPTIONS}")
    private List<Map<String, String>> options;

	@XComment("{SYS.XMODEL_FIELD.VALUE}")
    private Object value;

	@XComment("{SYS.XMODEL_FIELD.VALUE_OBJ}")
	private Object valueObj;

	public static XModelFieldDataDTO newInstance(MetaModelField field, Object value) {
		XModelFieldDataDTO dto = new XModelFieldDataDTO();
		dto.setLabel(field.getName());
		dto.setFieldName(IMetaModelType.DATA_FIELD_PREFIX + field.getCode());
		dto.setControlType(field.getControlType());
		dto.setFieldType(field.getFieldType());
		dto.setValidations(field.getValidations());
		dto.setOptions(XModelUtils.getOptions(field.getOptions()));
		dto.setValue(value);
		return dto;
	}
}
