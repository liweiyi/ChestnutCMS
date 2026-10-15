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
package com.chestnut.system.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@XComment("{API.DOC.SYS.ACCOUNT.BATCH_CHANGE_STATUS_REQ}")
public class BatchChangeAccountStatusRequest extends BaseDTO {

    public static final String DISABLE = "disable";

    public static final String REMOVE_DISABLEMENT = "removeDisablement";

    public static final String LOCK = "lock";

    public static final String UNLOCK = "unlock";

    @NotEmpty
    @XComment("{API.DOC.SYS.ACCOUNT.IDS}")
    private List<@NotNull @Positive Long> ids;

    @NotBlank
    @Pattern(regexp = "disable|removeDisablement|lock|unlock")
    @XComment("{API.DOC.SYS.ACCOUNT.STATUS_ACTION}")
    private String action;

    /** 锁定操作必须填写结束时间；已到期时恢复正常状态。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @XComment("{ENT.SYS.USER.LOCK_END_TIME}")
    private LocalDateTime lockEndTime;

    @JsonIgnore
    public boolean isActionValid() {
        return DISABLE.equals(action) || REMOVE_DISABLEMENT.equals(action) || LOCK.equals(action) || UNLOCK.equals(action);
    }

    @JsonIgnore
    @AssertTrue(message = "{VALID.SYS.LOCK_END_TIME_REQUIRED}")
    public boolean isLockEndTimeValid() {
        return !LOCK.equals(action) || lockEndTime != null;
    }
}
