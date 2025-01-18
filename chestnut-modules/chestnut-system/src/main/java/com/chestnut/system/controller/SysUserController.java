/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.system.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.extend.annotation.XssIgnore;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.ExcelExportable;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.config.SystemConfig;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.SysPost;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.dto.AuthRoleDTO;
import com.chestnut.system.domain.dto.UserImportData;
import com.chestnut.system.domain.vo.UserInfoVO;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.mapper.SysUserRoleMapper;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysDeptService;
import com.chestnut.system.service.ISysPostService;
import com.chestnut.system.service.ISysRoleService;
import com.chestnut.system.service.ISysUserService;
import com.chestnut.system.service.impl.SysUserServiceImpl.SysUserReadListener;
import com.chestnut.system.user.preference.IUserPreference;
import com.chestnut.system.validator.LongId;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class SysUserController extends BaseRestController {

	private final ISysUserService userService;

	private final ISysRoleService roleService;

	private final ISysDeptService deptService;

	private final ISysPostService postService;

	private final SysUserRoleMapper userRoleMapper;

	protected final Validator validator;

	/**
	 * 获取用户列表
	 */
	@ExcelExportable(SysUser.class)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserList)
	@GetMapping("/list")
	public R<?> list(SysUser user) {
		PageRequest pr = this.getPageRequest();
		Page<SysUser> page = userService.lambdaQuery()
				.like(StringUtils.isNotEmpty(user.getUserName()), SysUser::getUserName, user.getUserName())
				.like(StringUtils.isNotEmpty(user.getPhoneNumber()), SysUser::getPhoneNumber, user.getPhoneNumber())
				.eq(IdUtils.validate(user.getDeptId()), SysUser::getDeptId, user.getDeptId())
				.eq(Objects.nonNull(user.getStatus()), SysUser::getStatus, user.getStatus())
				.orderByDesc(SysUser::getUserId).page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
		page.getRecords().forEach(u -> {
			this.deptService.getDept(u.getDeptId()).ifPresent(d -> u.setDeptName(d.getDeptName()));
		});
		return bindDataTable(page);
	}

	@Log(title = "用户管理", businessType = BusinessType.IMPORT)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserAdd)
	@PostMapping("/importData")
	public R<String> importData(MultipartFile file, boolean updateSupport) throws Exception {
		if (Objects.isNull(file) || file.isEmpty()) {
			return R.fail("Import file not exists!");
		}
		StringWriter logWriter = new StringWriter();
		SysUserReadListener readListener = new SysUserReadListener(this.userService, this.deptService, this.roleService,
				this.postService, this.userRoleMapper);
		readListener.setValidator(validator);
		readListener.setOperator(StpAdminUtil.getLoginUser().getUsername());
		readListener.setUpdateSupport(updateSupport);
		readListener.setLogWriter(logWriter);
		EasyExcel.read(file.getInputStream(), UserImportData.class, readListener)
				.locale(LocaleContextHolder.getLocale()).doReadAll();
		return R.ok(logWriter.toString());
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserAdd)
	@PostMapping("/importTemplate")
	public void importTemplate(HttpServletResponse response) {
		exportExcel(List.of(), UserImportData.class, response);
	}

	/**
	 * 根据用户ID获取详细信息
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserList)
	@GetMapping(value = { "/", "/{userId}" })
	public R<?> getInfo(@PathVariable(value = "userId", required = false) Long userId) {
		List<SysPost> posts = postService.list();
		SysUser user = null;
		if (IdUtils.validate(userId)) {
			user = userService.getById(userId);
			user.setAvatarSrc(SystemConfig.getResourcePrefix() + user.getAvatar());
			user.setRoleIds(
					roleService.selectRolesByUserId(userId, EnableOrDisable.ENABLE)
							.stream().map(SysRole::getRoleId).toArray(Long[]::new)
			);
			user.setPostIds(
					postService.selectPostListByUserId(userId).stream().map(SysPost::getPostId).toArray(Long[]::new));
		}
		return R.ok(UserInfoVO.builder().posts(posts).user(user).build());
	}

	/**
	 * 新增用户
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserAdd)
	@Log(title = "用户管理", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysUser user) {
		user.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		userService.insertUser(user);
		return R.ok();
	}

	/**
	 * 修改用户
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserEdit)
	@Log(title = "用户管理", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysUser user) {
		user.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		userService.updateUser(user);
		return R.ok();
	}

	/**
	 * 删除用户
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserRemove)
	@Log(title = "用户管理", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> userIds) {
		userService.deleteUserByIds(userIds);
		return R.ok();
	}

	/**
	 * 重置密码
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserResetPwd)
	@Log(title = "用户管理", businessType = BusinessType.UPDATE, isSaveRequestData = false)
	@PutMapping("/resetPwd")
	public R<?> resetPwd(@RequestBody SysUser user) {
		userService.resetPwd(user);
		return R.ok();
	}

	/**
	 * 根据用户编号获取授权角色
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserList)
	@GetMapping("/authRole/{userId}")
	public R<?> authRole(@PathVariable("userId") @LongId Long userId) {
		SysUser user = userService.getById(userId);
		Assert.notNull(user, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(userId));

		List<SysRole> userRoles = roleService.selectRolesByUserId(userId, EnableOrDisable.ENABLE);
		user.setRoleIds(userRoles.stream().map(SysRole::getRoleId).toArray(Long[]::new));
		List<SysRole> roles = this.roleService.list();
		return R.ok(UserInfoVO.builder().user(user).roles(roles).build());
	}

	/**
	 * 用户授权角色
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserEdit)
	@Log(title = "用户管理", businessType = BusinessType.GRANT)
	@PutMapping("/authRole")
	public R<?> insertAuthRole(@Validated @RequestBody AuthRoleDTO dto) {
		userService.insertUserAuth(dto.getUserId(), dto.getRoleIds());
		return R.ok();
	}

	/**
	 * 获取部门树列表
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserList)
	@GetMapping("/deptTree")
	public R<?> deptTree(SysDept dept) {
		List<SysDept> depts = this.deptService.list(new LambdaQueryWrapper<SysDept>()
				.like(StringUtils.isNotEmpty(dept.getDeptName()), SysDept::getDeptName, dept.getDeptName()));
		return R.ok(deptService.buildDeptTreeSelect(depts));
	}

	private final List<IUserPreference> userPreferenceList;

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/getPreferences")
	public R<?> getPreferences() {
		SysUser user = this.userService.getById(StpAdminUtil.getLoginIdAsLong());
		return R.ok(Objects.isNull(user.getPreferences()) ? Map.of() : user.getPreferences());
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/preference")
	public R<?> getUserPreference(@RequestParam("id") @NotEmpty String id) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = (SysUser) loginUser.getUser();
		Optional<IUserPreference> findFirst = this.userPreferenceList.stream().filter(up -> up.getId().equals(id))
				.findFirst();
		if (findFirst.isEmpty()) {
			return R.fail();
		}
		Object value = findFirst.get().getDefaultValue();
		if (user.getPreferences() != null) {
			value = user.getPreferences().getOrDefault(id, findFirst.get().getDefaultValue());
		}
		return R.ok(value);
	}

	@XssIgnore
	@Priv(type = AdminUserType.TYPE)
	@PutMapping("/savePreferences")
	public R<?> saveUserPreferences(@RequestBody @NotNull Map<String, Object> userPreferences) {
		SysUser user = this.userService.getById(StpAdminUtil.getLoginIdAsLong());
		Map<String, Object> map = this.userPreferenceList.stream().collect(Collectors.toMap(IUserPreference::getId,
				up -> userPreferences.getOrDefault(up.getId(), up.getDefaultValue())));
		user.setPreferences(map);
		this.userService.updateById(user);
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		loginUser.setUser(user);
		StpAdminUtil.setLoginUser(loginUser);
		return R.ok();
	}
}
