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
import com.chestnut.system.validator.LongId;
import tools.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * TestMessageConfigReq
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.MSG.TEST_CONFIG_REQ}")
public class TestMessageConfigReq {

    @LongId
    @XComment("{API.DOC.MSG.CONFIG_ID}")
    private Long configId;

    @NotNull
    @XComment("{API.DOC.MSG.TEST_PARAMS}")
    private ObjectNode params;
}
