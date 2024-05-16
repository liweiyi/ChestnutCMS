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
