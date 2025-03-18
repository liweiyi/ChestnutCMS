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
package com.chestnut.member.core;

import java.util.Set;

/**
 * MemberPrivilegeProvider
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface MemberPrivType {

    /**
     * 获取指定类型权限拥有者的权限列表
     *
     * @param ownerType 权限所有者类型
     * @param owner 权限所有者唯一标识
     * @return 权限列表
     */
    default Set<String> getPrivileges(String ownerType, String owner) {

        return Set.of();
    }

    /**
     * 保存权限数据
     */
    default void savePrivileges(String ownerType, String owner, Set<String> privKeys) {

    }

    /**
     * 判断指定会员是否拥有指定权限
     *
     * @param memberId
     * @param privKey
     * @return
     */
    boolean checkPriv(Long memberId, String privKey);
}
