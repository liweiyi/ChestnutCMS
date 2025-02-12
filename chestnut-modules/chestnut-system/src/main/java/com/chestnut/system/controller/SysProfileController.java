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

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IP2RegionUtils;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.config.SystemConfig;
import com.chestnut.system.domain.SysMenu;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.vo.DashboardUserVO;
import com.chestnut.system.domain.vo.ShortcutVO;
import com.chestnut.system.domain.vo.UserProfileVO;
import com.chestnut.system.enums.MenuType;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.*;
import com.chestnut.system.user.preference.ShortcutUserPreference;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 个人信息 业务处理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseRestController {

	private final ISysUserService userService;

	private final ISysDeptService deptService;

	private final ISecurityConfigService securityConfigService;

	private final ISysMenuService menuService;

	private final ISysPermissionService permissionService;

	@Priv(type = AdminUserType.TYPE)
	@GetMapping
	public R<?> profile() {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = (SysUser) loginUser.getUser();
		user.setAvatarSrc(SystemConfig.getResourcePrefix() + user.getAvatar());
		String roleGroup = userService.selectUserRoleGroup(loginUser.getUserId());
		String postGroup = userService.selectUserPostGroup(loginUser.getUserId());
		return R.ok(new UserProfileVO(user, roleGroup, postGroup));
	}

	@Priv(type = AdminUserType.TYPE)
	@PutMapping
	@Log(title = "个人中心", businessType = BusinessType.UPDATE)
	public R<?> updateProfile(@RequestBody SysUser user) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();

		boolean checkPhoneUnique = this.userService.checkPhoneUnique(user.getPhoneNumber(), loginUser.getUserId());
		Assert.isTrue(checkPhoneUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("PhoneNumber"));

		boolean checkEmailUnique = this.userService.checkPhoneUnique(user.getEmail(), loginUser.getUserId());
		Assert.isTrue(checkEmailUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("Email"));

		SysUser sysUser = (SysUser) loginUser.getUser();
		sysUser.setNickName(user.getNickName());
		sysUser.setPhoneNumber(user.getPhoneNumber());
		sysUser.setEmail(user.getEmail());
		sysUser.setSex(user.getSex());

		LambdaUpdateWrapper<SysUser> q = new LambdaUpdateWrapper<SysUser>()
				.set(SysUser::getNickName, user.getNickName()).set(SysUser::getPhoneNumber, user.getPhoneNumber())
				.set(SysUser::getEmail, user.getEmail()).set(SysUser::getSex, user.getSex())
				.eq(SysUser::getUserId, loginUser.getUserId());
		if (this.userService.update(q)) {
			StpAdminUtil.setLoginUser(loginUser);
			return R.ok();
		}
		return R.fail();
	}

	@Priv(type = AdminUserType.TYPE)
	@Log(title = "个人中心", businessType = BusinessType.UPDATE, isSaveRequestData = false)
	@PutMapping("/updatePwd")
	public R<?> updatePwd(String oldPassword, String newPassword) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = userService.getById(loginUser.getUserId());
		if (!SecurityUtils.matches(oldPassword, user.getPassword())) {
			return R.fail("修改密码失败，旧密码错误");
		}
		if (SecurityUtils.matches(newPassword, user.getPassword())) {
			return R.fail("新密码不能与旧密码相同");
		}
		// 密码安全规则校验
		this.securityConfigService.validPassword(user, newPassword);

		boolean update = this.userService.lambdaUpdate()
				.set(SysUser::getPassword, SecurityUtils.passwordEncode(newPassword))
				.set(SysUser::getPasswordModifyTime, LocalDateTime.now())
				.eq(SysUser::getUserId, loginUser.getUserId()).update();
		return update ? R.ok() : R.fail();
	}

	@IgnoreDemoMode
	@Priv(type = AdminUserType.TYPE)
	@Log(title = "个人中心", businessType = BusinessType.UPDATE)
	@PostMapping("/avatar")
	public R<?> avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception {
		if (Objects.isNull(file) || file.isEmpty()) {
			return R.fail("上传图片异常，请联系管理员");
		}
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		String avatar = this.userService.uploadAvatar(loginUser.getUserId(), file);
		// 更新缓存用户头像
		SysUser user = (SysUser) loginUser.getUser();
		user.setAvatar(avatar);
		StpAdminUtil.setLoginUser(loginUser);
		return R.ok(SystemConfig.getResourcePrefix() + avatar);
	}

	/**
	 * 首页用户信息
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/homeInfo")
	public R<?> getHomeInfo() {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = (SysUser) loginUser.getUser();
		DashboardUserVO vo = new DashboardUserVO();
		vo.setUserName(user.getUserName());
		vo.setNickName(user.getNickName());
		vo.setLastLoginTime(user.getLoginDate());
		vo.setLastLoginIp(user.getLoginIp());
		vo.setLastLoginAddr(IP2RegionUtils.ip2Region(user.getLoginIp()));
		if (StringUtils.isNotEmpty(user.getAvatar())) {
			vo.setAvatar(SystemConfig.getResourcePrefix() + user.getAvatar());
		}
		this.deptService.getDept(user.getDeptId()).ifPresent(dept -> vo.setDeptName(dept.getDeptName()));
		return R.ok(vo);
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/shortcuts")
	public R<?> getHomeShortcuts() {
		SysUser user = this.userService.getById(StpAdminUtil.getLoginIdAsLong());
		List<Long> menuIds = ShortcutUserPreference.getValue(user.getPreferences());
		List<SysMenu> allMenus = this.menuService.lambdaQuery().list();

		List<SysMenu> shortcuts = allMenus.stream().filter(m -> menuIds.contains(m.getMenuId())).toList();
		List<String> menuPerms = StpAdminUtil.getLoginUser().getPermissions();
		if (!menuPerms.contains(ISysPermissionService.ALL_PERMISSION)) {
			shortcuts = shortcuts.stream().filter(m -> {
				return StringUtils.isEmpty(m.getPerms()) || menuPerms.contains(m.getPerms());
			}).toList();
		}
		shortcuts.forEach(shortcut -> {
			List<String> paths = new ArrayList<>();
			generateMenuRoute(shortcut, allMenus, paths);
			shortcut.setPath(String.join("/", paths));
		});

		I18nUtils.replaceI18nFields(shortcuts, LocaleContextHolder.getLocale());
		List<ShortcutVO> result = shortcuts.stream()
				.sorted(Comparator.comparingInt(m -> menuIds.indexOf(m.getMenuId())))
				.map(m -> new ShortcutVO(m.getMenuName(), m.getIcon(), m.getPath())).toList();
		return R.ok(result);
	}

	private void generateMenuRoute(SysMenu menu, List<SysMenu> menus, List<String> paths) {
		paths.add(0, menu.getPath());
		if (IdUtils.validate(menu.getParentId())) {
			menus.forEach(m -> {
				if (m.getMenuId().equals(menu.getParentId())) {
					generateMenuRoute(m, menus, paths);
				}
			});
		}
	}
}
