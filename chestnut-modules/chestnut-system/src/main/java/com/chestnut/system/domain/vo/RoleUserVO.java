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
package com.chestnut.system.domain.vo;

import com.chestnut.common.annotation.XComment;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.SysUser;

import java.time.LocalDateTime;
import java.util.List;

@XComment("{CC.SYS.USER.USER_INFO}")
public record RoleUserVO(
    Long userId,
    String userName,
    String realName,
    String nickName,
    String avatar,
    String phoneNumber,
    String email,
    LocalDateTime createTime,
    Boolean allocated
) {

    public static RoleUserVO create(SysUser user) {
        return new RoleUserVO(
                user.getUserId(),
                user.getUserName(),
                user.getRealName(),
                user.getNickName(),
                user.getAvatar(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getCreateTime(),
                true
        );
    }

    public static RoleUserVO create(SysUser user, boolean allocated) {
        return new RoleUserVO(
                user.getUserId(),
                user.getUserName(),
                user.getRealName(),
                user.getNickName(),
                user.getAvatar(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getCreateTime(),
                allocated
        );
    }
}
