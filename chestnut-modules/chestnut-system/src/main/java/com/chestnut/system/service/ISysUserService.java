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
package com.chestnut.system.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.dto.*;

import java.util.List;

/**
 * 用户 业务层
 */
public interface ISysUserService extends IService<SysUser> {

	/**
	 * 校验用户名称是否唯一
	 *
	 * @param username 用户名
	 * @param userId 用户ID
	 * @return 结果
	 */
	boolean checkUserNameUnique(String username, Long userId);

	/**
	 * 校验手机号码是否唯一
	 *
	 * @param phoneNumber 手机号
	 * @param userId 用户ID
	 * @return 结果
	 */
	boolean checkPhoneUnique(String phoneNumber, Long userId);

	/**
	 * 校验email是否唯一
	 *
	 * @param email email
	 * @param userId 用户ID
	 * @return 结果
	 */
	boolean checkEmailUnique(String email, Long userId);

	void updateUserProfile(UpdateUserProfileRequest req);

	/**
	 * 新增用户信息
	 * 
	 * @param user 用户信息
	 */
	SysUser insertUser(CreateUserRequest user);

    SysUser createBindingUser(CreateBindingUserRequest req);

    /**
	 * 注册用户信息
	 * 
	 * @param user 用户信息
	 */
	void registerUser(SysUser user);

	/**
	 * 修改用户信息
	 * 
	 * @param user 用户信息
	 */
	SysUser updateUser(UpdateUserRequest user);

	/**
	 * 重置用户密码
	 */
	void resetPwd(ResetUserPwdRequest req);

	/**
	 * 批量删除用户信息
	 * 
	 * @param userIds 需要删除的用户ID
	 * @param operator 由 Controller 获取并传入的可信操作人，用于校验机构数据范围
	 */
	void deleteUserByIds(List<Long> userIds, LoginUser operator);

	/**
	 * 解锁用户
	 */
	void unlockUser(Long userId);

	/**
	 * 上传用户头像
	 * 
	 * @param userId 用户ID
	 * @param fileBytes 头像文件
	 * @return 头像文件相对路径
	 */
	String uploadAvatar(Long userId, byte[] fileBytes);
}
