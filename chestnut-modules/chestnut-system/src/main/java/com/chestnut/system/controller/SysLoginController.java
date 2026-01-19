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

import com.chestnut.common.domain.R;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.config.SystemConfig;
import com.chestnut.system.domain.SysLoginConfig;
import com.chestnut.system.domain.SysMenu;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.dto.LoginBody;
import com.chestnut.system.domain.vo.LoginConfig;
import com.chestnut.system.domain.vo.LoginUserInfoVO;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.LoginLogType;
import com.chestnut.system.fixed.dict.SuccessOrFail;
import com.chestnut.system.fixed.dict.UserStatus;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.security.SysLoginService;
import com.chestnut.system.service.*;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 登录验证
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SysLoginController extends BaseRestController {

	private final SysLoginService loginService;

	private final ISysMenuService menuService;

	private final ISysRoleService roleService;

	private final ISysLogininforService logininfoService;

    private final ISysUserService userService;

    private final ILoginConfigService loginConfigService;

    private final ISecurityConfigService securityConfigService;

    @GetMapping("/checkUsername")
    public R<?> checkUsername(@RequestParam @NotBlank String username) {
        // 先校验用户名
        Optional<SysUser> opt = this.userService.lambdaQuery().eq(SysUser::getUserName, username).oneOpt();
        if (opt.isEmpty()) {
            throw SysErrorCode.USER_NOT_EXISTS.exception();
        }
        SysUser user = opt.get();
        if (user.isLocked()) {
            throw SysErrorCode.USER_LOCKED.exception(Objects.isNull(user.getLockEndTime()) ? "forever" : user.getLockEndTime().toString());
        } else if (UserStatus.isDisable(user.getStatus())) {
            throw SysErrorCode.USER_DISABLED.exception();
        }
        // 生成令牌
        if (this.userService.lambdaQuery().eq(SysUser::getUserName, username).count() == 0) {
            throw SysErrorCode.USER_NOT_EXISTS.exception();
        }
        return R.ok();
    }

	/**
	 * 登录方法
	 * 
	 * @param loginBody 登录信息
	 * @return 结果
	 */
	@PostMapping("/login")
	public R<?> login(@Validated @RequestBody LoginBody loginBody) {
		// 生成令牌
		String token = loginService.login(loginBody);
		return R.ok(token);
	}

	@PostMapping("/logout")
	public R<?> logout() {
		if (StpAdminUtil.isLogin()) {
            LoginUser loginUser = StpAdminUtil.getLoginUser();
            try {
                StpAdminUtil.logout();
                this.logininfoService.recordLogininfor(loginUser.getUserType(),
                        loginUser.getUserId(), loginUser.getUsername(), LoginLogType.LOGOUT, SuccessOrFail.SUCCESS,
                        StringUtils.EMPTY);
            } catch (Exception e) {
                this.logininfoService.recordLogininfor(loginUser.getUserType(),
                        loginUser.getUserId(), loginUser.getUsername(), LoginLogType.LOGOUT, SuccessOrFail.FAIL,
                        e.getMessage());
                log.error("Logout fail.", e);
            }
        }
        return R.ok();
	}

	/**
	 * 获取用户信息
	 * 
	 * @return 用户信息
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/getInfo")
	public R<?> getInfo() {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = (SysUser) loginUser.getUser();
		user.setAvatarSrc(SystemConfig.getResourcePrefix() + user.getAvatar());
		// 角色集合
		List<String> roles = this.roleService.selectRoleKeysByUserId(user.getUserId());
		// 权限集合
		List<String> permissions = loginUser.getPermissions();
		LoginUserInfoVO vo = new LoginUserInfoVO();
		vo.setUser(user);
		vo.setRoles(roles);
		vo.setPermissions(permissions);
		return R.ok(vo);
	}

	/**
	 * 获取路由信息
	 * 
	 * @return 路由信息
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/getRouters")
	public R<?> getRouters() {
		List<SysMenu> menus = this.menuService.lambdaQuery().orderByAsc(SysMenu::getOrderNum).list();

		List<String> permissions = StpAdminUtil.getLoginUser().getPermissions();
		if (!permissions.contains(ISysPermissionService.ALL_PERMISSION)) {
			menus = menus.stream().filter(m -> {
				return StringUtils.isEmpty(m.getPerms()) || permissions.contains(m.getPerms());
			}).toList();
		}
		// 国际化翻译
		I18nUtils.replaceI18nFields(menus, LocaleContextHolder.getLocale());
		// 上下级关系处理
		menus = menuService.getChildPerms(menus, 0);
		return R.ok(menuService.buildRouters(menus));
	}


    @GetMapping("/login/config")
    public R<?> getLoginConfig() {
        SysSecurityConfig securityConfig = this.securityConfigService.getSecurityConfig();
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.getCaptcha().setEnabled(false);
        loginConfig.setThirds(List.of());
        if (Objects.nonNull(securityConfig)) {
            loginConfig.getCaptcha().setEnabled(YesOrNo.isYes(securityConfig.getCaptchaEnable()));
            if (loginConfig.getCaptcha().isEnabled()) {
                loginConfig.getCaptcha().setType(securityConfig.getCaptchaType());
                loginConfig.getCaptcha().setExpires(Objects.requireNonNullElse(securityConfig.getCaptchaExpires(), 0));
                loginConfig.getCaptcha().setDuration(Objects.requireNonNullElse(securityConfig.getCaptchaDuration(), 0));
            }
            if (Objects.nonNull(securityConfig.getLoginTypeConfigIds())) {
                List<LoginConfig.ThirdLogin> thirdLogins = securityConfig.getLoginTypeConfigIds().stream().map(configId -> {
                    SysLoginConfig config = this.loginConfigService.getLoginConfig(configId);
                    LoginConfig.ThirdLogin thirdLogin = new LoginConfig.ThirdLogin();
                    thirdLogin.setType(config.getType());
                    thirdLogin.setId(configId);
                    return thirdLogin;
                }).toList();
                loginConfig.setThirds(thirdLogins);
            } else {
                loginConfig.setThirds(List.of());
            }
        }
        return R.ok(loginConfig);
    }
}
