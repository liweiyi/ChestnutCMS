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
package com.chestnut.system.security;

import cn.dev33.satoken.session.SaSession;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.enums.DeviceType;
import com.chestnut.common.utils.IP2RegionUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.LoginLogType;
import com.chestnut.system.fixed.dict.SuccessOrFail;
import com.chestnut.system.fixed.dict.UserStatus;
import com.chestnut.system.service.ISysDeptService;
import com.chestnut.system.service.ISysLogininforService;
import com.chestnut.system.service.ISysUserService;
import com.chestnut.system.service.impl.UserPermissionService;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * 登录校验方法
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class SysLoginService {

	public static final String CAPTCHA_SCENE = "syslogin";

	private final ISysDeptService deptService;

	private final ISysUserService userService;

	private final ISysLogininforService logininfoService;

	private final UserPermissionService userPermissionService;

	/**
	 * 查找正常用户，根据指定字段查询或查询username+email+phonenumber
	 */
	public SysUser findValidUser(String accountName, SFunction<SysUser, ?> findColumn) {
		LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<>();
		if (Objects.nonNull(findColumn)) {
			q.eq(findColumn, accountName);
		} else {
			q.eq(SysUser::getUserName, accountName)
					.or().eq(SysUser::getEmail, accountName)
					.or().eq(SysUser::getPhoneNumber, accountName);
		}
		Page<SysUser> users = userService.page(new Page<>(1, 2, false), q);
		if (users.getRecords().size() != 1) {
			// 以防万一存在重复用户，最多查2条数据，只要返回值不等于1都算非正常用户
			throw SysErrorCode.USER_NOT_EXISTS.exception();
		}
		SysUser user = users.getRecords().get(0);
		if (user.isLocked()) {
			throw SysErrorCode.USER_LOCKED.exception(user.getLockEndTime() == null
					? "forever" : user.getLockEndTime().toString());
		}
		if (UserStatus.DISABLE.equals(user.getStatus())) {
			throw SysErrorCode.USER_DISABLED.exception();
		}
		return user;
	}

	/**
	 * 系统用户登录
	 *
	 * @param user 用户信息
	 * @return 登录TOKEN
	 */
    public String userLogin(SysUser user) {
        // 记录用户最近登录时间和ip地址
        user.setLoginIp(ServletUtils.getIpAddr(ServletUtils.getRequest()));
        user.setLoginDate(LocalDateTime.now());
        userService.lambdaUpdate()
                .set(SysUser::getLoginIp, user.getLoginIp())
                .set(SysUser::getLoginDate, user.getLoginDate())
                .eq(SysUser::getUserId, user.getUserId())
                .update();
		SysDept dept = deptService.getDept(user.getDeptId());
		if (Objects.nonNull(dept)) {
			user.setDeptName(dept.getDeptName());
		}
		// 生成token
        LoginUser loginUser = createLoginUser(user);
        StpAdminUtil.login(user.getUserId(), DeviceType.PC.value());
        loginUser.setToken(StpAdminUtil.getTokenValueByLoginId(user.getUserId()));
        StpAdminUtil.getTokenSession().set(SaSession.USER, loginUser);
        // 日志
        this.logininfoService.recordLogininfor(AdminUserType.TYPE, user.getUserId(),
                loginUser.getUsername(), LoginLogType.LOGIN, SuccessOrFail.SUCCESS, StringUtils.EMPTY);
        return StpAdminUtil.getTokenValue();
    }

	public LoginUser createLoginUser(SysUser user) {
		LoginUser loginUser = new LoginUser();
		loginUser.setUserId(user.getUserId());
		loginUser.setDeptId(user.getDeptId());
		loginUser.setUserType(AdminUserType.TYPE);
		loginUser.setUsername(user.getUserName());
		loginUser.setLoginTime(Instant.now().toEpochMilli());
		loginUser.setLoginLocation(IP2RegionUtils.ip2Region(user.getLoginIp()));
		loginUser.setIpaddr(user.getLoginIp());
		UserAgent ua = UserAgent.parseUserAgentString(ServletUtils.getUserAgent());
		loginUser.setOs(ua.getOperatingSystem().name());
		loginUser.setBrowser(ua.getBrowser() + "/" + ua.getBrowserVersion());
		loginUser.setUser(user);

		Set<String> permissions = this.userPermissionService.getLoginUserPermissions(loginUser);
		loginUser.setPermissions(permissions.stream().toList());
		return loginUser;
	}
}
