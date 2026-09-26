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
package com.chestnut.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.*;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.config.SystemConfig;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.SysUserPost;
import com.chestnut.system.domain.SysUserRole;
import com.chestnut.system.domain.dto.*;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.fixed.dict.Gender;
import com.chestnut.system.fixed.dict.UserStatus;
import com.chestnut.system.listener.event.AfterSysUserAddEvent;
import com.chestnut.system.listener.event.AfterSysUserUpdateEvent;
import com.chestnut.system.mapper.SysUserMapper;
import com.chestnut.system.mapper.SysUserPostMapper;
import com.chestnut.system.mapper.SysUserRoleMapper;
import com.chestnut.system.permission.impl.UserPermissionOwnerType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISecurityConfigService;
import com.chestnut.system.service.ISysDeptService;
import com.chestnut.system.service.ISysPermissionService;
import com.chestnut.system.service.ISysUserService;
import com.chestnut.system.utils.SysDeptUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * 用户 业务层处理
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

	private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

	private final SysUserPostMapper userPostMapper;

	private final SysUserRoleMapper userRoleMapper;

	private final ISecurityConfigService securityConfigService;

	private final ISysDeptService deptService;

	private final ISysPermissionService permissionService;

    private final ApplicationContext applicationContext;

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
	public void updateUserProfile(UpdateUserProfileRequest req) {
		LoginUser loginUser = req.getOperator();

		boolean checkPhoneUnique = this.checkPhoneUnique(req.getPhoneNumber(), loginUser.getUserId());
		Assert.isTrue(checkPhoneUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("PhoneNumber"));

		boolean checkEmailUnique = this.checkEmailUnique(req.getEmail(), loginUser.getUserId());
		Assert.isTrue(checkEmailUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("Email"));

		LambdaUpdateWrapper<SysUser> q = new LambdaUpdateWrapper<SysUser>()
				.set(SysUser::getNickName, req.getNickName())
				.set(SysUser::getRealName, req.getRealName())
				.set(SysUser::getPhoneNumber, req.getPhoneNumber())
				.set(SysUser::getEmail, req.getEmail())
				.set(SysUser::getSex, req.getSex())
				.set(SysUser::getBirthday, req.getBirthday())
				.eq(SysUser::getUserId, loginUser.getUserId());
		this.update(q);
		// 角色关联荣誉字段更新
		new LambdaUpdateChainWrapper<>(userRoleMapper)
				.set(SysUserRole::getNickName, req.getNickName())
				.set(SysUserRole::getPhoneNumber, req.getPhoneNumber())
				.set(SysUserRole::getEmail, req.getEmail())
				.set(SysUserRole::getRealName, req.getRealName())
				.eq(SysUserRole::getUserId, loginUser.getUserId())
				.update();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public SysUser insertUser(CreateUserRequest req) {
		SysDept dept = this.deptService.getDept(req.getDeptId());
		Assert.notNull(dept, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("deptId", req.getDeptId()));
		SysDeptUtils.checkDeptScope(req.getOperator(), dept::getDeptId, dept::getAncestors);

		boolean checkUserUnique = this.checkUserUnique(req.getUserName(), req.getPhoneNumber(), req.getEmail(), null);
		Assert.isTrue(checkUserUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("[username,phoneNumber,email]"));
		// 校验密码
		SysUser user = new SysUser();
		BeanUtils.copyProperties(req, user);
		this.securityConfigService.validPassword(user, req.getPassword());
		// 强制首次登陆修改密码
		this.securityConfigService.forceModifyPwdAfterUserAdd(user);
		user.setUserId(IdUtils.getSnowflakeId());
		user.setDeptId(dept.getDeptId());
		user.setDeptAncestors(dept.getAncestors());
		user.setPassword(SecurityUtils.passwordEncode(req.getPassword()));
		user.createBy(req.getOperator().getUsername());
		this.save(user);
		// 新增用户岗位关联
		syncUserPost(user.getUserId(), req.getPostIds(), false);
        applicationContext.publishEvent(new AfterSysUserAddEvent(this, user));
        return user;
	}

    @Override
    public SysUser createBindingUser(CreateBindingUserRequest req) {
        // 获取微信用户信息
        SysUser user = new SysUser();
        user.setUserId(IdUtils.getSnowflakeId());
        user.setUserName(req.getUserName());
        user.setNickName(req.getNickName());
        if (StringUtils.isNotEmpty(req.getAvatar())) {
            try {
                // 保存头像图片到本地
                byte[] bytes  = HttpUtils.syncDownload(req.getAvatar(), true);
                String avatar = this.uploadAvatar(user.getUserId(), bytes);
                user.setAvatar(avatar);
            } catch (Exception e) {
                // DO NOTHING
                log.warn("Download avatar failed.", e);
            }
        }
        user.setSex(StringUtils.defaultIfEmpty(req.getSex(), Gender.UNKNOWN));
        // 默认顶级机构
        SysDept topDept = this.deptService.getTopDept();
        user.setDeptId(topDept.getDeptId());
		user.setDeptAncestors(topDept.getAncestors());
        user.setStatus(EnableOrDisable.ENABLE);
        user.createBy(SysConstants.SYS_OPERATOR);
        this.save(user);
        applicationContext.publishEvent(new AfterSysUserAddEvent(this, user));
        return user;
    }

	@Override
	public void registerUser(SysUser user) {
		boolean checkUserUnique = this.checkUserUnique(user.getUserName(), user.getPhoneNumber(), user.getEmail(), null);
		Assert.isTrue(checkUserUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("[username,phoneNumber,email]"));

		this.securityConfigService.validPassword(user, user.getPassword());

		user.setUserId(IdUtils.getSnowflakeId());
		user.setPassword(SecurityUtils.passwordEncode(user.getPassword()));
		// 默认顶级机构
		SysDept topDept = this.deptService.getTopDept();
		user.setDeptId(topDept.getDeptId());
		user.setDeptAncestors(topDept.getAncestors());
		user.setStatus(EnableOrDisable.ENABLE);
		user.createBy(SysConstants.SYS_OPERATOR);
		this.save(user);
		applicationContext.publishEvent(new AfterSysUserAddEvent(this, user));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public SysUser updateUser(UpdateUserRequest req) {
		SysUser db = this.getById(req.getUserId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(req.getUserId()));
		boolean checkUserUnique = this.checkUserUnique(db.getUserName(), req.getPhoneNumber(), req.getEmail(), req.getUserId());
		Assert.isTrue(checkUserUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("[username,phoneNumber,email]"));
		// 校验机构范围
		SysDeptUtils.checkDeptScope(req.getOperator(), db::getDeptId, db::getDeptAncestors);

		String oldStatus = db.getStatus();
		boolean isBasicInfoChanged = !Strings.CS.equals(db.getNickName(), req.getNickName())
				|| !Strings.CS.equals(db.getPhoneNumber(), req.getPhoneNumber())
				|| !Strings.CS.equals(db.getEmail(), req.getEmail())
				|| !Strings.CS.equals(db.getRealName(), req.getRealName());
		this.lambdaUpdate()
				.set(SysUser::getNickName, req.getNickName())
				.set(SysUser::getPhoneNumber, req.getPhoneNumber())
				.set(SysUser::getEmail, req.getEmail())
				.set(SysUser::getRealName, req.getRealName())
				.set(SysUser::getStatus, req.getStatus())
				.set(SysUser::getSex, req.getSex())
				.set(SysUser::getBirthday, req.getBirthday())
				.set(SysUser::getPostIds, req.getPostIds())
				.set(SysUser::getRemark, req.getRemark())
				.set(SysUser::getUpdateBy, req.getOperator().getUser())
				.set(SysUser::getUpdateTime, LocalDateTime.now())
				.eq(SysUser::getUserId, db.getUserId())
				.update();
		// 用户与岗位关联
		syncUserPost(db.getUserId(), req.getPostIds(), true);
		// 变更未封禁或锁定状态时注销登录状态
		if (!Strings.CS.equals(req.getStatus(), oldStatus)
				&& (UserStatus.isDisable(req.getStatus()) || UserStatus.isLocked(req.getStatus()))) {
			StpAdminUtil.logout(req.getUserId());
		}
		// 更新角色关联冗余字段信息
		if (isBasicInfoChanged) {
			new LambdaUpdateChainWrapper<>(userRoleMapper)
					.set(SysUserRole::getNickName, req.getNickName())
					.set(SysUserRole::getPhoneNumber, req.getPhoneNumber())
					.set(SysUserRole::getEmail, req.getEmail())
					.set(SysUserRole::getRealName, req.getRealName())
					.eq(SysUserRole::getUserId, db.getUserId())
					.update();
		}
		this.applicationContext.publishEvent(new AfterSysUserUpdateEvent(this, db));
		return db;
	}

	@Override
    @Transactional(rollbackFor = Exception.class)
	public void resetPwd(ResetUserPwdRequest req) {
		SysUser db = this.getById(req.getUserId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(req.getUserId()));
		Assert.isFalse(SecurityUtils.isSuperAdmin(db.getUserId()), SysErrorCode.SUPERADMIN_RESET_PWD::exception);
		// 校验机构范围
		SysDeptUtils.checkDeptScope(req.getOperator(), db::getDeptId, db::getDeptAncestors);
		// 密码策略校验
		this.securityConfigService.validPassword(db, req.getPassword());
		this.securityConfigService.forceModifyPwdAfterResetPwd(db);

		String password = SecurityUtils.passwordEncode(req.getPassword());
		this.lambdaUpdate().set(SysUser::getPassword, password).eq(SysUser::getUserId, db.getUserId()).update();
		// 注销用户登录状态
		StpAdminUtil.logout(db.getUserId());
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
	public void deleteUserByIds(List<Long> userIds, LoginUser operator) {
		// 超管账户校验
		Assert.isFalse(userIds.contains(SecurityUtils.SUPER_ADMIN_UID), SysErrorCode.SUPERADMIN_DELETE::exception);

		SysUser operatorUser = this.getById(operator.getUserId());
		List<SysUser> users = this.listByIds(userIds);
		for (SysUser user : users) {
			// 部门范围校验
			SysDept dept = this.deptService.getDept(user.getDeptId());
			SysDeptUtils.checkDeptScope(operatorUser, dept::getDeptId, dept::getAncestors);
			// 注销已登录token
			StpAdminUtil.logout(user.getUserId());
		}
		// 删除用户与角色关联
		userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
		// 删除用户与岗位关联
		userPostMapper.delete(new LambdaQueryWrapper<SysUserPost>().in(SysUserPost::getUserId, userIds));
		// 删除用户权限配置
		this.permissionService.removePermissions(UserPermissionOwnerType.TYPE, userIds.stream().map(Object::toString).toList());
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
	public String uploadAvatar(Long userId, byte[] fileBytes) {
		try {
			// 文件后缀名
			String ext = FileExUtils.getExtension(fileBytes);
			// 上传相对路径
			String path = SysConstants.USER_AVATAR_PATH + DateUtils.datePath() + "/" + userId + "." + ext;
			// 写入文件
			FileUtils.writeByteArrayToFile(new File(SystemConfig.getPublicFileUploadDir() + path), fileBytes);
			return path;
		} catch (IOException e) {
			throw CommonErrorCode.SYSTEM_ERROR.exception(e.getMessage());
		}
	}


}
