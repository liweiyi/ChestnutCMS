package com.chestnut.member.core.impl;

import com.chestnut.member.core.MemberPrivType;

import java.util.Set;

/**
 * 会员菜单权限
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class MemberMenuPrivType implements MemberPrivType {


    @Override
    public boolean checkPriv(Long memberId, String privKey) {
        return false;
    }
}
