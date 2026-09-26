package com.chestnut.system.patch.impl;

import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.patch.IUpdatePatcher;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysDeptService;
import com.chestnut.system.service.ISysRoleService;
import com.chestnut.system.service.ISysUserService;
import com.chestnut.system.utils.SysDeptUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UpdatePatcher_1_6_1 implements IUpdatePatcher {

    private final ISysUserService userService;

    private final ISysRoleService roleService;

    private final ISysDeptService deptService;

    private final RedisCache redisCache;

    @Override
    public String getVersion() {
        return "1.6.1";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update() {
        List<SysDept> topList = this.deptService.lambdaQuery().eq(SysDept::getParentId, 0).list();
        Assert.isTrue(topList.size() == 1, SysErrorCode.INVALID_TOP_DEPT::exception);
        SysDept topDept = topList.get(0);
        // v1.6.1 修正机构祖级编码
        if ("0".equals(topDept.getAncestors())) {
            List<String> cacheKeys = new ArrayList<>();
            List<SysDept> list = deptService.lambdaQuery().list();
            Map<Long, SysDept> deptMap = list.stream().collect(Collectors.toMap(SysDept::getDeptId, o -> o));
            List<TreeNode<Long>> treeNodes = list.stream().map(dept -> {
                cacheKeys.add(SysConstants.CACHE_SYS_DEPT_KEY + dept.getDeptId());
                TreeNode<Long> node = new TreeNode<>(
                        dept.getDeptId(),
                        dept.getParentId(),
                        dept.getDeptName(),
                        dept.getParentId().equals(0L)
                );
                node.setProps(Map.of("data", dept));
                return node;
            }).toList();
            List<TreeNode<Long>> tree = TreeNode.build(treeNodes);
            this.fixDeptAncestors(tree, deptMap);
            if (!this.deptService.updateBatchById(list)) {
                throw new IllegalStateException("Failed to update departments for patch 1.6.1");
            }
            this.redisCache.deleteObjects(cacheKeys);
            // 顶级机构查询与全量查询返回不同对象，使用修复后的祖级编码。
            topDept = deptMap.get(topDept.getDeptId());
            // 角色部门字段更新
            List<SysRole> roles = this.roleService.list();
            for (SysRole role : roles) {
                role.setDeptId(topDept.getDeptId());
                role.setDeptAncestors(topDept.getAncestors());
            }
            if (!roles.isEmpty() && !this.roleService.updateBatchById(roles)) {
                throw new IllegalStateException("Failed to update roles for patch 1.6.1");
            }
            // 用户部门字段更新
            List<SysUser> users = this.userService.list();
            for (SysUser user : users) {
                if (!IdUtils.validate(user.getDeptId())) {
                    user.setDeptId(topDept.getDeptId());
                    user.setDeptAncestors(topDept.getAncestors());
                } else {
                    SysDept dept = deptMap.get(user.getDeptId());
                    if (Objects.isNull(dept)) {
                        user.setDeptId(topDept.getDeptId());
                        user.setDeptAncestors(topDept.getAncestors());
                    } else {
                        user.setDeptAncestors(dept.getAncestors());
                    }
                }
            }
            if (!users.isEmpty() && !this.userService.updateBatchById(users)) {
                throw new IllegalStateException("Failed to update users for patch 1.6.1");
            }
            // 清空所有登录TOKEN
            String tokenPrefix = StpAdminUtil.getStpLogic().getConfigOrGlobal().getTokenPrefix();
            redisCache.deleteByPrefix(tokenPrefix + ":" + AdminUserType.TYPE + ":");
        }
    }

    private void fixDeptAncestors(List<TreeNode<Long>> list, Map<Long, SysDept> deptMap) {
        for (TreeNode<Long> node : list) {
            if (node.getParentId().equals(0L)) {
                deptMap.get(node.getId()).setAncestors(node.getId().toString());
            } else {
                SysDept parent = deptMap.get(node.getParentId());
                deptMap.get(node.getId()).setAncestors(SysDeptUtils.getDeptAncestors(parent, node.getId()));
            }
            if (StringUtils.isNotEmpty(node.getChildren())) {
                fixDeptAncestors(node.getChildren(), deptMap);
            }
        }
    }
}
