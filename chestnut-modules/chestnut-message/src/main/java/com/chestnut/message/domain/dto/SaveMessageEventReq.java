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
import com.chestnut.message.domain.CcMessageEvent;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.validator.Dict;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * CreateMessageEventReq
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MSG.SAVE_EVENT_REQ}")
public class SaveMessageEventReq extends BaseDTO {

    @NotBlank
    @Length(max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @XComment("{API.DOC.MSG.EVENT_ID}")
    private String eventId;

    @NotBlank
    @Length(max = 100)
    @XComment("{CC.ENTITY.NAME}")
    private String name;

    @NotBlank
    @Length(max = 1)
    @Dict(EnableOrDisable.TYPE)
    @XComment("{CC.ENTITY.STATUS}")
    private String status;

    @NotNull
    @XComment("{API.DOC.MSG.EVENT_NOTIFIES}")
    private List<CcMessageEvent.EventNotify> notifies;
}
