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
package com.chestnut.system.controller;

import cn.idev.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;

import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.ExcelExportable;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.config.SystemConfig;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.SysPost;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.dto.*;
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
import com.chestnut.system.user.preference.MenuShortcutUserPreference;
import com.chestnut.system.validator.LongId;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
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
@XComment("{API.DOC.SYS.USER.MODULE}")
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

	private final List<IUserPreference> userPreferenceList;

	/**
	 * 获取用户列表
	 */
    @XComment("{API.DOC.SYS.USER.GET_LIST}")
	@ExcelExportable(SysUser.class)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserList)
	@GetMapping("/list")
	public R<TableData<SysUser>> list(@Validated QueryUserRequest user) {
		PageRequest pr = this.getPageRequest();
		Page<SysUser> page = userService.lambdaQuery()
				.like(StringUtils.isNotEmpty(user.getUserName()), SysUser::getUserName, user.getUserName())
				.like(StringUtils.isNotEmpty(user.getPhoneNumber()), SysUser::getPhoneNumber, user.getPhoneNumber())
				.eq(IdUtils.validate(user.getDeptId()), SysUser::getDeptId, user.getDeptId())
				.eq(Objects.nonNull(user.getStatus()), SysUser::getStatus, user.getStatus())
				.orderByDesc(SysUser::getUserId).page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
		page.getRecords().forEach(u -> {
			this.deptService.getDept(u.getDeptId()).ifPresent(d -> u.setDeptName(d.getDeptName()));
            u.setPassword("******");
		});
		return bindDataTable(page);
	}

    @XComment("{API.DOC.SYS.USER.IMPORT}")
	@Log(title = "用户管理", businessType = BusinessType.IMPORT)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserAdd)
	@PostMapping("/importData")
	public R<String> importData(
            @XComment("{API.DOC.SYS.USER.IMPORT.FILE}") MultipartFile file,
            @XComment("{API.DOC.SYS.USER.IMPORT.UPDATE_SUPPORT}") boolean updateSupport
    ) throws Exception {
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
		readListener.setLocale(LocaleContextHolder.getLocale());
		EasyExcel.read(file.getInputStream(), UserImportData.class, readListener)
				.locale(LocaleContextHolder.getLocale()).doReadAll();
		return R.ok(logWriter.toString());
	}

    @XComment("{API.DOC.SYS.USER.DOWNLOAD_IMPORT_TEMPLATE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserAdd)
	@PostMapping("/importTemplate")
	public void importTemplate(HttpServletResponse response) {
		exportExcel(List.of(), UserImportData.class, response);
	}

	/**
	 * 根据用户ID获取详细信息
	 */
    @XComment("{API.DOC.SYS.USER.GET_INFO}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserList)
	@GetMapping(value = { "/detail", "/detail/{userId}" })
	public R<UserInfoVO> getInfo(@PathVariable(value = "userId", required = false) @XComment("{API.DOC.SYS.USER.ID}") Long userId) {
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
		return R.ok(UserInfoVO.create(user));
	}

	/**
	 * 新增用户
	 */
	@XComment("{API.DOC.SYS.USER.CREATE_USER}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserAdd)
	@Log(title = "用户管理", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<Void> add(@Validated @RequestBody CreateUserRequest user) {
		userService.insertUser(user);
		return R.ok();
	}

	/**
	 * 修改用户
	 */
	@XComment("{API.DOC.SYS.USER.UPDATE_USER}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserEdit)
	@Log(title = "用户管理", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<Void> edit(@Validated @RequestBody UpdateUserRequest user) {
		userService.updateUser(user);
		return R.ok();
	}

	/**
	 * 删除用户
	 */
    @XComment("{API.DOC.SYS.USER.DELETE_USER}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserRemove)
	@Log(title = "用户管理", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<Void> remove(@RequestBody @NotEmpty @XComment("{API.DOC.SYS.USER.IDS}") List<Long> userIds) {
		userService.deleteUserByIds(userIds);
		return R.ok();
	}

    @XComment("{API.DOC.SYS.USER.RESET_PWD}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserResetPwd)
	@Log(title = "用户管理", businessType = BusinessType.UPDATE, isSaveRequestData = false)
	@PostMapping("/resetPwd")
	public R<Void> resetPwd(@RequestBody @Validated ResetUserPwdRequest req) {
		userService.resetPwd(req);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.USER.USER_ROLES}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserList)
	@GetMapping("/authRole/{userId}")
	public R<UserInfoVO> authRole(@PathVariable @LongId @XComment("{API.DOC.SYS.USER.ID}") Long userId) {
		SysUser user = userService.getById(userId);
		Assert.notNull(user, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(userId));

		List<SysRole> userRoles = roleService.selectRolesByUserId(userId, EnableOrDisable.ENABLE);
		user.setRoleIds(userRoles.stream().map(SysRole::getRoleId).toArray(Long[]::new));
		List<SysRole> roles = this.roleService.list();
		return R.ok(new UserInfoVO(user, roles));
	}

    @XComment("{API.DOC.SYS.USER.SAVE_USER_ROLES}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserGrant)
	@Log(title = "用户管理", businessType = BusinessType.GRANT)
	@PostMapping("/authRole")
	public R<Void> insertAuthRole(@Validated @RequestBody AuthRoleRequest req) {
		userService.insertUserAuth(req.getUserId(), req.getRoleIds());
		return R.ok();
	}

    @XComment("{API.DOC.SYS.USER.GET_DETP_TREE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserList)
	@GetMapping("/deptTree")
	public R<List<TreeNode<Long>>> deptTree(@Validated QueryDeptRequest req) {
		List<SysDept> depts = this.deptService.list(new LambdaQueryWrapper<SysDept>()
				.like(StringUtils.isNotEmpty(req.getDeptName()), SysDept::getDeptName, req.getDeptName()));
		return R.ok(deptService.buildDeptTreeSelect(depts));
	}

    @XComment("{API.DOC.SYS.USER.GET_PREFERENCES}")
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/getPreferences")
	public R<Map<String, Object>> getPreferences() {
		SysUser user = this.userService.getById(StpAdminUtil.getLoginIdAsLong());
		if (Objects.isNull(user.getPreferences())) {
			return R.ok(Map.of());
		}
		return R.ok(user.getPreferences());
	}

    @XComment("{API.DOC.SYS.USER.GET_PREFERENCE}")
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/preference")
	public R<Object> getUserPreference(@RequestParam("id") @NotBlank @XComment("{API.DOC.SYS.USER.PREFERENCE_ID}") String id) {
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

    @XComment("{API.DOC.SYS.USER.SAVE_PREFERENCES}")
	@Priv(type = AdminUserType.TYPE)
	@PostMapping("/savePreferences")
	public R<Void> saveUserPreferences(@RequestBody @NotNull Map<String, Object> userPreferences) {
		SysUser user = this.userService.getById(StpAdminUtil.getLoginIdAsLong());
		Map<String, Object> map = this.userPreferenceList.stream().collect(Collectors.toMap(IUserPreference::getId,
				up -> userPreferences.getOrDefault(up.getId(), up.getDefaultValue())));
		MenuShortcutUserPreference.removeOldShortcut(map);
		user.setPreferences(map);
		this.userService.updateById(user);
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		loginUser.setUser(user);
		StpAdminUtil.setLoginUser(loginUser);
		return R.ok();
	}
}