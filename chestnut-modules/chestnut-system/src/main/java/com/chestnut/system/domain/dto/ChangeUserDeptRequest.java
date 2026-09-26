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
import com.chestnut.system.validator.LongId;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户机构调动请求。所属机构发生变更时，会解除该用户的全部角色关联。
 * 操作人由 Controller 入口的权限切面注入，不接受客户端指定。
 */
@Getter
@Setter
@XComment(value = "{API.DOC.SYS.USER.CHANGE_DEPT_REQ}", since = "1.6.1")
public class ChangeUserDeptRequest extends BaseDTO {

	/** 待调动用户的 ID，必须为已存在用户的有效正整数 ID。 */
	@LongId
	@XComment("{ENT.SYS.USER.ID}")
	private Long userId;

	/** 目标机构的 ID，必须为操作人可管理的真实机构，不能传 0。 */
	@LongId
	@XComment("{API.DOC.SYS.USER.CHANGE_DEPT.TARGET_DEPT_ID}")
	private Long deptId;
}
