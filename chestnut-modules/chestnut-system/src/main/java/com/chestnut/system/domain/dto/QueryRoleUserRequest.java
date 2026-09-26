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
import com.chestnut.system.validator.LongId;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * QueryRoleUserRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.SYS.ROLE.QUERY_ROLE_USER_REQ}")
public class QueryRoleUserRequest {

    @LongId
    @XComment("{API.DOC.SYS.ROLE.ID}")
    private Long roleId;

    @Length(max = 30)
    @XComment("{API.DOC.SYS.ROLE.USER_NAME}")
    private String userName;

    @Length(max = 30)
    @XComment("{API.DOC.SYS.ROLE.NICK_NAME}")
    private String nickName;

    @Length(max = 30)
    @XComment("{API.DOC.SYS.ROLE.REAL_NAME}")
    private String realName;

    @Length(max = 20)
    @XComment("{API.DOC.SYS.ROLE.PHONE_NUMBER}")
    private String phoneNumber;

    @Length(max = 20)
    @XComment("Email")
    private String email;
}
