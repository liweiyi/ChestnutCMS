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
