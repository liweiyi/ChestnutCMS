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
package com.chestnut.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.dto.CreateUserRequest;
import com.chestnut.system.domain.dto.ResetUserPwdRequest;
import com.chestnut.system.domain.dto.UpdateUserRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户 业务层
 */
public interface ISysUserService extends IService<SysUser> {

	/**
	 * 根据用户ID查询用户所属角色组
	 *
	 * @param userId 用户ID
	 * @return 结果
	 */
	String selectUserRoleGroup(Long userId);

	/**
	 * 根据用户ID查询用户所属岗位组
	 *
	 * @param userId 用户ID
	 * @return 结果
	 */
	String selectUserPostGroup(Long userId);

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

	/**
	 * 新增用户信息
	 * 
	 * @param user 用户信息
	 */
	void insertUser(CreateUserRequest user);

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
	void updateUser(UpdateUserRequest user);

	/**
	 * 用户授权角色
	 * 
	 * @param userId
	 *            用户ID
	 * @param roleIds
	 *            角色组
	 */
	void insertUserAuth(Long userId, List<Long> roleIds);

	/**
	 * 重置用户密码
	 */
	void resetPwd(ResetUserPwdRequest req);

	/**
	 * 批量删除用户信息
	 * 
	 * @param userIds 需要删除的用户ID
	 */
	void deleteUserByIds(List<Long> userIds);

	/**
	 * 解锁用户
	 */
	void unlockUser(Long userId);

	/**
	 * 上传用户头像
	 * 
	 * @param userId 用户ID
	 * @param file 头像文件
	 * @return 头像文件相对路径
	 */
	String uploadAvatar(Long userId, MultipartFile file);
}
