package com.chestnut.system.service.impl;

import cn.dev33.satoken.session.SaSession;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.SecurityUtils;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.*;
import com.chestnut.system.domain.dto.ChangeUserDeptRequest;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.mapper.SysUserRoleMapper;
import com.chestnut.system.permission.IPermissionType;
import com.chestnut.system.permission.impl.RolePermissionOwnerType;
import com.chestnut.system.permission.impl.UserPermissionOwnerType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysDeptService;
import com.chestnut.system.service.ISysPermissionService;
import com.chestnut.system.service.ISysRoleService;
import com.chestnut.system.service.ISysUserService;
import com.chestnut.system.utils.SysDeptUtils;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserPermissionService {

    private final ISysDeptService deptService;
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final SysUserRoleMapper userRoleMapper;
    private final ISysPermissionService permissionService;
    private final UserPermissionOwnerType userPermissionOwnerType;
    private final RolePermissionOwnerType rolePermissionOwnerType;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    private static final String LOCK_DEPT_CHANGE = "sys:dept:change:";

    /**
     * 调动用户部门
     */
    public void changeUserDept(ChangeUserDeptRequest req) {
        LoginUser operator = req.getOperator();
        RLock lock = redissonClient.getLock(LOCK_DEPT_CHANGE);
        try {
            lock.lock();
            SysUser user = this.userService.getById(req.getUserId());
            Assert.notNull(user, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("userId", req.getUserId()));
            if (user.getDeptId().equals(req.getDeptId())) {
                return;
            }
            SysDept dept = this.deptService.getDept(req.getDeptId());
            Assert.notNull(dept, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("deptId", req.getDeptId()));
            // 校验机构范围，同时拥有用户当前部门和调动目标部门权限
            SysDeptUtils.checkDeptScope(operator, user::getDeptId, user::getDeptAncestors);
            SysDeptUtils.checkDeptScope(operator, dept::getDeptId, dept::getAncestors);

            user.setDeptId(dept.getDeptId());
            user.setDeptAncestors(dept.getAncestors());
            user.updateBy(req.getOperator().getUsername());
            this.transactionTemplate.executeWithoutResult(status -> {
                this.userService.updateById(user);
                // 部门变更移除角色关联
                this.userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getUserId()));
            });
            // 更新用户token数据
            this.resetLoginUser(req.getUserId());
        } finally {
            lock.unlock();
        }
    }

    public void removeUsersFromRole(Long roleId, List<Long> userIds, LoginUser operator) {
        SysRole role = this.roleService.getRole(roleId);
        Assert.notNull(role, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(roleId));
        // 是否有角色操作权限
        SysDeptUtils.checkDeptScope(operator, role::getDeptId, role::getDeptAncestors);

        LambdaQueryWrapper<SysUserRole> q = new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId)
                .in(SysUserRole::getUserId, userIds);
        userRoleMapper.delete(q);
        // 重置用户登录TOKEN
        userIds.forEach(this::resetLoginUser);
    }

    public void addUsersToRole(Long roleId, List<Long> userIds, LoginUser operator) {
        RLock lock = redissonClient.getLock(LOCK_DEPT_CHANGE);
        try {
            lock.lock();
            SysRole role = this.roleService.getRole(roleId);
            Assert.notNull(role, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(roleId));
            // 是否有角色操作权限
            SysDept roleDept = SysDeptUtils.getDept(role.getDeptId());
            SysDeptUtils.checkDeptScope(operator, role::getDeptId, role::getDeptAncestors);
            // 已关联用户UIDS
            List<Long> existUserIds = this.userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId)
                    .in(SysUserRole::getUserId, userIds)).stream().map(SysUserRole::getUserId).toList();
            // 查询用户列表过滤已存在
            List<SysUser> users = this.userService.listByIds(userIds).stream().filter(user -> {
                return !existUserIds.contains(user.getUserId());
            }).toList();
            List<SysUserRole> userRoleList = new ArrayList<>();
            for (SysUser user : users) {
                if (!user.getDeptId().equals(roleDept.getDeptId())) {
                    throw SysErrorCode.USER_DEPT_NOT_MATCH_ROLE_DEPT.exception();
                }
                SysUserRole ur = SysUserRole.of(user, roleId);
                userRoleList.add(ur);
            }
            this.transactionTemplate.executeWithoutResult(status -> {
                this.userRoleMapper.insert(userRoleList);
            });
            // 重置用户登录TOKEN
            userIds.forEach(this::resetLoginUser);
        } finally {
            lock.unlock();
        }
    }

    public void resetUserRoles(final Long userId, final List<Long> roleIds, LoginUser operator) {
        RLock lock = redissonClient.getLock(LOCK_DEPT_CHANGE);
        try {
            lock.lock();
            SysUser user = this.userService.getById(userId);
            SysDeptUtils.checkDeptScope(operator, user::getDeptId, user::getDeptAncestors);
            List<SysRole> roles = StringUtils.isNotEmpty(roleIds) ? this.roleService.listByIds(roleIds) : List.of();
            for (SysRole role : roles) {
                boolean check = Objects.nonNull(role) && role.getDeptId().equals(user.getDeptId());
                Assert.isTrue(check, SysErrorCode.USER_DEPT_NOT_MATCH_ROLE_DEPT::exception);
            }
            transactionTemplate.executeWithoutResult(status -> {
                userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
                if (!roles.isEmpty()) {
                    List<SysUserRole> list = roles.stream().map(role -> SysUserRole.of(user, role.getRoleId())).toList();
                    list.forEach(userRoleMapper::insert);
                }
            });
            this.resetLoginUser(userId);
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getLoginUserPermissions(LoginUser loginUser) {
        return this.getUserPermissions(loginUser.getUserId());
    }

    public Set<String> getUserPermissions(Long userId) {
        Set<String> permissions = new HashSet<>();
        if (SecurityUtils.isSuperAdmin(userId)) {
            permissions.add(ISysPermissionService.ALL_PERMISSION);
        } else {
            // 用户权限
            Set<String> permissionKeys = this.userPermissionOwnerType.getPermissionKeys(userId.toString());
            permissions.addAll(permissionKeys);
            // 角色权限
            this.roleService.selectRolesByUserId(userId).stream()
                    .filter(role -> EnableOrDisable.isEnable(role.getStatus()))
                    .forEach(r -> {
                        Set<String> keys = this.rolePermissionOwnerType.getPermissionKeys(r.getRoleId().toString());
                        permissions.addAll(keys);
                    });
        }
        return permissions;
    }

    public void grantUserPermissions(Long userId, String permissionType, String permissionJson) {
        this.permissionService.grantPermission(
                UserPermissionOwnerType.TYPE,
                userId.toString(),
                permissionType,
                permissionJson
        );
    }

    public void resetLoginUser(String ownerType, String owner) {
        if (UserPermissionOwnerType.TYPE.equals(ownerType)) {
            this.resetLoginUser(Long.valueOf(owner));
        } else if (RolePermissionOwnerType.TYPE.equals(ownerType)) {
            List<Long> userIds = this.roleService.getUserIdsByRole(Long.valueOf(owner));
            userIds.forEach(this::resetLoginUser);
        }
    }

    public void resetLoginUser(Long userId) {
        SysUser user = this.userService.getById(userId);
        if (Objects.isNull(user)) {
            return;
        }
        List<String> userPermissions = this.getUserPermissions(userId).stream().toList();
        StpAdminUtil.getTokenValueListByLoginId(userId).forEach(token -> {
            SaSession session = StpAdminUtil.getTokenSessionByToken(token);
            LoginUser loginUser = (LoginUser) session.get(SaSession.USER);
            loginUser.setPermissions(userPermissions);
            loginUser.setUser(user);
            loginUser.setDeptId(user.getDeptId());
            session.set(SaSession.USER, loginUser);
        });
    }

    public Set<String> getInheritedPermissionKeys(String ownerType, String owner, String permissionType) {
        Set<String> inheritedPermissionKeys = new HashSet<>();
        IPermissionType pt = this.permissionService.getPermissionType(permissionType);
        if (UserPermissionOwnerType.TYPE.equals(ownerType)) {
            // 查找用户继承自角色的权限
            List<SysRole> roles = this.roleService.selectRolesByUserId(Long.valueOf(owner))
                    .stream().filter(role -> EnableOrDisable.isEnable(role.getStatus())).toList();
            if (!roles.isEmpty()) {
                List<SysPermission> rolePermissions = this.permissionService.lambdaQuery()
                        .eq(SysPermission::getOwnerType, RolePermissionOwnerType.TYPE)
                        .in(SysPermission::getOwner, roles.stream().map(SysRole::getRoleId).toList())
                        .list();
                rolePermissions.forEach(rolePermission -> {
                    String json = rolePermission.getPermissions().get(pt.getId());
                    inheritedPermissionKeys.addAll(pt.deserialize(json));
                });
            }
        }
        return inheritedPermissionKeys;
    }
}
