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
import com.chestnut.system.fixed.dict.UserStatus;
import com.chestnut.system.validator.Dict;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * QueryUserRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("用户列表查询请求参数")
public class QueryUserRequest {

    @Length(max = 30)
    @XComment("用户名")
    private String userName;

    @Length(max = 20)
    @XComment("手机号")
    private String phoneNumber;

    @Dict(UserStatus.TYPE)
    @XComment("状态")
    private String status;

    @XComment("所属机构ID")
    private Long deptId;
}
