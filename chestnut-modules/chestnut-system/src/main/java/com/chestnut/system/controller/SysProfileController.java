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

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.IP2RegionUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.config.SystemConfig;
import com.chestnut.system.domain.*;
import com.chestnut.system.domain.dto.UpdateUserProfileRequest;
import com.chestnut.system.domain.vo.DashboardUserVO;
import com.chestnut.system.domain.vo.ShortcutVO;
import com.chestnut.system.domain.vo.UserProfileVO;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.*;
import com.chestnut.system.service.impl.UserPermissionService;
import com.chestnut.system.user.preference.MenuShortcutUserPreference;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 个人信息 业务处理
 */
@XComment("{API.DOC.SYS.PROFILE.MODULE}")
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseRestController {

	private final ISysDeptService deptService;

	private final ISysUserService userService;

	private final ISysRoleService roleService;

	private final ISysPostService postService;

	private final UserPermissionService userPermissionService;

	private final ISecurityConfigService securityConfigService;

	private final ISysMenuService menuService;
	@XComment("{API.DOC.SYS.PROFILE.GET_INFO}")
	@GetMapping
	public R<UserProfileVO> profile() {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = (SysUser) loginUser.getUser();
		user.setAvatarSrc(SystemConfig.getResourcePrefix() + user.getAvatar());

		List<SysRole> roles = this.roleService.selectRolesByUserId(loginUser.getUserId());
		String roleGroup = roles.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
		List<SysPost> posts = postService.selectPostsByUserId(loginUser.getUserId());
		String postGroup = posts.stream().map(SysPost::getPostName).collect(Collectors.joining(","));
		return R.ok(new UserProfileVO(user, roleGroup, postGroup));
	}
	@XComment("{API.DOC.SYS.PROFILE.UPDATE_INFO}")
	@PostMapping("/updateInfo")
	@Transactional(rollbackFor = Exception.class)
	@Log(title = "个人中心", businessType = BusinessType.UPDATE)
	public R<?> updateProfile(@RequestBody @Validated UpdateUserProfileRequest req) {
		this.userService.updateUserProfile(req);
		// 更新用户token数据
		this.userPermissionService.resetLoginUser(req.getOperator().getUserId());
		return R.ok();
	}
	@XComment("{API.DOC.SYS.PROFILE.UPDATE_PWD}")
	@Log(title = "个人中心", businessType = BusinessType.UPDATE, isSaveRequestData = false)
	@PostMapping("/updatePwd")
	public R<?> updatePwd(
			@XComment("{API.DOC.SYS.PROFILE.OLD_PASSWORD}") String oldPassword,
			@XComment("{API.DOC.SYS.PROFILE.NEW_PASSWORD}") String newPassword) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = userService.getById(loginUser.getUserId());
        // 三方登录可能没有旧密码
        if (StringUtils.isNotEmpty(user.getPassword())) {
            if (!SecurityUtils.matches(oldPassword, user.getPassword())) {
                return R.fail("修改密码失败，旧密码错误");
            }
            if (SecurityUtils.matches(newPassword, user.getPassword())) {
                return R.fail("新密码不能与旧密码相同");
            }
        }
		// 密码安全规则校验
		this.securityConfigService.validPassword(user, newPassword);
        user.setPassword(SecurityUtils.passwordEncode(newPassword));
        user.setPasswordModifyTime(LocalDateTime.now());
        if (YesOrNo.isYes(user.getForceModifyPassword())) {
            user.setForceModifyPassword(YesOrNo.NO);
        }
        this.userService.updateById(user);
        loginUser.setUser(user);
        StpAdminUtil.setLoginUser(loginUser);
        return R.ok();
	}
	@XComment("{API.DOC.SYS.PROFILE.UPLOAD_AVATAR}")
	@IgnoreDemoMode
	@Log(title = "个人中心", businessType = BusinessType.UPDATE)
	@PostMapping("/avatar")
	public R<String> avatar(@RequestParam("avatarfile") @XComment("{API.DOC.SYS.PROFILE.AVATAR_FILE}") MultipartFile file) throws Exception {
		if (Objects.isNull(file) || file.isEmpty()) {
			return R.fail("上传图片异常，请联系管理员");
		}
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		String avatarPath = this.userService.uploadAvatar(loginUser.getUserId(), file.getBytes());
        // 更新数据库
        this.userService.lambdaUpdate().set(SysUser::getAvatar, avatarPath)
                .eq(SysUser::getUserId, loginUser.getUserId()).update();
        // 更新缓存用户头像
		SysUser user = (SysUser) loginUser.getUser();
		user.setAvatar(avatarPath);
		StpAdminUtil.setLoginUser(loginUser);
		return R.ok(SystemConfig.getResourcePrefix() + avatarPath);
	}

	/**
	 * 首页用户信息
	 */
	@XComment("{API.DOC.SYS.PROFILE.GET_HOME_INFO}")
	@GetMapping("/homeInfo")
	public R<DashboardUserVO> getHomeInfo() {
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
		SysDept dept = this.deptService.getDept(user.getDeptId());
		if (Objects.nonNull(dept)) {
			vo.setDeptName(dept.getDeptName());
		}
		return R.ok(vo);
	}
	@XComment("{API.DOC.SYS.PROFILE.GET_SHORTCUTS}")
	@GetMapping("/shortcuts")
	public R<List<ShortcutVO>> getHomeShortcuts() {
		SysUser user = this.userService.getById(StpAdminUtil.getLoginIdAsLong());
		List<String> menuIds = MenuShortcutUserPreference.getValue(user.getPreferences());
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
		if (!SysConstants.MENU_ROOT_ID.equals(menu.getParentId())) {
			menus.forEach(m -> {
				if (m.getMenuId().equals(menu.getParentId())) {
					generateMenuRoute(m, menus, paths);
				}
			});
		}
	}
}
