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
package com.chestnut.message.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * CreateMessageTemplateReq
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MSG.CREATE_TEMPLATE_REQ}")
public class CreateMessageTemplateReq extends BaseDTO {

    @NotBlank
    @Length(max = 50)
    @XComment("{API.DOC.MSG.MSG_TYPE}")
    private String type;

    @NotBlank
    @Length(max = 100)
    @XComment("{CC.ENTITY.NAME}")
    private String name;

    @Length(max = 100)
    @XComment("{API.DOC.MSG.TEMPLATE_TITLE}")
    private String title;

    @NotBlank
    @XComment("{API.DOC.MSG.TEMPLATE_CONTENT}")
    private String content;
}
