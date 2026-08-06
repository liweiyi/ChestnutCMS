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
package com.chestnut.cloud.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.validator.Dict;
import tools.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * CreateCloudConfigRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.CLOUD.CREATE_REQ}")
@Getter
@Setter
public class CreateCloudConfigRequest extends BaseDTO {

    @XComment("{API.DOC.CLOUD.TYPE}")
    @NotBlank
    @Length(max = 50)
    private String type;

    @XComment("{API.DOC.CLOUD.CONFIG_NAME}")
    @NotBlank
    @Length(max = 100)
    private String configName;

    @XComment("{API.DOC.CLOUD.CONFIG_DESC}")
    @Length(max = 100)
    private String configDesc;

    @XComment("{CC.ENTITY.STATUS}")
    @NotBlank
    @Dict(EnableOrDisable.TYPE)
    private String status;

    @XComment("{API.DOC.CLOUD.CONFIG_PROPS}")
    @NotNull
    private ObjectNode configProps;

    @XComment("{CC.ENTITY.REMARK}")
    @Length(max = 500)
    private String remark;
}
