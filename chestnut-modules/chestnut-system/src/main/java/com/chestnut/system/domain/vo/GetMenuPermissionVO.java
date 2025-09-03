/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.system.domain.vo;

import com.chestnut.system.domain.SysMenu;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * GetMenuPermissionVO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class GetMenuPermissionVO {

    /**
     * 菜单列表
     */
    private List<SysMenu> menus;

    /**
     * 拥有的权限列表
     */
    private Set<String> perms;

    /**
     * 不可变更的权限列表
     */
    private Set<String> disabledPerms;
}
