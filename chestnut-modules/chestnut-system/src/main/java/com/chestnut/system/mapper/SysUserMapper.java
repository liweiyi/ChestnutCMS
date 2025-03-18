/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.SysUserRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户表 数据层
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select({"""
			<script>
			SELECT sys_user.* FROM sys_user_role LEFT JOIN sys_user ON sys_user_role.user_id = sys_user.user_id WHERE sys_user_role.role_id = #{roleId} 
			<if test="userName != null and userName != ''"> AND sys_user.user_name like concat('%', #{userName}, '%')</if>
			<if test="phoneNumber != null and phoneNumber != ''"> AND sys_user.phone_number like concat('%', #{phoneNumber}, '%')</if>
			</script>
			"""})
    Page<SysUser> selectAllocatedList(Page<SysUserRole> page, @Param("roleId") Long roleId, @Param("userName") String userName, @Param("phoneNumber") String phoneNumber);
}
