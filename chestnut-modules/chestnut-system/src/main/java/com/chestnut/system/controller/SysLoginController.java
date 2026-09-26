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
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.config.SystemConfig;
import com.chestnut.system.config.properties.SysProperties;
import com.chestnut.system.domain.SysLoginConfig;
import com.chestnut.system.domain.SysMenu;
import com.chestnut.system.domain.SysSecurityConfig;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.dto.LoginBody;
import com.chestnut.system.domain.vo.LoginConfig;
import com.chestnut.system.domain.vo.LoginUserInfoVO;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.*;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.security.SysLoginService;
import com.chestnut.system.security.config.LoginSecurity;
import com.chestnut.system.security.config.LoginSecurityConfigType;
import com.chestnut.system.security.config.PasswordSecurity;
import com.chestnut.system.security.config.PasswordSecurityConfigType;
import com.chestnut.system.service.*;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 登录验证
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.SYS.LOGIN.API}")
@Slf4j
@RestController
@RequiredArgsConstructor
public class SysLoginController extends BaseRestController {

    private final SysProperties sysProperties;

	private final SysLoginService loginService;

	private final ISysMenuService menuService;

	private final ISysRoleService roleService;

	private final ISysLogininforService logininfoService;

    private final ISysUserService userService;

    private final ILoginConfigService loginConfigService;

    private final PasswordSecurityConfigType passwordSecurityConfigType;

    private final LoginSecurityConfigType loginSecurityConfigType;

    private final ISecurityConfigService securityConfigService;

    @XComment("{API.DOC.SYS.LOGIN.CHECK_USERNAME}")
    @GetMapping("/checkUsername")
    public R<?> checkUsername(@RequestParam @NotBlank String username) {
        // 先校验用户名
        List<SysUser> users = this.userService.lambdaQuery().eq(SysUser::getUserName, username)
                .or().eq(SysUser::getPhoneNumber, username)
                .or().eq(SysUser::getEmail, username).list();
        if (users.size() != 1) {
            throw SysErrorCode.USER_NOT_EXISTS.exception();
        }
        SysUser user = users.get(0);
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
    @XComment("{API.DOC.SYS.LOGIN.LOGIN}")
	@PostMapping("/login")
	public R<?> login(@Validated @RequestBody LoginBody loginBody) {
		// 生成令牌
		String token = loginService.login(loginBody);
		return R.ok(token);
	}

    @XComment("{API.DOC.SYS.LOGIN.LOGOUT}")
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
    @XComment("{API.DOC.SYS.LOGIN.GET_INFO}")
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
        vo.setSuperAdmin(loginUser.isSuperAdministrator());
		vo.setRoles(roles);
		vo.setPermissions(permissions);
        SysSecurityConfig securityConfig = this.securityConfigService.getSecurityConfig();
        if (Objects.nonNull(securityConfig)) {
            PasswordSecurity config = passwordSecurityConfigType.getConfig(securityConfig.getConfigs());
            if (config.getExpireSeconds() > 0) {
                LocalDateTime lastModifyPwdTime = Objects.isNull(user.getPasswordModifyTime())
                        ? user.getCreateTime() : user.getPasswordModifyTime();
                if (lastModifyPwdTime.plusSeconds(config.getExpireSeconds()).isBefore(LocalDateTime.now())) {
                    vo.getUser().setIsPasswordExpired(true);
                }
            }
        }
		return R.ok(vo);
	}

	/**
	 * 获取路由信息
	 * 
	 * @return 路由信息
	 */
    @XComment("{API.DOC.SYS.LOGIN.GET_ROUTERS}")
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
		menus = menuService.getChildPerms(menus, "0");
		return R.ok(menuService.buildRouters(menus));
	}

    @XComment("{API.DOC.SYS.LOGIN.GET_LOGIN_CONFIG}")
    @GetMapping("/login/config")
    public R<?> getLoginConfig() {
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.getCaptcha().setEnabled(false);
        loginConfig.setThirds(List.of());
        loginConfig.setDemoMode(sysProperties.isDemoMode());

        List<LoginConfig.ThirdLogin> thirdLogins = this.loginConfigService.lambdaQuery()
                .eq(SysLoginConfig::getStatus, EnableOrDisable.ENABLE).list()
                .stream().map(lc -> new LoginConfig.ThirdLogin(lc.getConfigId(), lc.getType(), lc.getConfigName())).toList();
        loginConfig.setThirds(thirdLogins);

        SysSecurityConfig securityConfig = this.securityConfigService.getSecurityConfig();
        if (Objects.nonNull(securityConfig)) {
            LoginSecurity loginSecurity = loginSecurityConfigType.getConfig(securityConfig.getConfigs());
            loginConfig.getCaptcha().setEnabled(YesOrNo.isYes(loginSecurity.getCaptchaEnable()));
            if (loginConfig.getCaptcha().isEnabled()) {
                loginConfig.getCaptcha().setType(loginSecurity.getCaptchaType());
                loginConfig.getCaptcha().setExpires(Objects.requireNonNullElse(loginSecurity.getCaptchaExpires(), 0));
                loginConfig.getCaptcha().setDuration(Objects.requireNonNullElse(loginSecurity.getCaptchaDuration(), 0));
            }
        }
        return R.ok(loginConfig);
    }
}
