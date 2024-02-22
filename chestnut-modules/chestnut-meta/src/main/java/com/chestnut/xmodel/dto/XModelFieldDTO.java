/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
