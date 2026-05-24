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
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.validator.Dict;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * CreateDeptRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.SYS.DEPT.CREATE_DEPT_REQ}")
public class CreateDeptRequest extends BaseDTO {

    @NotNull
    @XComment("{API.DOC.SYS.DEPT.PARENT_ID}")
    private Long parentId;

    @NotBlank
    @Length(max = 30)
    @XComment("{API.DOC.SYS.DEPT.DEPT_NAME}")
    private String deptName;

    @NotNull
    @XComment("{CC.ENTITY.SORT}")
    private Integer orderNum;

    @Length(max = 20)
    @XComment("{API.DOC.SYS.DEPT.LEADER}")
    private String leader;

    @Length(max = 11)
    @XComment("{API.DOC.SYS.DEPT.PHONE}")
    private String phone;

    @Email
    @Length(max = 20)
    @XComment("{API.DOC.SYS.DEPT.EMAIL}")
    private String email;

    @NotBlank
    @Dict(EnableOrDisable.TYPE)
    @XComment("{CC.ENTITY.STATUS}")
    private String status;
}
