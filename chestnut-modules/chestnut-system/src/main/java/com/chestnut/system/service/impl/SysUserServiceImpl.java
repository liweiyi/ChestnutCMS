package com.chestnut.system.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
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
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.SysPost;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.SysUserRole;
import com.chestnut.system.domain.dto.UserImportData;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.UserStatus;
import com.chestnut.system.mapper.SysPostMapper;
import com.chestnut.system.mapper.SysRoleMapper;
import com.chestnut.system.mapper.SysUserMapper;
import com.chestnut.system.mapper.SysUserRoleMapper;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISecurityConfigService;
import com.chestnut.system.service.ISysDeptService;
import com.chestnut.system.service.ISysPostService;
import com.chestnut.system.service.ISysRoleService;
import com.chestnut.system.service.ISysUserService;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

/**
 * 用户 业务层处理
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

	private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

	private final SysRoleMapper roleMapper;

	private final SysPostMapper postMapper;

	private final SysUserRoleMapper userRoleMapper;

	private final ISecurityConfigService securityConfigService;

	/**
	 * 查询用户所属角色组
	 *
	 * @param userId 用户ID
	 * @return 结果
	 */
	@Override
	public String selectUserRoleGroup(Long userId) {
		List<SysRole> list = roleMapper.selectRolesByUserId(userId);
		if (CollectionUtils.isEmpty(list)) {
			return StringUtils.EMPTY;
		}
		return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
	}

	/**
	 * 查询用户所属岗位组
	 *
	 * @param userId 用户ID
	 * @return 结果
	 */
	@Override
	public String selectUserPostGroup(Long userId) {
		List<SysPost> userPosts = postMapper.selectPostsByUserId(userId);
		if (CollectionUtils.isEmpty(userPosts)) {
			return StringUtils.EMPTY;
		}
		return userPosts.stream().map(SysPost::getPostName).collect(Collectors.joining(","));
	}

	/**
	 * 校验用户名称是否唯一
	 *
	 * @param username 用户名
	 * @param userId 用户ID
	 * @return 结果
	 */
	@Override
	public boolean checkUserNameUnique(String username, Long userId) {
		long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username)
				.ne(IdUtils.validate(userId), SysUser::getUserId, userId));
		return count == 0;
	}

	private boolean checkUserUnique(SysUser user) {
		LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<SysUser>()
				.and(wrapper -> wrapper
						.eq(StringUtils.isNotEmpty(user.getUserName()), SysUser::getUserName, user.getUserName()).or()
						.eq(StringUtils.isNotEmpty(user.getPhoneNumber()), SysUser::getPhoneNumber,
								user.getPhoneNumber())
						.or().eq(StringUtils.isNotEmpty(user.getEmail()), SysUser::getEmail, user.getEmail()))
				.ne(IdUtils.validate(user.getUserId()), SysUser::getUserId, user.getUserId());
		return this.count(q) == 0;
	}

	/**
	 * 校验手机号码是否唯一
	 *
	 * @param phoneNumber 用户电话号码
	 * @param userId 用户ID
	 * @return
	 */
	@Override
	public boolean checkPhoneUnique(String phoneNumber, Long userId) {
		if (StringUtils.isNotEmpty(phoneNumber)) {
			long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhoneNumber, phoneNumber)
					.ne(IdUtils.validate(userId), SysUser::getUserId, userId));
			return count == 0;
		}
		return true;
	}

	/**
	 * 校验email是否唯一
	 *
	 * @param email 用户Email
	 * @param userId 用户ID
	 * @return
	 */
	@Override
	public boolean checkEmailUnique(String email, Long userId) {
		if (StringUtils.isNotEmpty(email)) {
			long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email)
					.ne(IdUtils.validate(userId), SysUser::getUserId, userId));
			return count == 0;
		}
		return true;
	}

	/**
	 * 新增保存用户信息
	 *
	 * @param user 用户信息
	 * @return 结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void insertUser(SysUser user) {
		boolean checkUserUnique = this.checkUserUnique(user);
		Assert.isTrue(checkUserUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("[username,phonenumber,email]"));
		// 校验密码
		this.securityConfigService.validPassword(user, user.getPassword());
		// 强制首次登陆修改密码
		this.securityConfigService.forceModifyPwdAfterUserAdd(user);
		// 密码家吗
		user.setUserId(IdUtils.getSnowflakeId());
		user.setPassword(SecurityUtils.passwordEncode(user.getPassword()));
		user.setCreateTime(LocalDateTime.now());
		this.save(user);
		// 新增用户岗位关联
		insertUserPost(user);
	}

	/**
	 * 注册用户信息
	 *
	 * @param user 用户信息
	 * @return 结果
	 */
	@Override
	public void registerUser(SysUser user) {
		boolean checkUserUnique = this.checkUserUnique(user);
		Assert.isTrue(checkUserUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("[username,phonenumber,email]"));

		this.securityConfigService.validPassword(user, user.getPassword());

		user.setUserId(IdUtils.getSnowflakeId());
		user.setPassword(SecurityUtils.passwordEncode(user.getPassword()));
		user.setCreateTime(LocalDateTime.now());
		this.save(user);
	}

	/**
	 * 修改保存用户信息
	 *
	 * @param user 用户信息
	 * @return 结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateUser(SysUser user) {
		SysUser db = this.getById(user.getUserId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(user.getUserId()));
		boolean checkUserUnique = this.checkUserUnique(user);
		Assert.isTrue(checkUserUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("[username,phonenumber,email]"));

		String oldStatus = db.getStatus();

		db.setNickName(user.getNickName());
		db.setDeptId(user.getDeptId());
		db.setPhoneNumber(user.getPhoneNumber());
		db.setEmail(user.getEmail());
		db.setSex(user.getSex());
		db.setStatus(user.getStatus());
		db.setPostIds(user.getPostIds());
		db.setRoleIds(user.getRoleIds());
		db.setRemark(user.getRemark());
		db.updateBy(user.getUpdateBy());
		this.updateById(db);
		// 用户与岗位关联
		postMapper.deleteUserPost(db.getUserId());
		insertUserPost(user);
		// 变更未封禁或锁定状态时注销登录状态
		if (!StringUtils.equals(db.getStatus(), oldStatus)
				&& (UserStatus.isDisbale(db.getStatus()) || UserStatus.isLocked(db.getStatus()))) {
			StpAdminUtil.logout(user.getUserId());
		}
	}

	/**
	 * 用户授权角色
	 *
	 * @param userId  用户ID
	 * @param roleIds 角色组
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void insertUserAuth(final Long userId, final List<Long> roleIds) {
		userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
		if (StringUtils.isNotEmpty(roleIds)) {
			List<SysUserRole> list = roleIds.stream().map(roleId -> new SysUserRole(userId, roleId)).toList();
			userRoleMapper.batchInserUserRoles(list);
		}
	}

	/**
	 * 重置用户密码
	 *
	 * @param user 用户信息
	 * @return 结果
	 */
	@Override
	public void resetPwd(SysUser user) {
		SysUser db = this.getById(user.getUserId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("userId", user.getUserId()));

		this.securityConfigService.validPassword(db, user.getPassword());

		db.setPassword(SecurityUtils.passwordEncode(user.getPassword()));
		db.setUpdateTime(LocalDateTime.now());
		db.setUpdateBy(user.getUpdateBy());
		this.updateById(db);
	}

	/**
	 * 新增用户岗位信息
	 *
	 * @param user 用户对象
	 */
	public void insertUserPost(SysUser user) {
		Long[] posts = user.getPostIds();
		if (StringUtils.isNotEmpty(posts)) {
			// 新增用户与岗位管理
			for (Long postId : posts) {
				postMapper.insertUserPost(user.getUserId(), postId);
			}
		}
	}

	/**
	 * 批量删除用户信息
	 *
	 * @param userIds 需要删除的用户ID
	 * @return 结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteUserByIds(List<Long> userIds) {
		for (Long userId : userIds) {
			Assert.isFalse(SecurityUtils.isSuperAdmin(userId), SysErrorCode.SUPERADMIN_DELETE::exception);
		}
		// 删除用户与角色关联
		userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
		// 删除用户与岗位关联
		postMapper.deleteUserPost(userIds.stream().toArray(Long[]::new));
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

		private Validator validator;

		private boolean isUpdateSupport;

		private String operator;

		private int successCount;

		private int failCount;

		private StringWriter logWriter;

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
						logWriter.append("账号 " + data.getUserName() + " 部门不存在。<br/>");
						return;
					}
					u = new SysUser();
					u.setDeptId(dept.getDeptId());
					if (StringUtils.isNotEmpty(data.getRoleCodes())) {
						Long[] roleIds = data.getRoleCodes().stream().map(roleCode -> {
							SysRole role = this.roleService.getRole(roleCode);
							return Objects.isNull(role) ? 0L : role.getRoleId();
						}).filter(roleId -> roleId != 0).toArray(Long[]::new);
						u.setRoleIds(roleIds);
					}
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
					this.userService.insertUser(u);
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
					this.userService.updateUser(u);
					successCount++;
				} else {
					failCount++;
					logWriter.append("账号 " + data.getUserName() + " 已存在。<br/>");
				}
			} catch (Exception e) {
				failCount++;
				logWriter.append("账号 " + data.getUserName() + " 导入失败：" + e.getMessage() + "<br/>");
			}
		}

		@Override
		public void doAfterAllAnalysed(AnalysisContext context) {
			logWriter.append("导入完成，成功数：" + successCount + "，失败数：" + failCount);
		}

		public void setOperator(String operator) {
			this.operator = operator;
		}

		public void setValidator(Validator validator) {
			this.validator = validator;
		}

		public void setUpdateSupport(boolean isUpdateSupport) {
			this.isUpdateSupport = isUpdateSupport;
		}

		public void setLogWriter(StringWriter logWriter) {
			this.logWriter = logWriter;
		}
	};
}
