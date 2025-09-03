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
package com.chestnut.system.service.impl;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.common.validation.BeanValidators;
import com.chestnut.system.config.SystemConfig;
import com.chestnut.system.domain.*;
import com.chestnut.system.domain.dto.CreateUserRequest;
import com.chestnut.system.domain.dto.ResetUserPwdRequest;
import com.chestnut.system.domain.dto.UpdateUserRequest;
import com.chestnut.system.domain.dto.UserImportData;
import com.chestnut.system.enums.PermissionOwnerType;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.exception.SystemUserTips;
import com.chestnut.system.fixed.dict.UserStatus;
import com.chestnut.system.mapper.*;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.*;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户 业务层处理
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

	private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

	private final ISysRoleService roleService;

	private final SysPostMapper postMapper;

	private final SysUserPostMapper userPostMapper;

	private final SysUserRoleMapper userRoleMapper;

	private final ISecurityConfigService securityConfigService;

	private final SysPermissionMapper permissionMapper;

	@Override
	public String selectUserRoleGroup(Long userId) {
		List<SysRole> list = roleService.selectRolesByUserId(userId);
		if (CollectionUtils.isEmpty(list)) {
			return StringUtils.EMPTY;
		}
		return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
	}

	@Override
	public String selectUserPostGroup(Long userId) {
		List<Long> postIds = this.userPostMapper.selectList(
				new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, userId)
		).stream().map(SysUserPost::getPostId).toList();
		if (CollectionUtils.isEmpty(postIds)) {
			return StringUtils.EMPTY;
		}
		return postMapper.selectList(
				new LambdaQueryWrapper<SysPost>().select(SysPost::getPostName).in(SysPost::getPostId, postIds)
		).stream().map(SysPost::getPostName).collect(Collectors.joining(","));
	}

	@Override
	public boolean checkUserNameUnique(String username, Long userId) {
		long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username)
				.ne(IdUtils.validate(userId), SysUser::getUserId, userId));
		return count == 0;
	}

	private boolean checkUserUnique(String username, String phoneNumber, String email, Long userId) {
		LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<SysUser>()
				.and(wrapper -> wrapper
						.eq(StringUtils.isNotEmpty(username), SysUser::getUserName, username).or()
						.eq(StringUtils.isNotEmpty(phoneNumber), SysUser::getPhoneNumber, phoneNumber)
						.or().eq(StringUtils.isNotEmpty(email), SysUser::getEmail, email))
				.ne(IdUtils.validate(userId), SysUser::getUserId, userId);
		return this.count(q) == 0;
	}

	@Override
	public boolean checkPhoneUnique(String phoneNumber, Long userId) {
		if (StringUtils.isNotEmpty(phoneNumber)) {
			long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhoneNumber, phoneNumber)
					.ne(IdUtils.validate(userId), SysUser::getUserId, userId));
			return count == 0;
		}
		return true;
	}

	@Override
	public boolean checkEmailUnique(String email, Long userId) {
		if (StringUtils.isNotEmpty(email)) {
			long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email)
					.ne(IdUtils.validate(userId), SysUser::getUserId, userId));
			return count == 0;
		}
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void insertUser(CreateUserRequest req) {
		boolean checkUserUnique = this.checkUserUnique(req.getUserName(), req.getPhoneNumber(), req.getEmail(), null);
		Assert.isTrue(checkUserUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("[username,phoneNumber,email]"));
		// 校验密码
		SysUser user = new SysUser();
		BeanUtils.copyProperties(req, user);
		this.securityConfigService.validPassword(user, req.getPassword());
		// 强制首次登陆修改密码
		this.securityConfigService.forceModifyPwdAfterUserAdd(user);
		// 密码家吗
		user.setUserId(IdUtils.getSnowflakeId());
		user.setPassword(SecurityUtils.passwordEncode(req.getPassword()));
		user.createBy(req.getOperator().getUsername());
		this.save(user);
		// 新增用户岗位关联
		syncUserPost(user.getUserId(), req.getPostIds(), false);
	}

	@Override
	public void registerUser(SysUser user) {
		boolean checkUserUnique = this.checkUserUnique(user.getUserName(), user.getPhoneNumber(), user.getEmail(), null);
		Assert.isTrue(checkUserUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("[username,phoneNumber,email]"));

		this.securityConfigService.validPassword(user, user.getPassword());

		user.setUserId(IdUtils.getSnowflakeId());
		user.setPassword(SecurityUtils.passwordEncode(user.getPassword()));
		user.setCreateTime(LocalDateTime.now());
		this.save(user);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUser(UpdateUserRequest req) {
		SysUser db = this.getById(req.getUserId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(req.getUserId()));
		boolean checkUserUnique = this.checkUserUnique(req.getUserName(), req.getPhoneNumber(), req.getEmail(), req.getUserId());
		Assert.isTrue(checkUserUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("[username,phoneNumber,email]"));

		String oldStatus = db.getStatus();

		db.setNickName(req.getNickName());
		db.setRealName(req.getRealName());
		db.setDeptId(req.getDeptId());
		db.setPhoneNumber(req.getPhoneNumber());
		db.setEmail(req.getEmail());
		db.setSex(req.getSex());
		db.setStatus(req.getStatus());
		db.updateBy(req.getOperator().getUsername());
		this.updateById(db);
		// 用户与岗位关联
		syncUserPost(db.getUserId(), req.getPostIds(), true);
		// 变更未封禁或锁定状态时注销登录状态
		if (!StringUtils.equals(db.getStatus(), oldStatus)
				&& (UserStatus.isDisbale(db.getStatus()) || UserStatus.isLocked(db.getStatus()))) {
			StpAdminUtil.logout(req.getUserId());
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void insertUserAuth(final Long userId, final List<Long> roleIds) {
		userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
		if (StringUtils.isNotEmpty(roleIds)) {
			List<SysUserRole> list = roleIds.stream().map(roleId -> new SysUserRole(userId, roleId)).toList();
			list.forEach(userRoleMapper::insert);
		}
	}

	@Override
	public void resetPwd(ResetUserPwdRequest req) {
		SysUser db = this.getById(req.getUserId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("userId", req.getUserId()));

		this.securityConfigService.validPassword(db, req.getPassword());

		db.setPassword(SecurityUtils.passwordEncode(req.getPassword()));
		db.setUpdateTime(LocalDateTime.now());
		db.setUpdateBy(req.getOperator().getUsername());
		this.updateById(db);
	}

	public void syncUserPost(Long userId, Long[] postIds, boolean update) {
		if (update) {
			userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getUserId, userId));
		}
		if (StringUtils.isNotEmpty(postIds)) {
			// 新增用户与岗位管理
			Stream.of(postIds).map(postId -> {
				SysUserPost up = new SysUserPost();
				up.setPostId(postId);
				up.setUserId(userId);
				return up;
			}).forEach(userPostMapper::insert);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteUserByIds(List<Long> userIds) {
		for (Long userId : userIds) {
			Assert.isFalse(SecurityUtils.isSuperAdmin(userId), SysErrorCode.SUPERADMIN_DELETE::exception);
			// 注销已登录token
			StpAdminUtil.logout(userId);
		}
		// 删除用户与角色关联
		userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
		// 删除用户与岗位关联
		userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().in(SysUserPost::getUserId, userIds));
		// 删除用户权限配置
		this.permissionMapper.delete(new LambdaQueryWrapper<SysPermission>()
				.eq(SysPermission::getOwnerType, PermissionOwnerType.User.name())
				.in(SysPermission::getOwner, userIds));
		// 删除用户数据
		this.removeByIds(userIds);
	}

	@Override
	public void unlockUser(Long userId) {
		SysUser user = this.getById(userId);
		Assert.notNull(user, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(userId));

		if (!UserStatus.isLocked(user.getStatus())) {
			return;
		}
		user.setStatus(UserStatus.ENABLE);
		user.setLockEndTime(null);
		this.updateById(user);
	}

	@Override
	public String uploadAvatar(Long userId, MultipartFile file) {
		try {
			SysUser user = this.getById(userId);
			Assert.notNull(user, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(userId));
			// 文件后缀名
			String ext = FileExUtils.getExtension(file.getBytes());
			// 上传相对路径
			String path = "avatar/" + DateUtils.datePath() + "/" + userId + "." + ext;
			// 写入文件
			FileUtils.writeByteArrayToFile(new File(SystemConfig.getUploadDir() + path), file.getBytes());
			// 设置用户头像
			user.setAvatar(path);
			this.lambdaUpdate().set(SysUser::getAvatar, path).eq(SysUser::getUserId, userId).update();
			return path;
		} catch (IOException e) {
			throw CommonErrorCode.SYSTEM_ERROR.exception(e.getMessage());
		}
	}

	@RequiredArgsConstructor
	public static class SysUserReadListener implements ReadListener<UserImportData> {

		private final ISysUserService userService;

		private final ISysDeptService deptService;

		private final ISysRoleService roleService;

		private final ISysPostService postService;

		private final SysUserRoleMapper userRoleMapper;

		@Setter
		private Validator validator;

		private boolean isUpdateSupport;

		@Setter
		private String operator;

		private int successCount;

		private int failCount;

		@Setter
		private StringWriter logWriter;

		@Setter
		private Locale locale;

		@Override
		public void invoke(UserImportData data, AnalysisContext context) {
			try {
				log.debug(context.readRowHolder().getRowIndex() + "/"
						+ context.readSheetHolder().getApproximateTotalRowNumber() + ": " + data.getUserName());
				SysUser u = this.userService
						.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, data.getUserName()));
				if (Objects.isNull(u)) {
					BeanValidators.validateWithException(this.validator, data);
					SysDept dept = this.deptService
							.getOne(new LambdaQueryWrapper<SysDept>().eq(SysDept::getDeptName, data.getDeptName()));
					if (Objects.isNull(dept)) {
						failCount++;
						logWriter.append(SystemUserTips.USER_DEPT_NOT_EXISTS.locale(locale, data.getUserName())).append("<br/>");
						return;
					}
					u = new SysUser();
					u.setDeptId(dept.getDeptId());
					if (StringUtils.isNotEmpty(data.getPostCodes())) {
						Long[] postIds = data.getPostCodes().stream().map(postCode -> {
							SysPost post = this.postService.getPost(postCode);
							return Objects.isNull(post) ? 0L : post.getPostId();
						}).filter(roleId -> roleId != 0).toArray(Long[]::new);
						u.setPostIds(postIds);
					}
					u.setUserName(data.getUserName());
					u.setNickName(data.getNickName());
					u.setPassword(data.getPassword());
					u.setPhoneNumber(data.getPhoneNumber());
					u.setEmail(data.getEmail());
					u.setSex(data.getGender());
					u.setStatus(data.getStatus());
					u.setRemark(data.getRemark());
					u.setPassword(data.getPassword());
					u.setCreateBy(this.operator);
					CreateUserRequest req = new CreateUserRequest();
					BeanUtils.copyProperties(u, req);
					this.userService.insertUser(req);
					if (StringUtils.isNotEmpty(data.getRoleCodes())) {
						for (String roleCode : data.getRoleCodes()) {
							SysRole role = this.roleService.getRole(roleCode);
							if (Objects.nonNull(role)) {
								SysUserRole ur = new SysUserRole();
								ur.setUserId(u.getUserId());
								ur.setRoleId(role.getRoleId());
								this.userRoleMapper.insert(ur);
							}
						}
					}
					successCount++;
				} else if (this.isUpdateSupport) {
					BeanValidators.validateWithException(this.validator, data);
					u.setUserName(data.getUserName());
					u.setNickName(data.getNickName());
					u.setPhoneNumber(data.getPhoneNumber());
					u.setEmail(data.getEmail());
					u.setSex(data.getGender());
					u.setStatus(data.getStatus());
					u.setRemark(data.getRemark());
					u.setPassword(data.getPassword());
					u.setUpdateBy(this.operator);
					UpdateUserRequest req = new UpdateUserRequest();
					BeanUtils.copyProperties(u, req);
					this.userService.updateUser(req);
					successCount++;
				} else {
					failCount++;
					logWriter.append(SystemUserTips.USER_EXISTS.locale(locale, data.getUserName())).append("<br/>");
				}
			} catch (Exception e) {
				failCount++;
				logWriter.append(SystemUserTips.IMPORT_FAIL.locale(locale, data.getUserName(), e.getMessage())).append("<br/>");
			}
		}

		@Override
		public void doAfterAllAnalysed(AnalysisContext context) {
			logWriter.append(SystemUserTips.IMPORT_FAIL.locale(locale, successCount, failCount));
		}

		public void setUpdateSupport(boolean isUpdateSupport) {
			this.isUpdateSupport = isUpdateSupport;
		}
	}
}
