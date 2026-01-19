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

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.common.validation.RegexConsts;
import com.chestnut.xmodel.domain.XModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.BeanUtils;

@Setter
@Getter
public class CreateXModelRequest extends BaseDTO {

    private String ownerType;

    private String ownerId;

    @NotBlank
    @Length(max = 100)
    private String name;

    @Pattern(regexp = RegexConsts.REGEX_CODE)
    @Length(max = 50)
    private String code;

    @NotBlank
    @Length(max = 100)
    private String tableName;

    @Length(max = 500)
    private String remark;
    
	public static CreateXModelRequest newInstance(XModel model) {
		CreateXModelRequest dto = new CreateXModelRequest();
    	BeanUtils.copyProperties(model, dto);
		return dto;
	}
}
