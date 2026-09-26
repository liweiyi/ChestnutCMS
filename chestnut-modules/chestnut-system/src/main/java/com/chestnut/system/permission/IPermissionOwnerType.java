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
package com.chestnut.system.permission;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysPermission;
import com.chestnut.system.mapper.SysPermissionMapper;
import com.chestnut.system.utils.SysDeptUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限所有者类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IPermissionOwnerType<T extends HasDept> {

    String BEAN_PREFIX = "PermissionOwnerType_";

    /**
     * 唯一标识
     */
    String getType();

    SysPermissionMapper getPermissionMapper();

    List<IPermissionType> getPermissionTypes();

    /**
     * 所有者实例
     */
    T getOwnerInstance(Serializable owner);

    /**
     * 校验操作人部门范围
     *
     * @param operator 操作人
     * @param owner 所有者标识
     */
    default void checkScope(LoginUser operator, String owner) {
        T o = this.getOwnerInstance(owner);
        Assert.notNull(o, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("owner", owner));
        SysDeptUtils.checkDeptScope(operator, o::getDeptId, o::getDeptAncestors);
    }

    default Set<String> getPermissionKeys(String owner) {
        return getPermissionKeys(owner, null);
    }

    default Set<String> getPermissionKeys(String owner, String permissionType) {
        Set<String> permissionKeys = new HashSet<>();
        // 权限
        List<SysPermission> list = new LambdaQueryChainWrapper<>(this.getPermissionMapper())
                .eq(SysPermission::getOwnerType, this.getType())
                .eq(SysPermission::getOwner, owner)
                .list();
        if (!list.isEmpty()) {
            SysPermission userPermission = list.get(0);
            this.getPermissionTypes().forEach(pt -> {
                if (StringUtils.isEmpty(permissionType) || pt.getId().equals(permissionType)) {
                    String json = userPermission.getPermissions().get(pt.getId());
                    if (StringUtils.isNotEmpty(json)) {
                        permissionKeys.addAll(pt.deserialize(json));
                    }
                }
            });
        }
        return permissionKeys;
    }
}
